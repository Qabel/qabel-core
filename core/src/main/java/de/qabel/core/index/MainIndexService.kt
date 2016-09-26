package de.qabel.core.index

import de.qabel.core.config.Contact
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.config.VerificationStatus
import de.qabel.core.index.server.ExternalContactsAccessor
import de.qabel.core.index.server.IndexServer
import de.qabel.core.logging.QabelLog
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.util.DefaultHashMap

class MainIndexService(private val indexServer: IndexServer,
                       private val contactRepository: ContactRepository,
                       private val identityRepository: IdentityRepository) : IndexService, QabelLog {

    override fun updateIdentity(identity: Identity, oldIdentity: Identity?) {
        oldIdentity?.let {
            if (it.alias != identity.alias) {
                removeIdentity(it)
            }
        }
        identity.emailStatus = updateFieldValueIfRequired(identity, FieldType.EMAIL, identity.email, oldIdentity?.email)
        identity.phoneStatus = updateFieldValueIfRequired(identity, FieldType.PHONE, identity.phone, oldIdentity?.phone)
        identityRepository.save(identity)
    }

    override fun updateIdentities() {
        identityRepository.findAll().identities.forEach {
            updateIdentity(it)
        }
    }

    private fun updateFieldValueIfRequired(identity: Identity, fieldType: FieldType, newValue: String?,
                                           oldValue: String? = null): VerificationStatus {
        //Delete old value if exists, changed and is set on server
        if (newValue != oldValue &&
            findStateForValue(identity, oldValue, fieldType) == VerificationStatus.VERIFIED) {
            info("Removing field from index $fieldType")
            UpdateIdentity(identity, listOf(UpdateField(UpdateAction.DELETE, fieldType, oldValue!!))).let {
                indexServer.updateIdentity(it).let {
                    if (it == UpdateResult.ACCEPTED_DEFERRED) {
                        warn("Received unexpected UpdateResult[${it.name}] for delete action!")
                        throw IndexServerException("Failed to delete old field value!")
                    }
                }
            }
        }

        val currentStatus = findStateForValue(identity, newValue, fieldType)
        return if (currentStatus == VerificationStatus.NOT_VERIFIED)
            UpdateIdentity(identity, listOf(UpdateField(UpdateAction.CREATE, fieldType, newValue!!))).let {
                info("Updating index with field $fieldType")
                when (indexServer.updateIdentity(it)) {
                    UpdateResult.ACCEPTED_IMMEDIATE -> VerificationStatus.VERIFIED
                    else -> VerificationStatus.NOT_VERIFIED
                }.apply {
                    info("Index updated. New VerificationStatus ${this.name}")
                }
            }
        else currentStatus
    }

    private fun findStateForValue(identity: Identity, value: String?, fieldType: FieldType): VerificationStatus =
        if (value.isNullOrBlank()) {
            VerificationStatus.NONE
        } else if (indexServer.search(mapOf(Pair(fieldType, value!!))).any {
            it.publicKey.readableKeyIdentifier == identity.keyIdentifier
        }) {
            //Identity found by field
            VerificationStatus.VERIFIED
        } else {
            VerificationStatus.NOT_VERIFIED
        }

    override fun updateIdentityVerifications() {
        val identities = identityRepository.findAll()
        identities.identities.forEach { updateIdentityVerificationFlags(it) }
    }

    private fun updateIdentityVerificationFlags(identity: Identity) {
        val emailStatus = findStateForValue(identity, identity.email, FieldType.EMAIL)
        if (emailStatus != identity.emailStatus) {
            identity.emailStatus = emailStatus
            info("EmailStatus changed to ${identity.emailStatus.name} for identity ${identity.keyIdentifier}")
        }
        val phoneStatus = findStateForValue(identity, identity.phone, FieldType.PHONE)
        if (emailStatus != identity.phoneStatus) {
            identity.phoneStatus = phoneStatus
            info("PhoneStatus changed to ${identity.phoneStatus.name} for identity ${identity.keyIdentifier}")
        }
        identityRepository.save(identity)
    }

    override fun removeIdentity(identity: Identity) {
        if (!identity.phone.isNullOrBlank() || !identity.email.isNullOrBlank())
            UpdateIdentity.fromIdentity(identity, UpdateAction.DELETE).let {
                indexServer.updateIdentity(it)
                updateIdentityVerificationFlags(identity)
            }
    }

    override fun removeIdentities() {
        identityRepository.findAll().identities.forEach {
            removeIdentity(it)
        }
    }

    override fun confirmVerification(code: String) {
        indexServer.confirmVerificationCode(code)
        updateIdentityVerifications()
    }

    override fun searchContacts(email: String, phone: String): List<Contact> {
        val data = mutableListOf<IndexSearch>().apply {
            if (email.isNotBlank()) {
                add(IndexSearch(FieldType.EMAIL, email))
            }
            if (phone.isNotBlank()) {
                add(IndexSearch(FieldType.PHONE, phone))
            }
        }

        if (data.isEmpty()) {
            return emptyList()
        }

        val resultMap = mutableMapOf<String, Contact>()
        data.forEach { search ->
            val indexResult = indexServer.search(search.toMap())
            indexResult.forEach { indexIdentity ->
                val contact = resultMap.getOrPut(indexIdentity.publicKey.readableKeyIdentifier,
                    { indexIdentity.toContact() })
                when (search.fieldType) {
                    FieldType.EMAIL -> contact.email = search.value
                    FieldType.PHONE -> contact.phone = search.value
                }
            }
        }

        return resultMap.values.toList()
    }

    /**
     * Search the index for the external contacts and updates the local contact repository.
     * @Returns a list of [IndexSyncResult] for created and updated [Contact]s
     */
    override fun syncContacts(externalContactsAccessor: ExternalContactsAccessor): List<IndexSyncResult> {
        val externalContacts: List<RawContact> = externalContactsAccessor.getContacts()

        //Map lists of values to a list of fieldType and value associated with its raw contact
        val searchValues: Map<RawContact, List<IndexSearch>> =
            externalContacts.associate {
                Pair(it, mutableListOf<IndexSearch>().apply {
                    addAll(it.emailAddresses.map {
                        IndexSearch(FieldType.EMAIL, it)
                    })
                    addAll(it.mobilePhoneNumbers.map {
                        IndexSearch(FieldType.PHONE, it)
                    })
                })
            }

        val searchResults = DefaultHashMap<IndexContact, MutableList<IndexResult>>({ mutableListOf() })
        searchValues.forEach {
            val (contact, values) = it
            values.forEach { search ->
                debug("IndexSearch for ${contact.displayName} with ${search.value} (${search.fieldType.name})")
                indexServer.search(search.toMap()).forEach { indexContact ->
                    debug("Received IndexContact for ${contact.displayName} with ${search.value}. Received ${indexContact.alias} ${indexContact.publicKey.readableKeyIdentifier}")
                    val indexResults = searchResults.getOrDefault(indexContact)
                    indexResults.find({ it.rawContact == contact })?.let {
                        it.search.add(search)
                    } ?: indexResults.add(IndexResult(contact, mutableListOf(search)))
                    Unit
                }
            }
        }

        val results = mutableListOf<IndexSyncResult>()
        val identities = identityRepository.findAll()
        searchResults.map {
            val (indexContact, matchedSearches) = it
            val receivedContact = prepareIndexResults(indexContact, matchedSearches)

            handleIndexContact(receivedContact, identities)?.let {
                debug("IndexResult Contact ${it.contact.alias} ${it.action.name}")
                results.add(it)
            }
        }
        return results
    }

    /**
     * Converts the [IndexContact] and apply best matching data by the [RawContact]s
     */
    private fun prepareIndexResults(indexContact: IndexContact, results: MutableList<IndexResult>): Contact {
        val receivedContact = indexContact.toContact()

        //Apply matched data, results cannot be empty
        val bestMatch = results.maxBy { it.search.size }!!
        var phone = bestMatch.search.find {
            it.fieldType == FieldType.PHONE
        }?.value ?: ""
        if (phone.isEmpty()) {
            results.forEach {
                it.search.find { it.fieldType == FieldType.PHONE }?.let {
                    phone = it.value
                }
            }
        }
        var mail = bestMatch.search.find {
            it.fieldType == FieldType.EMAIL
        }?.value ?: ""
        if (mail.isEmpty()) {
            results.forEach {
                it.search.find { it.fieldType == FieldType.EMAIL }?.let {
                    mail = it.value
                }
            }
        }

        if (phone.isNotEmpty()) {
            receivedContact.phone = phone
        }
        if (mail.isNotEmpty()) {
            receivedContact.email = mail
        }
        receivedContact.nickName = bestMatch.rawContact.displayName

        return receivedContact
    }

    /**
     * Internal usage to handle index search.
     *
     */
    private data class IndexResult(val rawContact: RawContact, val search: MutableList<IndexSearch>)

    /**
     * Handles [Contact] created by [IndexContact] from server. Creates IndexSyncResult if a new qabel contact found or contact is updated.
     *
     */
    private fun handleIndexContact(receivedContact: Contact, identities: Identities): IndexSyncResult? {
        if (contactRepository.exists(receivedContact)) {
            val contactData = contactRepository.findContactWithIdentities(receivedContact.keyIdentifier)
            val localContact = contactData.contact

            var updated: Boolean = false

            if (localContact.alias != receivedContact.alias) {
                localContact.alias = receivedContact.alias
                updated = true
            }

            //Set nick from external contact
            if (localContact.nickName.isNullOrBlank() || localContact.alias == localContact.nickName) {
                localContact.nickName = receivedContact.nickName
                updated = true
            }

            //Apply matched data
            if (localContact.phone.isNullOrEmpty() && !receivedContact.phone.isNullOrEmpty()) {
                localContact.phone = receivedContact.phone
                updated = true
            }
            if (localContact.email.isNullOrEmpty() && !receivedContact.email.isNullOrEmpty()) {
                localContact.email = receivedContact.email
                updated = true
            }
            if (updated) {
                contactRepository.update(localContact, contactData.identities)
                return IndexSyncResult(localContact, IndexSyncAction.UPDATE)
            }
            return null
        } else {
            val identitySet = identities.identities
            if (identitySet.size == 1) {
                contactRepository.save(receivedContact, identitySet.first())
            } else {
                //Add unassigned contact
                contactRepository.persist(receivedContact, emptyList())
            }
            return IndexSyncResult(receivedContact, IndexSyncAction.CREATE)
        }
    }
}

package de.qabel.core.index

import de.qabel.core.config.Contact
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.config.VerificationStatus
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.util.DefaultHashMap

class MainIndexInteractor(private val indexServer: IndexServer,
                          private val contactRepository: ContactRepository,
                          private val identityRepository: IdentityRepository) : IndexInteractor {

    override fun updateIdentity(identity: Identity) {
        identity.emailStatus = updateFieldValueIfRequired(identity, FieldType.EMAIL, identity.email)
        identity.phoneStatus = updateFieldValueIfRequired(identity, FieldType.PHONE, identity.phone)
        identityRepository.save(identity)
    }

    override fun updateIdentityPhone(identity: Identity, oldPhone: String) {
        identity.phoneStatus = updateFieldValueIfRequired(identity, FieldType.PHONE, identity.phone, oldPhone)
        identityRepository.save(identity)
    }

    override fun updateIdentityEmail(identity: Identity, oldEmail: String) {
        identity.emailStatus = updateFieldValueIfRequired(identity, FieldType.EMAIL, identity.email, oldEmail)
        identityRepository.save(identity)
    }

    private fun updateFieldValueIfRequired(identity: Identity, fieldType: FieldType, newValue: String?,
                                           oldValue: String? = null): VerificationStatus {
        //Delete old value if exists and is set on server
        if (findStateForValue(identity, oldValue, fieldType) == VerificationStatus.VERIFIED) {
            UpdateIdentity(identity, listOf(UpdateField(UpdateAction.DELETE, fieldType, oldValue!!))).let {
                if (indexServer.updateIdentity(it) == UpdateResult.ACCEPTED_IMMEDIATE) {
                    throw IndexServerException("Failed to delete old field value!")
                }
            }
        }
        val currentStatus = findStateForValue(identity, newValue, fieldType)
        return when (currentStatus) {
            VerificationStatus.NOT_VERIFIED ->
                UpdateIdentity(identity, listOf(UpdateField(UpdateAction.CREATE, fieldType, oldValue!!))).let {
                    handleUpdateIdentity(it)
                }
            else -> currentStatus
        }
    }

    private fun handleUpdateIdentity(update: UpdateIdentity): VerificationStatus =
        when (indexServer.updateIdentity(update)) {
            UpdateResult.ACCEPTED_IMMEDIATE -> VerificationStatus.VERIFIED
            else -> VerificationStatus.NOT_VERIFIED
        }

    private fun findStateForValue(identity: Identity, value: String?, fieldType: FieldType): VerificationStatus =
        if (value.isNullOrBlank()) {
            VerificationStatus.NONE
        } else if (indexServer.search(mapOf(Pair(fieldType, value!!))).any {
            it.publicKey.equals(identity.ecPublicKey)
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
        identity.emailStatus = findStateForValue(identity, identity.email, FieldType.EMAIL)
        identity.phoneStatus = findStateForValue(identity, identity.phone, FieldType.PHONE)
        identityRepository.save(identity)
    }

    override fun deleteIdentity(identity: Identity) {
        UpdateIdentity.fromIdentity(identity, UpdateAction.DELETE).let {
            indexServer.updateIdentity(it)
        }
    }

    override fun confirmVerification(code: String) {
        indexServer.confirmVerificationCode(code)
        updateIdentityVerifications()
    }

    /**
     * Search the index for the external contacts and updates the local contact repository.
     * Returns a list of new contacts.
     *
     * TODO With my last android researches my phone would requires 265 HttpRequests (265 values, 237 contacts)
     *
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
            values.forEach {
                val search = it
                indexServer.search(search.toMap()).forEach { indexContact ->
                    searchResults.getOrDefault(indexContact).apply {
                        find({ it.rawContact == contact })?.let {
                            it.search.add(search)
                        } ?: add(IndexResult(contact, mutableListOf(it)))
                    }
                }
            }
        }

        val results = mutableListOf<IndexSyncResult>()
        val identities = identityRepository.findAll()
        searchResults.map {
            val (indexContact, matchedSearches) = it
            val receivedContact = prepareIndexResults(indexContact, matchedSearches)

            handleIndexContact(receivedContact, identities)?.let {
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
     * TODO We updating our identity contact if we modified the identity on another device. Bug or Feature? :D Easy fix -> contactData knows if contact is an an identity.
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

            //TODO Force update for alias, email, phone?

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

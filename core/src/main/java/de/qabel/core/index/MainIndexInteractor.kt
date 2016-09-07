package de.qabel.core.index

import de.qabel.core.config.Contact
import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.util.DefaultHashMap

class MainIndexInteractor(private val indexServer: IndexServer,
                          private val contactRepository: ContactRepository,
                          private val identityRepository: IdentityRepository) : IndexInteractor {

    /**
     * TODO How to handle verifications?
     * 1. save as Not verified -> Save update action? (simple way : add phoneVerified and emailVerified with Enum to identity)
     * 2. trigger updateVerifyFlags on accept verification by received code -> Add inputCode, resendVerification Dialog on AppStartUp
     * 3. Poll for verification updates while local data is not verified, work with verification timeouts?
     */
    override fun updateIdentity(identity: Identity) {
        UpdateIdentity.fromIdentity(identity, UpdateAction.CREATE).let {
            indexServer.updateIdentity(it).name
        }
    }

    override fun updateIdentityPhone(identity: Identity, oldPhone: String) {
        createFieldUpdate(identity, FieldType.PHONE, oldPhone).let {
            indexServer.updateIdentity(it).name
        }
    }

    override fun updateIdentityEmail(identity: Identity, oldEmail: String) {
        createFieldUpdate(identity, FieldType.EMAIL, oldEmail).let {
            indexServer.updateIdentity(it).name
        }
    }

    private fun createFieldUpdate(identity: Identity, fieldType: FieldType, oldValue: String): UpdateIdentity {
        val fields = mutableListOf<UpdateField>(UpdateField(UpdateAction.DELETE, fieldType, oldValue))
        if (!identity.email.isNullOrBlank()) {
            fields += UpdateField(UpdateAction.CREATE, FieldType.EMAIL, identity.email)
        }
        if (!identity.phone.isNullOrBlank()) {
            fields += UpdateField(UpdateAction.CREATE, FieldType.PHONE, identity.phone)
        }
        return UpdateIdentity(
            keyPair = identity.primaryKeyPair,
            dropURL = identity.helloDropUrl,
            alias = identity.alias,
            fields = fields
        )
    }

    /**
     * TODO current way to see is verified. Just understatement implementation...
     * TODO WIP
     */
    override fun updateIdentityVerifications() {
        val identities = identityRepository.findAll()
        identities.identities.forEach { identity ->
            val phoneNumberVerified: Boolean =
                indexServer.searchForPhone(identity.phone).any {
                    it.publicKey.equals(identity.ecPublicKey)
                }
            val emailVerified: Boolean =
                indexServer.searchForMail(identity.email).any {
                    it.publicKey.equals(identity.ecPublicKey)
                }
            //TODO Update new fields
        }
    }

    override fun deleteIdentity(identity: Identity) {
        UpdateIdentity.fromIdentity(identity, UpdateAction.DELETE).let {
            indexServer.updateIdentity(it)
        }
    }

    /**
     * Search the index for the external contacts and updates the local contact repository.
     * Returns a list of new contacts.
     *
     * TODO With my last android researches my phone would requires 265 HttpRequests (265 values, 237 contacts)
     * We need an interface to search for multiple values with "OR"
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

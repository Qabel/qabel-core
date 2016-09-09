package de.qabel.core.index

import de.qabel.core.config.Identity

interface IndexInteractor {

    fun updateIdentity(identity: Identity)
    fun deleteIdentity(identity: Identity)

    /**
     * Replaces oldPhone with current [Identity]s phone
     */
    fun updateIdentityPhone(identity: Identity, oldPhone: String)

    /**
     * Replaces oldEmail with current [Identity]s email
     */
    fun updateIdentityEmail(identity: Identity, oldEmail: String)

    /**
     * Updates the emailStatus and phoneStatus for all identities
     */
    fun updateIdentityVerifications()

    /**
     * Sends verificationCode to index and updates local identities.
     */
    fun confirmVerification(code: String)

    /**
     * Matches external contacts with IndexServer and
     * use results to update local contact repository.
     *
     * Returns a list of [IndexSyncResult] with created and updated contacts.
     *
     */
    fun syncContacts(externalContactsAccessor: ExternalContactsAccessor): List<IndexSyncResult>

}

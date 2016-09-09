package de.qabel.core.index

import de.qabel.core.config.Identity

interface IndexInteractor {

    /**
     * Syncs the current [Identity.email] and [Identity.email]
     * with the [IndexServer]. Updates [Identity.emailStatus] and [Identity.phoneStatus].
     */
    fun updateIdentity(identity: Identity)

    /**
     * Deletes the current [Identity.email] and [Identity.email] from [IndexServer].
     */
    fun deleteIdentity(identity: Identity)

    /**
     * Replaces oldPhone with current [Identity.phone]
     */
    fun updateIdentityPhone(identity: Identity, oldPhone: String)

    /**
     * Replaces oldEmail with current [Identity.email]
     */
    fun updateIdentityEmail(identity: Identity, oldEmail: String)

    /**
     * Updates [Identity.emailStatus] and [Identity.phoneStatus] for all identities
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

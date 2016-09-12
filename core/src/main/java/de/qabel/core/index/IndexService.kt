package de.qabel.core.index

import de.qabel.core.config.Identity
import de.qabel.core.index.server.ExternalContactsAccessor

interface IndexService {

    /**
     * Syncs the current [Identity.email] and [Identity.email]
     * with the [IndexServer].
     * Updates [Identity.emailStatus] and [Identity.phoneStatus].
     */
    fun updateIdentity(identity: Identity, oldIdentity : Identity? = null)

    /**
     * Deletes the current [Identity.email] and [Identity.email] from [IndexServer].
     */
    fun deleteIdentity(identity: Identity)

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

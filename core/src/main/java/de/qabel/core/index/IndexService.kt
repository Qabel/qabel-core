package de.qabel.core.index

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.index.server.ExternalContactsAccessor

interface IndexService {

    /**
     * Syncs the current [Identity.email] and [Identity.email]
     * with the [IndexServer].
     * Updates [Identity.emailStatus] and [Identity.phoneStatus].
     */
    fun updateIdentity(identity: Identity, oldIdentity: Identity? = null)

    /**
     * Syncs all [Identity]s with valid fields to Index.
     */
    fun updateIdentities()

    /**
     * Deletes the current [Identity.email] and [Identity.email] from [IndexServer].
     */
    fun removeIdentity(identity: Identity)

    /**
     * Removes all [Identity]s from index.
     */
    fun removeIdentities()

    /**
     * Updates [Identity.emailStatus] and [Identity.phoneStatus] for all identities
     */
    fun updateIdentityVerifications()

    /**
     * Sends verificationCode to index and updates local identities.
     */
    fun confirmVerification(code: String)

    /**
     * Search the IndexServer for [email] and [phone].
     * @Returns a list of unique [Contact]s with email and phone set by search values they found for.
     */
    fun searchContacts(email: String, phone: String): List<Contact>

    /**
     * Matches external contacts with IndexServer and
     * use results to update local contact repository.
     *
     * Returns a list of [IndexSyncResult] with created and updated contacts.
     *
     */
    fun syncContacts(externalContactsAccessor: ExternalContactsAccessor): List<IndexSyncResult>

}

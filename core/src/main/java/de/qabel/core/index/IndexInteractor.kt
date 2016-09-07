package de.qabel.core.index

import de.qabel.core.config.Identity

interface IndexInteractor {

    fun updateIdentity(identity: Identity)
    fun updateIdentityPhone(identity : Identity, oldPhone : String)
    fun updateIdentityEmail(identity: Identity, oldEmail : String)
    fun updateIdentityVerifications()
    fun deleteIdentity(identity: Identity)

    /**
     * Matches external contacts with IndexServer and
     * use results to update local contact repository.
     *
     * Returns a list of [IndexSyncResult] with created and updated contacts.
     *
     */
    fun syncContacts(externalContactsAccessor: ExternalContactsAccessor): List<IndexSyncResult>

}

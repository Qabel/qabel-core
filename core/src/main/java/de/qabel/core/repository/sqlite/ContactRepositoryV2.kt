package de.qabel.core.repository.sqlite

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.EntityManager
import de.qabel.core.repository.framework.BaseRepositoryImpl
import de.qabel.core.repository.sqlite.schemas.ContactDB


class ContactRepositoryV2(db: ClientDatabase, em: EntityManager, dropUrlRepository: DropUrlRepository) :
    BaseRepositoryImpl<Contact>(ContactDB(dropUrlRepository), db, em), ContactRepository {

    override fun find(identity: Identity): Contacts {
        throw UnsupportedOperationException()
    }

    override fun save(contact: Contact, identity: Identity) {
        throw UnsupportedOperationException()
    }

    override fun delete(contact: Contact, identity: Identity) {
        throw UnsupportedOperationException()
    }

    override fun findByKeyId(identity: Identity, keyId: String): Contact {
        throw UnsupportedOperationException()
    }

    override fun findByKeyId(keyId: String): Contact {
        throw UnsupportedOperationException()
    }

    override fun exists(contact: Contact): Boolean {
        throw UnsupportedOperationException()
    }

    override fun findContactWithIdentities(key: String): Pair<Contact, List<Identity>> {
        throw UnsupportedOperationException()
    }

    override fun findWithIdentities(searchString: String): Collection<Pair<Contact, List<Identity>>> {
        throw UnsupportedOperationException()
    }

    override fun find(id: Int): Contact {
        throw UnsupportedOperationException()
    }

}

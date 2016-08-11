package de.qabel.core.config

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contacts
 */

class Contacts(val identity: Identity) : EntityMap<Contact>() {

    /**
     * Returns unmodifiable set of contained contacts

     * @return Set
     */
    val contacts: Set<Contact>
        get() = entities

    override fun hashCode(): Int {
        return identity.hashCode()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj == null) {
            return false
        }
        if (obj !is Contacts) {
            return false
        }
        if (obj.identity != identity) {
            return false
        }
        return super.equals(obj)
    }

    companion object {
        private val serialVersionUID = -6765883283398035654L
    }
}

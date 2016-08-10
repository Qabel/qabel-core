package de.qabel.core.config

import com.google.gson.JsonParseException

import java.io.IOException
import java.util.HashSet

class SyncedSettings {
    var identities: Identities? = null
    var accounts: Accounts? = null
    private val contacts: MutableSet<Contacts>?
    var dropServers: DropServers? = null

    init {
        accounts = Accounts()
        contacts = HashSet<Contacts>()
        dropServers = DropServers()
        identities = Identities()
    }

    fun getContacts(): Set<Contacts> {
        return contacts
    }

    fun setContacts(value: Contacts) {
        for (c in contacts!!.toTypedArray()) {
            if (c.identity === value.identity) {
                contacts.remove(c)
                break
            }
        }
        contacts.add(value)
    }


    /**
     * Serializes this class to a Json String

     * @return Json String
     */
    @Throws(IOException::class)
    fun toJson(): String {
        val adapter = SyncedSettingsTypeAdapter()
        return adapter.toJson(this)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (accounts == null) 0 else accounts!!.hashCode()
        result = prime * result + if (contacts == null) 0 else contacts.hashCode()
        result = prime * result + if (dropServers == null) 0 else dropServers!!.hashCode()
        result = prime * result + if (identities == null) 0 else identities!!.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as SyncedSettings?
        if (accounts == null) {
            if (other!!.accounts != null) {
                return false
            }
        } else if (accounts != other!!.accounts) {
            return false
        }
        if (contacts == null) {
            if (other.contacts != null) {
                return false
            }
        } else if (contacts != other.contacts) {
            return false
        }
        if (dropServers == null) {
            if (other.dropServers != null) {
                return false
            }
        } else if (dropServers != other.dropServers) {
            return false
        }
        if (identities == null) {
            if (other.identities != null) {
                return false
            }
        } else if (identities != other.identities) {
            return false
        }
        return true
    }

    companion object {

        /**
         * Deserializes a Json String

         * @param json Json to deserialize
         * *
         * @return SyncedSettings
         */
        @Throws(IOException::class, JsonParseException::class)
        fun fromJson(json: String): SyncedSettings {
            val adapter = SyncedSettingsTypeAdapter()
            return adapter.fromJson(json)
        }
    }

}
/**
 * Creates an instance of SyncedSettings
 */

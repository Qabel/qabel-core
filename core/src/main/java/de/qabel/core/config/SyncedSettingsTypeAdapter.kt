package de.qabel.core.config

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

import java.io.IOException
import java.util.HashSet

class SyncedSettingsTypeAdapter : TypeAdapter<SyncedSettings>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: SyncedSettings) {
        out.beginObject()

        // Accounts
        out.name("accounts")
        val accountsAdapter = AccountsTypeAdapter()
        accountsAdapter.write(out, value.accounts)

        // Identities
        out.name("identities")
        val identitiesAdapter = IdentitiesTypeAdapter()
        identitiesAdapter.write(out, value.identities)

        // Contacts
        out.name("contacts")
        val contactsAdapter = ContactsTypeAdapter(value.identities)
        out.beginArray()
        for (contacts in value.contacts) {
            contactsAdapter.write(out, contacts)
        }
        out.endArray()

        // DropServers
        out.name("drop_servers")
        val dropServersAdapter = DropServersTypeAdapter()
        dropServersAdapter.write(out, value.dropServers)

        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): SyncedSettings? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        val syncedSettings: SyncedSettings
        var accounts: Accounts? = null
        val contacts = HashSet<Contacts>()
        var identities: Identities? = null
        var dropServers: DropServers? = null

        `in`.beginObject()
        while (`in`.hasNext()) {
            when (`in`.nextName()) {
                "accounts" -> {
                    val accountsAdapter = AccountsTypeAdapter()
                    accounts = accountsAdapter.read(`in`)
                }
                "contacts" -> {
                    val contactsAdapter = ContactsTypeAdapter(identities)
                    `in`.beginArray()
                    while (`in`.hasNext()) {
                        val read = contactsAdapter.read(`in`)
                        if (read != null) {
                            contacts.add(read)
                        }
                    }
                    `in`.endArray()
                }
                "identities" -> {
                    val identitiesAdapter = IdentitiesTypeAdapter()
                    identities = identitiesAdapter.read(`in`)
                }
                "drop_servers" -> {
                    val dropServersAdapter = DropServersTypeAdapter()
                    dropServers = dropServersAdapter.read(`in`)
                }
            }
        }
        `in`.endObject()

        if (accounts == null || identities == null || dropServers == null) {
            return null
        }

        syncedSettings = SyncedSettings()
        syncedSettings.accounts = accounts
        for (c in contacts) {
            syncedSettings.setContacts(c)
        }
        syncedSettings.identities = identities
        syncedSettings.dropServers = dropServers

        return syncedSettings
    }
}

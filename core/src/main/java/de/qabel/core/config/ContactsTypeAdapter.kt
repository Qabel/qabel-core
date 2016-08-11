package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

import java.io.IOException

class ContactsTypeAdapter(private val identities: Identities) : TypeAdapter<Contacts>() {

    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Contacts) {
        out.beginObject()
        out.name(IDENTITY_NAME)
        out.value(value.identity.keyIdentifier)

        out.name(CONTACTS_NAME)
        out.beginArray()
        val gson = Gson()
        val set = value.contacts
        val adapter = gson.getAdapter(Contact::class.java)
        for (contact in set) {
            adapter.write(out, contact)
        }
        out.endArray()
        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(`in`: JsonReader): Contacts? {
        if (`in`.peek() == JsonToken.NULL) {
            `in`.nextNull()
            return null
        }

        `in`.beginObject()
        expectName(IDENTITY_NAME, `in`.nextName())

        val gson = Gson()
        val contacts = Contacts(identities.getByKeyIdentifier(`in`.nextString()))
        val adapter = gson.getAdapter(Contact::class.java)
        var contact: Contact

        expectName(CONTACTS_NAME, `in`.nextName())
        `in`.beginArray()
        while (`in`.hasNext()) {
            contact = adapter.read(`in`)
            contacts.put(contact)
        }
        `in`.endArray()
        `in`.endObject()

        return contacts
    }

    private fun expectName(expectedName: String, next: String) {
        if (next != expectedName) {
            throw IllegalArgumentException("wrong format, expecting key '$expectedName' but found '$next'")
        }
    }

    companion object {
        val IDENTITY_NAME = "owner"
        val CONTACTS_NAME = "contacts"
    }
}

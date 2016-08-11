package de.qabel.core.config

import de.qabel.core.contacts.ContactExchangeFormats
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.exceptions.QblInvalidFormatException
import org.json.JSONException

import java.net.URISyntaxException
import java.util.Collections

object ContactExportImport {

    private val exchangeFormats = ContactExchangeFormats()

    @Throws(JSONException::class)
    fun exportContacts(contacts: Contacts): String {
        return exchangeFormats.exportToContactsJSON(contacts.contacts)
    }

    fun exportContact(contact: Contact): String {
        return exchangeFormats.exportToContactsJSON(setOf(contact))
    }

    /**
     * Exports the [Contact] information as a JSON string from an [Identity]

     * @param identity [Identity] to export [Contact] information from
     * *
     * @return [Contact] information as JSON string
     */
    fun exportIdentityAsContact(identity: Identity): String {
        return exchangeFormats.exportToContactsJSON(identity)
    }

    /**
     * Parse a [Contact] from a [Contact] JSON string

     * @param json [Contact] JSON string
     * *
     * @return [Contact] parsed from JSON string
     */
    @Throws(JSONException::class, URISyntaxException::class, QblDropInvalidURL::class)
    fun parseContactForIdentity(json: String): Contact {
        try {
            return exchangeFormats.importFromContactsJSON(json)[0]
        } catch (e: IndexOutOfBoundsException) {
            throw JSONException(e)
        } catch (e: QblInvalidFormatException) {
            throw JSONException(e)
        }

    }

    /**
     * Parse [Contacts] from a [Contacts] JSON string

     * @param identity [Identity] for setting the owner of the [Contact]s
     * *
     * @param json     [Contacts] JSON string
     * *
     * @return [Contacts] parsed from JSON string
     */
    @Throws(JSONException::class, URISyntaxException::class, QblDropInvalidURL::class)
    fun parseContactsForIdentity(identity: Identity, json: String): Contacts {
        val contacts = Contacts(identity)
        try {

            val contactList = exchangeFormats.importFromContactsJSON(json)
            for (contact in contactList) {
                contacts.put(contact)
            }
            return contacts
        } catch (e: QblInvalidFormatException) {
            throw JSONException(e)
        }

    }
}

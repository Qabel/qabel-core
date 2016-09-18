package de.qabel.core.contacts

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.exceptions.QblInvalidFormatException
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.spongycastle.util.encoders.Hex
import java.net.URISyntaxException
import java.util.*

class ContactExchangeFormats {

    companion object {
        private val CONTACT_STRING_PREFIX = "QABELCONTACT";
        private val CONTACT_STRING_SEPARATOR = "\n";
        private val CONTACT_STRING_LENGTH = 4;

        private val KEY_ALIAS = "alias"
        private val KEY_EMAIL = "email"
        private val KEY_PHONE = "phone"
        private val KEY_PUBLIC_KEY = "public_key"
        private val KEY_DROP_URLS = "drop_urls"
        private val KEY_CONTACTS = "contacts"
    }

    fun exportToContactsJSON(identity: Identity): String {
        return exportToContactsJSON(setOf(identity.toContact()));
    }

    @Throws(QblInvalidFormatException::class)
    fun importFromContactsJSON(json: String): List<Contact> {
        try {
            val data = JSONObject(json);
            if (data.has(KEY_CONTACTS)) {
                val contactsJSON = data.getJSONArray(KEY_CONTACTS);
                val resultList = ArrayList<Contact>(contactsJSON.length());
                (0 until contactsJSON.length()).map {
                    resultList.add(parseContactJson(contactsJSON.getJSONObject(it)));
                };
                return resultList;
            } else {
                return listOf(parseContactJson(data));
            }
        } catch(ex: JSONException) {
            throw QblInvalidFormatException("Invalid json string!");
        }
    }

    fun exportToContactsJSON(contacts: Set<Contact>): String {
        try {
            val container = JSONObject();
            val jsonArray = JSONArray();
            contacts.forEach { contact -> jsonArray.put(createContactJson(contact)) }
            container.put(KEY_CONTACTS, jsonArray);
            return container.toString();
        } catch(ex: JSONException) {
            throw QblInvalidFormatException("Error exporting to json string!");
        }
    }

    @Throws(JSONException::class, URISyntaxException::class, QblDropInvalidURL::class)
    private fun parseContactJson(jsonObject: JSONObject): Contact {
        val dropURLs = ArrayList<DropURL>()
        val alias = jsonObject.getString(KEY_ALIAS)
        val jsonDropURLS = jsonObject.getJSONArray(KEY_DROP_URLS)
        (0 until jsonDropURLS.length()).map {
            dropURLs.add(DropURL(jsonDropURLS.getString(it)))
        }
        val keyIdentifier = jsonObject.getString(KEY_PUBLIC_KEY)

        val contact = Contact(alias, dropURLs, QblECPublicKey(Hex.decode(keyIdentifier)))
        val email = jsonObject.optString(KEY_EMAIL, "")
        if (!email.isNullOrBlank()) {
            contact.email = email
        }
        val phone = jsonObject.optString(KEY_PHONE, "")
        if (!phone.isNullOrBlank()) {
            contact.phone = phone
        }
        return contact
    }

    private fun createContactJson(contact: Contact): JSONObject {
        val jsonObject = JSONObject()
        val jsonDropUrls = JSONArray()
        jsonObject.put(KEY_ALIAS, contact.alias)
        if (!contact.email.isNullOrBlank()) {
            jsonObject.put(KEY_EMAIL, contact.email)
        }
        if (!contact.phone.isNullOrBlank()) {
            jsonObject.put(KEY_PHONE, contact.phone)
        }
        jsonObject.put(KEY_PUBLIC_KEY, contact.keyIdentifier)
        for (dropURL in contact.dropUrls) {
            jsonDropUrls.put(dropURL)
        }
        jsonObject.put(KEY_DROP_URLS, jsonDropUrls)

        return jsonObject
    }

    fun exportToContactString(contact: Contact): String {
        return CONTACT_STRING_PREFIX + CONTACT_STRING_SEPARATOR +
            contact.alias + CONTACT_STRING_SEPARATOR +
            contact.dropUrls.joinToString { drop -> drop.toString() } + CONTACT_STRING_SEPARATOR +
            contact.keyIdentifier;
    }

    @Throws(QblInvalidFormatException::class)
    fun importFromContactString(contactString: String): Contact {
        val parts = contactString.split(CONTACT_STRING_SEPARATOR);
        if (parts.size != CONTACT_STRING_LENGTH ||
            !parts.first().startsWith(CONTACT_STRING_PREFIX)) {
            throw QblInvalidFormatException("Invalid contact-string format");
        }

        val (ignoredPrefix, alias, dropUrlString, keyString) = parts
        try {
            return Contact(
                alias,
                dropUrlString.split(", ").map { url -> DropURL(url) },
                QblECPublicKey(Hex.decode(keyString)))
        } catch(ex: URISyntaxException) {
            throw QblInvalidFormatException("Invalid DropURL in contact-string");
        } catch(ex: QblDropInvalidURL) {
            throw QblInvalidFormatException("Invalid DropURL contact-string format");
        }
    }

}

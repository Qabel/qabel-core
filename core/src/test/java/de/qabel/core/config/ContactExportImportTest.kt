package de.qabel.core.config

import de.qabel.core.config.Contact
import de.qabel.core.config.ContactExportImport
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import org.json.JSONException
import org.junit.Before
import org.junit.Test

import java.net.URISyntaxException
import java.util.ArrayList

import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is.`is`

class ContactExportImportTest {

    lateinit internal var identity: Identity
    lateinit internal var contact1: Contact
    lateinit internal var contact2: Contact
    lateinit internal var contacts: Contacts
    lateinit internal var qblECKeyPair: QblECKeyPair

    @Before
    @Throws(Exception::class)
    fun setUp() {

        qblECKeyPair = QblECKeyPair()
        val dropURLs = ArrayList<DropURL>()
        dropURLs.add(DropURL(DROP_URL_1))
        dropURLs.add(DropURL(DROP_URL_2))
        identity = Identity("Identity", dropURLs, qblECKeyPair)
        identity.email = "test@example.com"
        identity.phone = "+491111111"

        val contact1KeyPair = QblECKeyPair()
        contact1 = Contact("Contact1", dropURLs, contact1KeyPair.pub)

        val contact2KeyPair = QblECKeyPair()
        contact2 = Contact("Contact2", dropURLs, contact2KeyPair.pub)

        contacts = Contacts(identity)
        contacts.put(contact1)
        contacts.put(contact2)
    }

    @Test
    @Throws(QblDropInvalidURL::class, JSONException::class, URISyntaxException::class)
    fun testExportImportContact() {

        val contactJSON = ContactExportImport.exportContact(contact1)
        val importedContact1 = ContactExportImport.parseContactForIdentity(contactJSON)
        contactEquals(contact1, importedContact1)
    }

    @Test
    @Throws(QblDropInvalidURL::class, JSONException::class, URISyntaxException::class)
    fun testExportImportContactWithOptionals() {

        contact1.email = "test@example.com"
        contact1.phone = "+491111111"
        val contactJSON = ContactExportImport.exportContact(contact1)
        val importedContact1 = ContactExportImport.parseContactForIdentity(contactJSON)
        contactEquals(contact1, importedContact1)
    }

    @Test
    @Throws(JSONException::class, URISyntaxException::class, QblDropInvalidURL::class)
    fun testExportImportContacts() {

        val contactsJSON = ContactExportImport.exportContacts(contacts)

        val importedContacts = ContactExportImport.parseContactsForIdentity(identity, contactsJSON)

        assertThat(importedContacts.contacts.size, `is`(2))
        val importedContact1 = importedContacts.getByKeyIdentifier(contact1.keyIdentifier)
        val importedContact2 = importedContacts.getByKeyIdentifier(contact2.keyIdentifier)

        contactEquals(contact1, importedContact1)
        contactEquals(contact2, importedContact2)
    }

    private fun contactEquals(contact1: Contact, contact2: Contact) {

        assertThat(contact1.alias, `is`(contact2.alias))
        assertThat(contact1.dropUrls, `is`(contact2.dropUrls))
        assertThat(contact1.ecPublicKey.readableKeyIdentifier, `is`(contact2.ecPublicKey.readableKeyIdentifier))
        assertThat<String>(contact1.phone, `is`<String>(contact2.phone))
        assertThat<String>(contact1.email, `is`<String>(contact2.email))
    }

    @Test
    @Throws(URISyntaxException::class, QblDropInvalidURL::class, JSONException::class)
    fun testImportExportedContactFromIdentity() {

        val json = ContactExportImport.exportIdentityAsContact(identity)
        // Normally a contact wouldn't be imported for the belonging identity, but it doesn't matter for the test.
        val contact = ContactExportImport.parseContactForIdentity(json)

        assertThat(identity.alias, `is`(contact.alias))
        assertThat(identity.dropUrls, `is`(contact.dropUrls))
        assertThat(identity.ecPublicKey.readableKeyIdentifier, `is`(contact.ecPublicKey.readableKeyIdentifier))
        assertThat<String>(identity.phone, `is`<String>(contact.phone))
        assertThat<String>(identity.email, `is`<String>(contact.email))
    }

    companion object {

        private val DROP_URL_1 = "http://localhost:6000/1234567890123456789012345678901234567891234"
        private val DROP_URL_2 = "http://localhost:6000/0000000000000000000000000000000000000000000"
    }
}

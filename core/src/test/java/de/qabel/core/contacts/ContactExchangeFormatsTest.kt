package de.qabel.core.contacts

import de.qabel.core.config.Contact
import de.qabel.core.config.Contacts
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.junit.Before
import org.junit.Test

class ContactExchangeFormatsTest {

    companion object {
        private val DROP_URL_1 = "http://localhost:6000/1234567890123456789012345678901234567891234"
        private val DROP_URL_2 = "http://localhost:6000/0000000000000000000000000000000000000000000"
    }

    internal lateinit var identity: Identity
    internal lateinit var contact1: Contact
    internal lateinit var contact2: Contact
    internal lateinit var contacts: Contacts
    internal lateinit var qblECKeyPair: QblECKeyPair

    internal lateinit var contactExchangeFormats: ContactExchangeFormats

    @Before
    @Throws(Exception::class)
    fun setUp() {
        contactExchangeFormats = ContactExchangeFormats();

        val dropURLs = listOf(DropURL(DROP_URL_1), DropURL(DROP_URL_2));

        qblECKeyPair = QblECKeyPair()
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

    private fun assertContactEquals(contact1: Contact, contact2: Contact) {
        assertThat("Alias doesnt match", contact1.alias.equals(contact2.alias))
        assertThat("DropUrls dont match", contact1.dropUrls.equals(contact2.dropUrls))
        assertThat("PubKey doesnt match", contact1.ecPublicKey.readableKeyIdentifier.
            equals(contact2.ecPublicKey.readableKeyIdentifier))
        assertThat("Phone doesnt match", contact1.phone == contact2.phone)
        assertThat("Email doesnt match", contact1.email == contact2.email)
    }

    @Test
    fun testImportExportContactJSON() {
        val contactJSON = contactExchangeFormats.exportToContactsJSON(setOf(contact1));
        val parsedContacts = contactExchangeFormats.importFromContactsJSON(contactJSON);
        assertThat(1, Is.`is`(parsedContacts.size));
        assertContactEquals(contact1, parsedContacts.first());
    }

    @Test
    fun testImportExportContactsJSON() {
        val contactsJSON = contactExchangeFormats.exportToContactsJSON(contacts.contacts);
        val parsedContacts = contactExchangeFormats.importFromContactsJSON(contactsJSON);
        assertThat(contacts.contacts.size, Is.`is`(parsedContacts.size));
        for (contact in parsedContacts) {
            assertContactEquals(contacts.getByKeyIdentifier(contact.keyIdentifier),
                contact);
        }
    }

    @Test
    fun testImportExportContactString() {
        val contactString = contactExchangeFormats.exportToContactString(contact1);
        val parsedContact = contactExchangeFormats.importFromContactString(contactString);
        assertContactEquals(contact1, parsedContact);
    }

}


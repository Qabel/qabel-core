package de.qabel.core.config;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ContactExportImportTest {

	private static final String DROP_URL_1 = "http://localhost:6000/1234567890123456789012345678901234567891234";
	private static final String DROP_URL_2 = "http://localhost:6000/0000000000000000000000000000000000000000000";

	Identity identity;
	Contact contact1;
	Contact contact2;
	Contacts contacts;
	QblECKeyPair qblECKeyPair;

	@Before
	public void setUp() throws Exception {

		qblECKeyPair = new QblECKeyPair();
		Collection<DropURL> dropURLs = new ArrayList<>();
		dropURLs.add(new DropURL(DROP_URL_1));
		dropURLs.add(new DropURL(DROP_URL_2));
		identity = new Identity("Identity", dropURLs, qblECKeyPair);
		identity.setEmail("test@example.com");
		identity.setPhone("+491111111");

		QblECKeyPair contact1KeyPair = new QblECKeyPair();
		contact1 = new Contact("Contact1", dropURLs, contact1KeyPair.getPub());

		QblECKeyPair contact2KeyPair = new QblECKeyPair();
		contact2 = new Contact("Contact2", dropURLs, contact2KeyPair.getPub());

		contacts = new Contacts(identity);
		contacts.put(contact1);
		contacts.put(contact2);
	}

	@Test
	public void testExportImportContact() throws QblDropInvalidURL, JSONException, URISyntaxException {

		String contactJSON = ContactExportImport.exportContact(contact1);
		Contact importedContact1 = ContactExportImport.parseContactForIdentity(contactJSON);
		contactEquals(contact1, importedContact1);
	}

	@Test
	public void testExportImportContactWithOptionals() throws QblDropInvalidURL, JSONException, URISyntaxException {

		contact1.setEmail("test@example.com");
		contact1.setPhone("+491111111");
		String contactJSON = ContactExportImport.exportContact(contact1);
		Contact importedContact1 = ContactExportImport.parseContactForIdentity(contactJSON);
		contactEquals(contact1, importedContact1);
	}

	@Test
	public void testExportImportContacts() throws JSONException, URISyntaxException, QblDropInvalidURL {

		String contactsJSON = ContactExportImport.exportContacts(contacts);

		Contacts importedContacts = ContactExportImport.parseContactsForIdentity(identity, contactsJSON);

		assertThat(importedContacts.getContacts().size(), is(2));
		Contact importedContact1 = importedContacts.getByKeyIdentifier(contact1.getKeyIdentifier());
		Contact importedContact2 = importedContacts.getByKeyIdentifier(contact2.getKeyIdentifier());

		contactEquals(contact1, importedContact1);
		contactEquals(contact2, importedContact2);
	}

	private void contactEquals(Contact contact1, Contact contact2) {

		assertThat(contact1.getAlias(), is(contact2.getAlias()));
		assertThat(contact1.getDropUrls(), is(contact2.getDropUrls()));
		assertThat(contact1.getEcPublicKey().getReadableKeyIdentifier(), is(contact2.getEcPublicKey().getReadableKeyIdentifier()));
		assertThat(contact1.getPhone(), is(contact2.getPhone()));
		assertThat(contact1.getEmail(), is(contact2.getEmail()));
	}

	@Test
	public void testImportExportedContactFromIdentity() throws URISyntaxException, QblDropInvalidURL, JSONException {

		String json = ContactExportImport.exportIdentityAsContact(identity);
		// Normally a contact wouldn't be imported for the belonging identity, but it doesn't matter for the test.
		Contact contact = ContactExportImport.parseContactForIdentity(json);

		assertThat(identity.getAlias(), is(contact.getAlias()));
		assertThat(identity.getDropUrls(), is(contact.getDropUrls()));
		assertThat(identity.getEcPublicKey().getReadableKeyIdentifier(), is(contact.getEcPublicKey().getReadableKeyIdentifier()));
		assertThat(identity.getPhone(), is(contact.getPhone()));
		assertThat(identity.getEmail(), is(contact.getEmail()));
	}
}

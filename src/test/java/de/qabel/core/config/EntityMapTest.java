package de.qabel.core.config;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.junit.Before;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EntityMapTest {

	private Collection<DropURL> dropURLs;
	private QblECKeyPair qblECKeyPair = new QblECKeyPair();
	private Identity identity;
	private Contacts contacts;
	private Contact contact;

	@Before
	public void setUp() throws Exception {
		final boolean[] uptoDate = {false};
		dropURLs = new ArrayList<>();
		dropURLs.add(new DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"));
		identity = new Identity("Identity", dropURLs, qblECKeyPair);
		contacts = new Contacts(identity);
		contact = new Contact("Contact", identity.getDropUrls(), identity.getEcPublicKey());
		contacts.put(contact);
	}

	@Test
	public void testPutNotifyObserver() throws Exception {
		final boolean[] uptoDate = {false};

		EntityObserver observer = new EntityObserver() {
			@Override
			public void update() {
				uptoDate[0] = true;
			}
		};

		contacts.addObserver(observer);
		contacts.put(contact);
		assertTrue(uptoDate[0]);
	}

	@Test
	public void testRemoveNotifyObserver() throws Exception {
		final boolean[] uptoDate = {false};

		EntityObserver observer = new EntityObserver() {
			@Override
			public void update() {
				uptoDate[0] = true;
			}
		};

		contacts.addObserver(observer);
		contacts.removeObserver(observer);
		contacts.put(contact);
		assertFalse(uptoDate[0]);
	}

	@Test
	public void testRemoveStringNotifyObserver() throws Exception {

		final boolean[] uptoDate = {false};

		EntityObserver observer = new EntityObserver() {

			@Override
			public void update() {
				uptoDate[0] = true;
			}
		};

		contacts.addObserver(observer);
		contacts.remove(contact.getKeyIdentifier());
		assertTrue(uptoDate[0]);
	}

	@Test
	public void testRemoveEntityNotifyObserver() throws Exception {
		final boolean[] uptoDate = {false};

		EntityObserver observer = new EntityObserver() {

			@Override
			public void update() {
				uptoDate[0] = true;
			}
		};

		contacts.addObserver(observer);
		contacts.remove(contact);
		assertTrue(uptoDate[0]);
	}




}

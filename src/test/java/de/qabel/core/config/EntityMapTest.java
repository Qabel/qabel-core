package de.qabel.core.config;

import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;

import static org.junit.Assert.assertTrue;

public class EntityMapTest {
	@Test
	public void testPutNotifyObserver() throws Exception {
		QblECKeyPair qblECKeyPair = new QblECKeyPair();
		Collection<DropURL> dropURLs = new ArrayList<>();
		dropURLs.add(new DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"));
		Identity identity = new Identity("Identity", dropURLs, qblECKeyPair);

		Contacts contacts = new Contacts(identity);
		Contact contact = new Contact("Contact", identity.getDropUrls(), identity.getEcPublicKey());
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
}

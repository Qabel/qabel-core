package de.qabel.core.config;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.crypto.QblECKeyPair;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;

public class ConfigSerializationTest {

    @Test
    public void syncedSettingsTest() throws QblDropInvalidURL, IOException, URISyntaxException {
        SyncedSettings syncedSettings = new SyncedSettings();

        //generate and put an "accounts" entry
        Account account = new Account("provider", "user", "auth");

        syncedSettings.getAccounts().put(account);

        //generate and put an "drop_servers" entry
        DropServer dropServer = new DropServer(new URI("https://drop.qabel.de/0123456789012345678901234567890123456789123"), "auth", true);
        syncedSettings.getDropServers().put(dropServer);

        //generate "identities" array
        syncedSettings.setIdentities(new Identities());
        //generate and put an "identities" entry
        QblECKeyPair key;
        Collection<DropURL> drops;
        Identity identity;

        key = new QblECKeyPair();
        drops = new ArrayList<>();
        drops.add(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012c"));
        identity = new Identity("alias", drops, key);
        syncedSettings.getIdentities().put(identity);

        //add contacts entry
        Contacts contacts = new Contacts(identity);
        contacts.put(new Contact("alias", null, key.getPub()));
        syncedSettings.getContacts().add(contacts);

        SyncedSettings deserializedSyncedSettings = SyncedSettings.fromJson(syncedSettings.toJson());
        assertEquals(syncedSettings.toJson(), deserializedSyncedSettings.toJson());

        assertEquals(deserializedSyncedSettings, syncedSettings);
    }

    @Test
    public void localSettingsTest() throws IOException {
        LocalSettings localSettings = new LocalSettings(10, new Date(System.currentTimeMillis()));

        LocalSettings deserializedLocalSettings = LocalSettings.fromJson(localSettings.toJson());

        assertEquals(deserializedLocalSettings, localSettings);
    }

    @Test
    public void contactTest() {
        Contact contact;
        Contact deserializedContact;
        try {

            Identity i = new Identity("alias", new ArrayList<DropURL>(), new QblECKeyPair());
            i.addDrop(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012c"));
            QblECKeyPair ecKeyPair = new QblECKeyPair();
            contact = new Contact("", null, ecKeyPair.getPub());
            contact.addDrop(new DropURL("https://inbox.qabel.de/123456789012345678901234567890123456789012d"));
            contact.setEmail("alice@example.org");
            contact.setPhone("+49123456789012");

            GsonBuilder builder = new GsonBuilder();
            builder.registerTypeAdapter(Contact.class, new ContactTypeAdapter());
            Gson gson = builder.create();
            deserializedContact = gson.fromJson(gson.toJson(contact), Contact.class);

            assertEquals(contact, deserializedContact);
            assertEquals("alice@example.org", deserializedContact.getEmail());
            assertEquals("+49123456789012", deserializedContact.getPhone());

        } catch (QblDropInvalidURL | URISyntaxException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

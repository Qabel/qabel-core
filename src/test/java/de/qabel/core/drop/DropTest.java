package de.qabel.core.drop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.*;
import de.qabel.core.crypto.QblPrimaryKeyPair;
import org.junit.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class DropTest {
    String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";
    URL identityUrl = null;
    URL contactUrl = null;
    QblPrimaryKeyPair keypair = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    QblPrimaryKeyPair qpkp = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    QblPrimaryPublicKey qppk = qpkp.getQblPrimaryPublicKey();
    QblEncPublicKey qepk = qpkp.getQblEncPublicKey();
    QblSignPublicKey qspk = qpkp.getQblSignPublicKey();

    Identity i = new Identity("foo", identityUrl);
    Identities is = new Identities();
    Contacts contacts = new Contacts();
    Contact contact = new Contact(i);

    static class TestMessage extends ModelObject {
        public String content;

        public TestMessage() {
        }
    }

    @Test
    @Ignore
    public void sendAndForgetTest() {

        try {
            identityUrl = new URL(iUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        try {
            contactUrl = new URL(cUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        i.setPrimaryKeyPair(keypair);
        is.getIdentities().add(i);


        contact.getDropUrls().add(contactUrl);

        contact.setPrimaryPublicKey(qppk);
        contact.setEncryptionPublicKey(qepk);
        contact.setSignaturePublicKey(qspk);

        contacts.getContacts().add(contact);

        Drop d = new Drop();


        TestMessage m = new TestMessage();
        m.content = "baz";

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>();
        Date date = new Date();

        dm.setTime(date);
        dm.setSender("foo");
        dm.setData(m);
        dm.setAcknowledgeID("bar");
        dm.setVersion(1);
        dm.setModelObject(TestMessage.class);

        Assert.assertEquals(200, d.sendAndForget(dm, contacts, i));
    }

    @Test
    @Ignore
    public void retrieveTest() {

        contact.getDropUrls().add(contactUrl);

        contact.setPrimaryPublicKey(qppk);
        contact.setEncryptionPublicKey(qepk);
        contact.setSignaturePublicKey(qspk);

        contacts.getContacts().add(contact);

        try {
            contactUrl = new URL(cUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        Drop d = new Drop();


        DropMessage<ModelObject> result = d.retrieve(contactUrl, contacts);
        //We don't have the key (yet), so the Message should be null.
        Assert.assertNull(result);
    }
}

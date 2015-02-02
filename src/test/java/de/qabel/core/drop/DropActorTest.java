package de.qabel.core.drop;

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
=======
import de.qabel.ackack.event.EventActor;
import de.qabel.ackack.event.EventEmitter;
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.exceptions.QblDropPayloadSizeException;

import org.junit.*;

import java.awt.*;
import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
public class DropControllerTest {
    private static final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    private static String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";
    private Identity sender, recipient;
    private Contact senderContact, recipientContact;
    private DropController controller;
    private Identities identities;
    private Contacts contacts;
=======
public class DropActorTest {
    final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    final String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";

    final QblPrimaryKeyPair qpkpSender = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    final QblPrimaryPublicKey qppkSender = qpkpSender.getQblPrimaryPublicKey();
    final QblEncPublicKey qepkSender = qpkpSender.getQblEncPublicKey();
    final QblSignPublicKey qspkSender = qpkpSender.getQblSignPublicKey();
        
    final QblPrimaryKeyPair qpkpRecipient = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    final QblPrimaryPublicKey qppkRecipient = qpkpRecipient.getQblPrimaryPublicKey();
    final QblEncPublicKey qepkRecipient = qpkpRecipient.getQblEncPublicKey();
    final QblSignPublicKey qspkRecipient = qpkpRecipient.getQblSignPublicKey();

>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
    
    static class TestMessage extends ModelObject {
        public String content;

        public TestMessage(String content) {
        	this.content = content;
        }
    }
    
    @Before
    public void setup() throws MalformedURLException, QblDropInvalidURL, InvalidKeyException {
    	QblPrimaryKeyPair qpkpSender = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    	QblPrimaryKeyPair qpkpRecipient = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    	sender = new Identity("Bernd", null, qpkpSender);
    	sender.addDrop(new DropURL(iUrl));
    	recipient = new Identity("Bernd", null, qpkpRecipient);
    	recipient.addDrop(new DropURL(cUrl));

    	recipientContact = new Contact(sender, recipient.getDropUrls(), qpkpRecipient.getQblPrimaryPublicKey());
    	senderContact = new Contact(recipient, sender.getDropUrls(), qpkpSender.getQblPrimaryPublicKey());

    	identities = new Identities();
    	identities.add(sender);
    	identities.add(recipient);

    	contacts = new Contacts();
    	contacts.add(senderContact);
    	contacts.add(recipientContact);

        controller = new DropController();
    }

    @Test
<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
    public void sendAndForgetTest() throws MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {  
        TestMessage m = new TestMessage("baz");
=======
    public void sendAndForgetTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {  
        DropURL identityUrl = new DropURL(iUrl);
        DropURL contactUrl = new DropURL(cUrl);

        Collection<DropURL> drops = new ArrayList<DropURL>();
        drops.add(identityUrl);
        Identity i = new Identity("foo", drops, qpkpSender);
        Identities is = new Identities();
        Contact contact = new Contact(i);
        is.add(i);        

        contact.getDropUrls().add(contactUrl);

        contact.setPrimaryPublicKey(qppkRecipient);
        contact.addEncryptionPublicKey(qepkRecipient);
        contact.addSignaturePublicKey(qspkRecipient);

        DropActor d = new DropActor(EventEmitter.getDefault());
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
        HashSet<Contact> recipients = new HashSet<Contact>();
        recipients.add(recipientContact);
        Assert.assertTrue(controller.sendAndForget(dm, recipients).isSuccess());
=======
        DropMessage<TestMessage> dm = new DropMessage<TestMessage>();
        Date date = new Date();

        dm.setTime(date);
        dm.setSender("foo");
        dm.setData(m);
        dm.setAcknowledgeID("bar");
        dm.setVersion(1);
        dm.setModelObject(TestMessage.class);

        HashSet<Contact> contacts = new HashSet<Contact>();
        contacts.add(contact);
        DropActor.send(EventEmitter.getDefault(), dm, contacts);
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
        
        retrieveTest();
    }

    @Test
    public void sendAndForgetAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {
        TestMessage m = new TestMessage("baz");

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
        Assert.assertTrue(controller.sendAndForget(m, recipientContact).isSuccess());
=======
        contact.setPrimaryPublicKey(qppkRecipient);
        contact.addEncryptionPublicKey(qepkRecipient);
        contact.addSignaturePublicKey(qspkRecipient);

        DropActor d = new DropActor();

        TestMessage m = new TestMessage();
        m.content = "baz";

        HashSet<Contact> contacts = new HashSet<Contact>();
        contacts.add(contact);
        Assert.assertTrue(d.sendAndForget(m, contact).isSuccess());
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java

        retrieveAutoTest();
    }

    @Test
    public void sendTestSingle() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {    	
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
        DropResultContact result = controller.sendAndForget(dm, recipientContact);
        Assert.assertTrue(result.isSuccess());
=======
        DropActor d = new DropActor();

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

        Assert.assertTrue(d.sendAndForget(dm, contact).isSuccess());
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
        
        retrieveTest();
    }

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
    public void retrieveTest() throws MalformedURLException, QblDropInvalidURL {
        Collection<DropMessage<?>> result = controller.retrieve(
        		new DropURL(cUrl).getUrl(), contacts.getContacts());
        //We expect at least one drop message from sender
=======
    public void retrieveTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        DropURL identityUrl = new DropURL(iUrl);
        DropURL contactUrl = new DropURL(cUrl);
        
        Collection<DropURL> drops = new ArrayList<DropURL>();
        drops.add(identityUrl);
        Identity i = new Identity("foo", drops, qpkpRecipient);
        Contact contact = new Contact(i);

        contact.getDropUrls().add(contactUrl);

        contact.setPrimaryPublicKey(qppkSender);
        contact.addEncryptionPublicKey(qepkSender);
        contact.addSignaturePublicKey(qspkSender);

        Contacts contacts = new Contacts();
        contacts.add(contact);

        DropActor d = new DropActor(EventEmitter.getDefault());



        Collection<DropMessage> result = d.retrieve(contactUrl.getUrl(), contacts.getContacts());
        //We expect at least one drop message from "foo"
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<?> dm : result){
        	 Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
        }
    }

<<<<<<< HEAD:src/test/java/de/qabel/core/drop/DropControllerTest.java
    public void retrieveAutoTest() throws MalformedURLException, QblDropInvalidURL {
        Collection<DropMessage<?>> result = controller.retrieve(
        		new DropURL(cUrl).getUrl(), contacts.getContacts());
        //We expect at least one drop message from sender
=======
    public void retrieveAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        DropURL identityUrl = new DropURL(iUrl);
        DropURL contactUrl = new DropURL(cUrl);

        Collection<DropURL> drops = new ArrayList<DropURL>();
        drops.add(identityUrl);
        Identity i = new Identity("foo", drops, qpkpRecipient);
        Contact contact = new Contact(i);

        contact.getDropUrls().add(contactUrl);

        contact.setPrimaryPublicKey(qppkSender);
        contact.addEncryptionPublicKey(qepkSender);
        contact.addSignaturePublicKey(qspkSender);

        Contacts contacts = new Contacts();
        contacts.add(contact);

        DropActor d = new DropActor();

        Collection<DropMessage> result = d.retrieve(contactUrl.getUrl(), contacts.getContacts());
        //We expect at least one drop message from "foo"
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.:src/test/java/de/qabel/core/drop/DropActorTest.java
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<?> dm : result){
            Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
        }
    }
}

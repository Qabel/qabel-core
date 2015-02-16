package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.ackack.event.EventEmitter;
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

public class DropActorTest {
    private static final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    private static String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";
    private Identity sender, recipient;
    private Contact senderContact, recipientContact;
    private DropActor controller;
    private Identities identities;
    private Contacts contacts;
    private EventEmitter emitter;

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
        emitter = new EventEmitter();

        controller = new DropActor(emitter);
    }

    @Test
    public void sendAndForgetTest() throws MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {
        TestMessage m = new TestMessage("baz");
        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        HashSet<Contact> recipients = new HashSet<Contact>();
        recipients.add(recipientContact);
        DropActor.send(emitter, dm, new HashSet<>(contacts.getContacts()));

        retrieveTest();
    }

    @Test
    public void sendAndForgetAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {
        TestMessage m = new TestMessage("baz");

        DropActor.send(emitter, m, recipientContact);

        retrieveAutoTest();
    }

    @Test
    public void sendTestSingle() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL, QblDropPayloadSizeException {    	
        TestMessage m = new TestMessage("baz");

        DropMessage<TestMessage> dm = new DropMessage<TestMessage>(sender, m);

        DropActor.send(emitter, dm, recipientContact);
        retrieveTest();
    }

    public void retrieveTest() throws MalformedURLException, QblDropInvalidURL {
        Collection<DropMessage<?>> result = controller.retrieve(
        		new DropURL(cUrl).getUrl(), contacts.getContacts());
        //We expect at least one drop message from sender
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<?> dm : result){
        	 Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
        }
    }

    public void retrieveAutoTest() throws MalformedURLException, QblDropInvalidURL {
        Collection<DropMessage<?>> result = controller.retrieve(
        		new DropURL(cUrl).getUrl(), contacts.getContacts());
        //We expect at least one drop message from sender
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<?> dm : result){
            Assert.assertEquals(sender.getKeyIdentifier(), dm.getSender().getKeyIdentifier());
        }
    }
}

package de.qabel.core.drop;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.*;
import de.qabel.core.exceptions.QblDropInvalidURL;

import org.junit.*;

import java.net.MalformedURLException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

public class DropControllerTest {
    final String iUrl = "http://localhost:6000/123456789012345678901234567890123456789012c";
    final String cUrl = "http://localhost:6000/123456789012345678901234567890123456789012d";

    final QblPrimaryKeyPair qpkpSender = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    final QblPrimaryPublicKey qppkSender = qpkpSender.getQblPrimaryPublicKey();
    final QblEncPublicKey qepkSender = qpkpSender.getQblEncPublicKeys().get(0);
    final QblSignPublicKey qspkSender = qpkpSender.getQblSignPublicKeys().get(0);
        
    final QblPrimaryKeyPair qpkpRecipient = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
    final QblPrimaryPublicKey qppkRecipient = qpkpRecipient.getQblPrimaryPublicKey();
    final QblEncPublicKey qepkRecipient = qpkpRecipient.getQblEncPublicKeys().get(0);
    final QblSignPublicKey qspkRecipient = qpkpRecipient.getQblSignPublicKeys().get(0);

    private Contact senderContact;
    private HashSet<Contact> senderContacts;
    private Contacts recipientContacts;
    private DropURL contactUrl;
    private DropController dropController;
    
    static class TestMessage extends ModelObject {
        public String content;

        public TestMessage() {
        }
    }

	@Before
	public void setUp() throws MalformedURLException, QblDropInvalidURL, InvalidKeyException {
        DropURL identityUrl = new DropURL(iUrl);

        contactUrl = new DropURL(cUrl);

        Collection<DropURL> drops = new ArrayList<DropURL>();
        drops.add(identityUrl);
        
        Identity senderIdentity = new Identity("foo", drops, qpkpSender);
        Identities is = new Identities();

        senderContact = new Contact(senderIdentity);
        senderContact.getDropUrls().add(contactUrl);
        senderContact.setPrimaryPublicKey(qppkRecipient);
        senderContact.addEncryptionPublicKey(qepkRecipient);
        senderContact.addSignaturePublicKey(qspkRecipient);

        is.add(senderIdentity);        

        senderContacts = new HashSet<Contact>();
        senderContacts.add(senderContact);

        Identity recipientIdentity = new Identity("foo", drops, qpkpRecipient);

        Contact recipientContact = new Contact(recipientIdentity);
        recipientContact.getDropUrls().add(contactUrl);
        recipientContact.setPrimaryPublicKey(qppkSender);
        recipientContact.addEncryptionPublicKey(qepkSender);
        recipientContact.addSignaturePublicKey(qspkSender);

        recipientContacts = new Contacts();
        recipientContacts.add(recipientContact);

        dropController = new DropController();
		
	}

    @Test
    public void sendAndForgetTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {  

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

        Assert.assertTrue(dropController.sendAndForget(dm, senderContacts).isSuccess());
        
        retrieveTest();
    }

    @Test
    public void sendAndForgetAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        TestMessage m = new TestMessage();
        m.content = "baz";

        Assert.assertTrue(dropController.sendAndForget(m, senderContact).isSuccess());

        retrieveAutoTest();
    }

    @Test
    public void sendTestSingle() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {    	
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

        Assert.assertTrue(dropController.sendAndForget(dm, senderContact).isSuccess());
        
        retrieveTest();
    }

    @Test
    public void addingAndRemovingHeader() {
        TestMessage m = new TestMessage();
        m.content = "baz";

        DropMessage<TestMessage> dm = new DropMessage<>();
        dm.setTime(new Date());
        dm.setSender("foo");
        dm.setData(m);
        dm.setAcknowledgeID("bar");
        dm.setVersion(1);
        dm.setModelObject(TestMessage.class);

        GsonBuilder gb = new GsonBuilder();
        gb.registerTypeAdapter(DropMessage.class, new DropSerializer());
        gb.registerTypeAdapter(DropMessage.class, new DropDeserializer());
        Gson gson = gb.create();

        String message = gson.toJson(dm);
        byte[] messageBytes = message.getBytes();
        
        //Adding header
        byte[] headerAndMessage = dropController.concatHeaderAndEncryptedMessage((byte) 1, messageBytes);
        //Removing header
        byte[] messageBytesRemovedHeader = dropController.removeHeaderFromCipherMessage(headerAndMessage);
        DropMessage newMessage = gson.fromJson(new String(messageBytesRemovedHeader), DropMessage.class);

        Assert.assertEquals(messageBytes.length + 1, headerAndMessage.length);
        Assert.assertEquals(headerAndMessage[0], (byte) 1);
        Assert.assertArrayEquals(messageBytes, messageBytesRemovedHeader);

        Assert.assertEquals(dm.getTime(), newMessage.getTime());
        Assert.assertEquals(dm.getSender(), newMessage.getSender());
        Assert.assertEquals(dm.getAcknowledgeID(), newMessage.getAcknowledgeID());
        Assert.assertEquals(dm.getVersion(), newMessage.getVersion());
        Assert.assertEquals(dm.getModelObject(), newMessage.getModelObject());
    }

    public void retrieveTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {
        
        Collection<DropMessage> result = dropController.retrieve(contactUrl.getUrl(), recipientContacts.getContacts());
        //We expect at least one drop message from "foo"
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<ModelObject> dm : result){
        	 Assert.assertEquals("foo", dm.getSender());
        }
    }

    public void retrieveAutoTest() throws InvalidKeyException, MalformedURLException, QblDropInvalidURL {

        Collection<DropMessage> result = dropController.retrieve(contactUrl.getUrl(), recipientContacts.getContacts());
        //We expect at least one drop message from "foo"
        Assert.assertTrue(result.size() >= 1);
        for (DropMessage<ModelObject> dm : result){
            Assert.assertEquals("", dm.getSender());
        }
    }
}

package de.qabel.core.drop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.Identities;
import de.qabel.core.config.Identity;
import org.junit.*;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

public class DropTest {
    String iUrl = "http://localhost:6000/12345678901234567890123456789012345678901234567890";
    URL identityUrl = null;
    String cUrl = "http://localhost:6000/12345678901234567890123456789012345678901234567891";
    URL contactUrl = null;

    @Test
    public <T extends ModelObject> void sendAndForgetTest() {
        Identities is = new Identities();
        try {
            identityUrl = new URL(iUrl);
        } catch (MalformedURLException e){
            //I don't care.
        }
            Identity i = new Identity("foo", identityUrl);

        try {
            contactUrl = new URL(cUrl);
        } catch (MalformedURLException e){
            //I don't care.
        }

        Contacts contacts = new Contacts();
        Contact contact = new Contact(i);

        contact.getDropUrls().add(contactUrl);
        Drop d = new Drop();
        d.setContacts(contacts);

        class TestMessage extends ModelObject{
            public String content;

        }
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

        d.setMessage(dm);

        Assert.assertEquals(200, d.sendAndForget());



    }


}

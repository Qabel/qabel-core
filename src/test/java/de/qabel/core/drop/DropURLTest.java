package de.qabel.core.drop;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.qabel.core.config.DropServer;
import de.qabel.core.exceptions.QblDropInvalidURL;

public class DropURLTest {
    @Test
    public void validURLTest() throws MalformedURLException, QblDropInvalidURL {
    	new DropURL("http://www.foo.org/1234567890123456789012345678901234567890123");
    }

    @Rule public ExpectedException exception = ExpectedException.none();

    @Test
    public void tooShortURLTest() throws MalformedURLException, QblDropInvalidURL {
    	exception.expect(QblDropInvalidURL.class);
    	new DropURL("http://www.bar.org/not43base64chars");
    }

    @Test
    public void tooLongURLTest() throws QblDropInvalidURL, MalformedURLException {
    	exception.expect(QblDropInvalidURL.class);
    	new DropURL("http://www.foo.org/01234567890123456789012345678901234567890123");
    }

    @Test
    public void nonBase64URLTest() throws MalformedURLException, QblDropInvalidURL {
    	exception.expect(QblDropInvalidURL.class);
    	new DropURL("http://www.baz.org/2@34567890123456789012345678901234567890123");
    }
    
    @Test
    public void testGeneration() throws MalformedURLException, QblDropInvalidURL {
    	DropServer server = new DropServer();
    	server.setUrl(new URL("http://example.com"));
    	DropURL drop = new DropURL(server);
    	new DropURL(drop.toString());
    }
}

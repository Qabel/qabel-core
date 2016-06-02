package de.qabel.core.drop;

import de.qabel.core.config.DropServer;
import de.qabel.core.drop.DropURL;
import de.qabel.core.exceptions.QblDropInvalidURL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.net.URI;
import java.net.URISyntaxException;

public class DropURLTest {
    @Test
    public void validURLTest() throws URISyntaxException, QblDropInvalidURL {
        new DropURL("http://www.foo.org/1234567890123456789012345678901234567890123");
    }

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void tooShortURLTest() throws URISyntaxException, QblDropInvalidURL {
        exception.expect(QblDropInvalidURL.class);
        new DropURL("http://www.bar.org/not43base64chars");
    }

    @Test
    public void tooLongURLTest() throws QblDropInvalidURL, URISyntaxException {
        exception.expect(QblDropInvalidURL.class);
        new DropURL("http://www.foo.org/01234567890123456789012345678901234567890123");
    }

    @Test
    public void nonBase64URLTest() throws URISyntaxException, QblDropInvalidURL {
        exception.expect(QblDropInvalidURL.class);
        new DropURL("http://www.baz.org/2@34567890123456789012345678901234567890123");
    }

    @Test
    public void testGeneration() throws URISyntaxException, QblDropInvalidURL {
        DropServer server = new DropServer();
        server.setUri(new URI("http://example.com"));
        DropURL drop = new DropURL(server);
        new DropURL(drop.toString());
    }
}

package de.qabel.core.drop;

import com.google.gson.annotations.JsonAdapter;
import de.qabel.core.exceptions.QblDropInvalidURL;
import de.qabel.core.config.DropServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.UrlBase64;

import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Class DropURL represents a URL fully identifying a drop.
 */
@JsonAdapter(DropURLTypeAdapter.class)
public class DropURL implements Serializable {
    private static final long serialVersionUID = 8103657475203731210L;

    private static final Logger logger = LoggerFactory.getLogger(DropURL.class.getName());

    private URI uri;

    /**
     * Constructs a drop url by the given url.
     *
     * @param url DropURL fully qualifying a drop
     * @throws URISyntaxException if the string could not be parsed as URI reference
     * @throws QblDropInvalidURL  if the url is generally well-formed but violates the drop url syntax
     */
    public DropURL(String url) throws URISyntaxException, QblDropInvalidURL {
        uri = new URI(url);
        checkDropIdFromUrl();
    }

    /**
     * Constructs a new drop url for a drop on the given drop server.
     * This uses the default drop id generator.
     *
     * @param server Hosting drop server
     * @see DropIdGenerator
     */
    public DropURL(DropServer server) {
        this(server, DropIdGenerator.getDefaultDropIdGenerator());
    }

    /**
     * Constructs a new drop url for a drop on the given drop server.
     *
     * @param server    Hosting drop server.
     * @param generator Generator used for drop id generation.
     */
    public DropURL(DropServer server, DropIdGenerator generator) {
        String dropId = generator.generateDropId();

        try {
            uri = new URI(server.getUri() + "/" + dropId);
        } catch (URISyntaxException e) {
            logger.error("Failed to create drop url.", e);
            // should not happen - cannot recover from this
            throw new RuntimeException("Failed to create drop url.", e);
        }
    }

    /**
     * Gets drop id part of drop url.
     *
     * @return the drop id
     */
    private String getDropId() {
        String path = uri.getPath();
        return path.substring(path.lastIndexOf("/") + 1);
    }

    /**
     * Syntactically verifies the dropId part of the url.
     */
    private void checkDropIdFromUrl() throws QblDropInvalidURL {
        // check if its a valid Drop-URL.
        String dropID = getDropId();

        if (dropID.length() != DropIdGenerator.DROP_ID_LENGTH) {
            throw new QblDropInvalidURL();
        }

        try {
            UrlBase64.decode(dropID + "."); // add terminating dot
        } catch (DecoderException e) {
            throw new QblDropInvalidURL();
        }
    }

    /**
     * Gets the URI of this drop.
     *
     * @return the URI
     */
    public URI getUri() {
        return uri;
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (uri == null ? 0 : uri.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        DropURL other = (DropURL) obj;
        if (uri == null) {
            if (other.uri != null) {
                return false;
            }
        } else if (!uri.equals(other.uri)) {
            return false;
        }
        return true;
    }
}

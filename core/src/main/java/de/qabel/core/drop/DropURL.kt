package de.qabel.core.drop

import com.google.gson.annotations.JsonAdapter
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.config.DropServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongycastle.util.encoders.DecoderException
import org.spongycastle.util.encoders.UrlBase64

import java.io.Serializable
import java.net.URI
import java.net.URISyntaxException

/**
 * Class DropURL represents a URL fully identifying a drop.
 */
@JsonAdapter(DropURLTypeAdapter::class)
class DropURL : Serializable {

    /**
     * Gets the URI of this drop.

     * @return the URI
     */
    var uri: URI
        private set

    /**
     * Constructs a drop url by the given url.

     * @param url DropURL fully qualifying a drop
     * *
     * @throws URISyntaxException if the string could not be parsed as URI reference
     * *
     * @throws QblDropInvalidURL  if the url is generally well-formed but violates the drop url syntax
     */
    @Throws(URISyntaxException::class, QblDropInvalidURL::class)
    constructor(url: String) {
        uri = URI(url)
        checkDropIdFromUrl()
    }

    /**
     * Constructs a new drop url for a drop on the given drop server.

     * @param server    Hosting drop server.
     * *
     * @param generator Generator used for drop id generation.
     */
    @JvmOverloads constructor(server: DropServer, generator: DropIdGenerator = DropIdGenerator.defaultDropIdGenerator) {
        val dropId = generator.generateDropId()

        try {
            uri = URI(server.uri.toString() + "/" + dropId)
        } catch (e: URISyntaxException) {
            logger.error("Failed to create drop url.", e)
            // should not happen - cannot recover from this
            throw RuntimeException("Failed to create drop url.", e)
        }

    }

    /**
     * Gets drop id part of drop url.

     * @return the drop id
     */
    private val dropId: String
        get() {
            val path = uri.path
            return path.substring(path.lastIndexOf("/") + 1)
        }

    /**
     * Syntactically verifies the dropId part of the url.
     */
    @Throws(QblDropInvalidURL::class)
    private fun checkDropIdFromUrl() {
        // check if its a valid Drop-URL.
        val dropID = dropId

        if (dropID.length != DropIdGenerator.DROP_ID_LENGTH) {
            throw QblDropInvalidURL()
        }

        try {
            UrlBase64.decode(dropID + ".") // add terminating dot
        } catch (e: DecoderException) {
            throw QblDropInvalidURL()
        }

    }

    override fun toString(): String {
        return uri.toString()
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (uri == null) 0 else uri.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DropURL?
        if (uri == null) {
            if (other!!.uri != null) {
                return false
            }
        } else if (uri != other!!.uri) {
            return false
        }
        return true
    }

    companion object {
        private val serialVersionUID = 8103657475203731210L

        private val logger = LoggerFactory.getLogger(DropURL::class.java.name)
    }
}
/**
 * Constructs a new drop url for a drop on the given drop server.
 * This uses the default drop id generator.

 * @param server Hosting drop server
 * *
 * @see DropIdGenerator
 */

package de.qabel.core.config

import java.net.URI

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#drop-server
 */
class DropServer : SyncSettingItem {
    /**
     * URI to the DropServer without the drop id.
     * Field name in serialized json: "uri"
     */
    /**
     * Returns the uri of the DropServer.

     * @return URI
     */
    /**
     * Sets the uri of the DropServer.

     * @param uri URI of the DropServer.
     */
    var uri: URI? = null
    /**
     * Authentication for the DropServer (Credential for optional, additional access regulation).
     * Field name in serialized json: "auth"
     */
    /**
     * Returns the authentication of the DropServer.

     * @return authentication
     */
    /**
     * Sets the authentication of the DropServer.

     * @param value Authentication for the DropServer.
     */
    var auth: String? = null
    /**
     * Status flag of the DropServer.
     * Field name in serialized json: "active"
     */
    /**
     * Returns the status flag of the DropServer.

     * @return boolean
     */
    /**
     * Sets the status flag of the DropServer.

     * @param value Status flag to set the DropServer to.
     */
    var isActive: Boolean = false

    /**
     * Creates an instance of DropServer.

     * @param uri    URI of the DropServer.
     * *
     * @param auth   Authentication for the DropServer.
     * *
     * @param active Status flag of the DropServer.
     */
    constructor(uri: URI, auth: String?, active: Boolean) {
        this.uri = uri
        this.auth = auth
        isActive = active
    }

    /**
     * Creates an instance of DropServer.
     */
    constructor() {

    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = super.hashCode()

        result = prime * result + if (isActive) 1231 else 1237
        result = prime * result + if (auth == null) 0 else auth!!.hashCode()
        result = prime * result + if (uri == null) 0 else uri!!.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (super.equals(obj) == false) {
            return false
        }

        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }

        val other = obj as DropServer?
        if (isActive != other!!.isActive) {
            return false
        }
        if (auth == null) {
            if (other.auth != null) {
                return false
            }
        } else if (auth != other.auth) {
            return false
        }
        if (uri == null) {
            if (other.uri != null) {
                return false
            }
        } else if (uri != other.uri) {
            return false
        }
        return true
    }

    companion object {
        private val serialVersionUID = 6784516352213179983L
    }

}

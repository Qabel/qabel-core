package de.qabel.core.config

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#account
 */
class Account
/**
 * Creates an instance of Account

 * @param provider Provider of the account.
 * *
 * @param user     User of the account
 * *
 * @param auth     Authentication of the account
 */
(provider: String, user: String, auth: String) : SyncSettingItem() {
    /**
     * Provider of the account
     * Field name in serialized json: "provider"
     */
    var provider: String? = null
    /**
     * User of the account
     * Field name in serialized json: "user"
     */
    var user: String? = null
    /**
     * Authentication of the account
     * Field name in serialized json: "auth"
     */
    var auth: String? = null

    init {
        this.provider = provider
        this.user = user
        this.auth = auth
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = super.hashCode()

        result = prime * result + if (auth == null) 0 else auth!!.hashCode()
        result = prime * result + if (provider == null) 0 else provider!!.hashCode()
        result = prime * result + if (user == null) 0 else user!!.hashCode()
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

        val other = obj as Account?
        if (auth == null) {
            if (other!!.auth != null) {
                return false
            }
        } else if (auth != other!!.auth) {
            return false
        }
        if (provider == null) {
            if (other.provider != null) {
                return false
            }
        } else if (provider != other.provider) {
            return false
        }
        if (user == null) {
            if (other.user != null) {
                return false
            }
        } else if (user != other.user) {
            return false
        }
        return true
    }

    companion object {
        private val serialVersionUID = -6902585443982494539L
    }
}

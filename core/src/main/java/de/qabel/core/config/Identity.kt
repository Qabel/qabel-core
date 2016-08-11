package de.qabel.core.config

import com.google.gson.annotations.SerializedName
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL

import java.util.*
import kotlin.properties.Delegates


class Identity(alias: String, drops: Collection<DropURL>,
               primaryKeyPair: QblECKeyPair) : Entity(drops) {

    /**
     * Sets the alias address of the identity.
     * The alias is mutable, thus has no influence on the hashCode evaluation.

     * @param alias the alias of the identity
     */
    var alias: String by Delegates.observable(alias) {
        property, old, new ->
            notifyAllObservers()
        }

    /**
     * Returns the email address of the identity.

     * @return email
     */
    /**
     * Sets the email address of the identity.
     * The email address is optional, thus has no influence on the hashCode evaluation.

     * @param email the email address of the identity
     */
    var email: String? = null

    /**
     * Returns the phone number of the identity.

     * @return phone the phone number of the identity
     */
    /**
     * Sets the phone number of the identity.
     * The phone number is optional, thus has no influence on the hashCode evaluation.

     * @param phone the phone number of the identity
     */
    var phone: String? = null

    /**
     * Returns the list of prefixes of the identity

     * @return prefixes
     */
    /**
     * Sets the list of prefixes of the identity.
     * The list of prefixes is mutable, thus has no influence on the hashCode evaluation.

     * @param prefixes the prefixes of the identity
     */
    var prefixes: MutableList<String> = mutableListOf()

    /**
     * Returns the primary key pair of the identity.

     * @return QblECKeyPair
     */
    /**
     * Sets the primary key pair of the identity.

     * @param key Primary Key pair of the identity.
     */
    @SerializedName("keys")
    var primaryKeyPair: QblECKeyPair

    init {
        this.primaryKeyPair = primaryKeyPair
        prefixes = ArrayList<String>()
    }

    private val observers = ArrayList<IdentityObserver>()

    fun attach(observer: IdentityObserver) {
        observers.add(observer)
    }

    fun notifyAllObservers() {
        for (observer in observers) {
            observer.update()
        }
    }

    fun toContact(): Contact {
        val contact = Contact(this.alias, dropUrls, ecPublicKey)
        contact.email = email
        contact.phone = phone
        return contact
    }

    override val ecPublicKey: QblECPublicKey
        get() = primaryKeyPair.pub

    /**
    * The drop URL that should be used for the HELLO protocol, i.e.
    * made public and exported to qabel-index.
    */
    val helloDropUrl: DropURL
        get() = dropUrls.first()

    override fun hashCode(): Int {
        val prime = 31
        var result = 1

        result = super.hashCode()
        result = prime * result + if (primaryKeyPair == null) 0 else primaryKeyPair!!.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (!super.equals(obj)) {
            return false
        }

        if (this === obj) {
            return true
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }
        val other = obj as Identity?
        if (this.alias == null) {
            if (other!!.alias != null) {
                return false
            }
        } else if (this.alias != other!!.alias) {
            return false
        }
        if (primaryKeyPair == null) {
            if (other.primaryKeyPair != null) {
                return false
            }
        } else if (primaryKeyPair != other.primaryKeyPair) {
            return false
        }
        if (prefixes == null) {
            if (other.prefixes != null) {
                return false
            }
        } else if (prefixes != other.prefixes) {
            return false
        }
        if (email == null) {
            if (other.email != null) {
                return false
            }
        } else if (email != other.email) {
            return false
        }
        if (phone == null) {
            if (other.phone != null) {
                return false
            }
        } else if (phone != other.phone) {
            return false
        }

        return true
    }

    companion object {
        private val serialVersionUID = 3949018763372790094L
    }
}

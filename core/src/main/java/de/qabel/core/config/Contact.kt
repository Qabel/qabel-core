package de.qabel.core.config

import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL

class Contact : Entity {

    /**
     * Returns the alias name of the identity.

     * @return alias
     */
    var alias: String? = null

    /**
     * Returns the email address of the identity.

     * @return email
     */
    /**
     * Sets the email address of the identity.
     * The email address is optional, thus has no influence on the identity / hashCode / equals evaluation.

     * @param email the email address of the identity
     */
    var email: String? = null

    /**
     * Returns the phone number of the identity.

     * @return phone the phone number of the identity
     */
    /**
     * Sets the phone number of the identity.
     * The phone number is optional, thus has no influence on the identity / hashCode / equals evaluation.

     * @param phone the phone number of the identity
     */
    var phone: String? = null

    //Internal nick given by user. Do not export anywhere!
    var nickName: String? = null

    /**
     * Returns the primary public key of the contact

     * @return QblECPublicKey
     */
    /**
     * Sets the primary public key of the contacts
     */
    override var ecPublicKey: QblECPublicKey? = null

    var status = ContactStatus.NORMAL

    var isIgnored = false

    enum class ContactStatus private constructor(var status: Int) {
        UNKNOWN(0), NORMAL(1), VERIFIED(2)
    }

    /**
     * Creates an instance of Contact and sets the contactOwner and contactOwnerKeyId
     */
    constructor(alias: String, dropUrls: Collection<DropURL>, pubKey: QblECPublicKey) : super(dropUrls) {
        ecPublicKey = pubKey
        this.alias = alias
    }

    /**
     * Creates an instance of Contact without any attributes set
     * Attention: This constructor is intended for deserialization purposes when getting copied by ContactsActor
     */
    protected constructor() : super(null) {
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + if (ecPublicKey == null) 0 else ecPublicKey!!.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }
        val other = obj as Contact?
        if (ecPublicKey == null) {
            if (other!!.ecPublicKey != null) {
                return false
            }
        } else if (ecPublicKey != other!!.ecPublicKey) {
            return false
        }
        if (alias == null) {
            if (other.alias != null) {
                return false
            }
        } else if (alias != other.alias) {
            return false
        }
        return true
    }

    companion object {
        private val serialVersionUID = 3971315594579958553L
    }
}

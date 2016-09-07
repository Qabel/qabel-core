package de.qabel.core.config;

import de.qabel.core.crypto.QblECPublicKey;
import de.qabel.core.drop.DropURL;

import java.util.Collection;

public class Contact extends Entity {
    private static final long serialVersionUID = 3971315594579958553L;

    private String alias;

    private String email = "";

    private String phone = "";

    //Internal nick given by user. Do not export anywhere!
    private String nickName;

    private QblECPublicKey ecPublicKey;

    private ContactStatus status = ContactStatus.NORMAL;

    private boolean ignored = false;

    public enum ContactStatus {
        UNKNOWN(0), NORMAL(1), VERIFIED(2);

        public int status;
        ContactStatus(int status){ this.status = status; }
    }

    public ContactStatus getStatus() {
        return status;
    }

    public void setStatus(ContactStatus status) {
        this.status = status;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public boolean isIgnored() {
        return ignored;
    }

    public void setIgnored(boolean ignored) {
        this.ignored = ignored;
    }

    /**
     * Returns the primary public key of the contact
     *
     * @return QblECPublicKey
     */
    @Override
    public QblECPublicKey getEcPublicKey() {
        return ecPublicKey;
    }

    /**
     * Sets the primary public key of the contacts
     */
    public void setEcPublicKey(QblECPublicKey key) {
        ecPublicKey = key;
    }

    /**
     * Returns the alias name of the identity.
     *
     * @return alias
     */
    public String getAlias() {
        return alias;
    }

    public void setAlias(String value) {
        alias = value;
    }

    /**
     * Returns the email address of the identity.
     *
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the identity.
     * The email address is optional, thus has no influence on the identity / hashCode / equals evaluation.
     *
     * @param email the email address of the identity
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number of the identity.
     *
     * @return phone the phone number of the identity
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Sets the phone number of the identity.
     * The phone number is optional, thus has no influence on the identity / hashCode / equals evaluation.
     *
     * @param phone the phone number of the identity
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Creates an instance of Contact and sets the contactOwner and contactOwnerKeyId
     */
    public Contact(String alias, Collection<DropURL> dropUrls, QblECPublicKey pubKey) {
        super(dropUrls);
        setEcPublicKey(pubKey);
        this.alias = alias;
    }

    /**
     * Creates an instance of Contact without any attributes set
     * Attention: This constructor is intended for deserialization purposes when getting copied by ContactsActor
     */
    protected Contact() {
        super(null);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
            + (ecPublicKey == null ? 0 : ecPublicKey.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Contact other = (Contact) obj;
        if (ecPublicKey == null) {
            if (other.ecPublicKey != null) {
                return false;
            }
        } else if (!ecPublicKey.equals(other.ecPublicKey)) {
            return false;
        }
        if (alias == null) {
            if (other.alias != null) {
                return false;
            }
        } else if (!alias.equals(other.alias)) {
            return false;
        }
        return true;
    }
}

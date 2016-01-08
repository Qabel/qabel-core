package de.qabel.core.config;

import de.qabel.core.crypto.*;
import de.qabel.core.drop.DropURL;

import java.util.*;

public class Contact extends Entity {
	private static final long serialVersionUID = 3971315594579958553L;

	private String alias;

	private String email;

	private String phone;

	private QblECPublicKey ecPublicKey;

	private Identity contactOwner;

	private String contactOwnerKeyId;

	/**
	 * Returns the primary public key of the contact
	 * @return QblECPublicKey
	 */
	@Override
	public QblECPublicKey getEcPublicKey() {
		return ecPublicKey;
	}

	/**
	 * Sets the primary public key of the contacts
	 * @param key
	 */
	public void setEcPublicKey(QblECPublicKey key)
	{
		ecPublicKey = key;
	}

	/**
	 * Returns the alias name of the identity.
	 * @return alias
	 */
	public String getAlias() {
		return this.alias;
	}

	public void setAlias(String value) {
		this.alias = value;
	}

	/**
	 * Returns the email address of the identity.
	 * @return email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * Sets the email address of the identity.
	 * The email address is optional, thus has no influence on the identity / hashCode / equals evaluation.
	 * @param email the email address of the identity
	 */
	public void setEmail(String email) {
		this.email = email;
	}

	/**
	 * Returns the phone number of the identity.
	 * @return phone the phone number of the identity
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * Sets the phone number of the identity.
	 * The phone number is optional, thus has no influence on the identity / hashCode / equals evaluation.
	 * @param phone the phone number of the identity
	 */
	public void setPhone(String phone) {
		this.phone = phone;
	}

	/**
	 * Returns the identity which owns the contact.
	 * Note: This is not the identity which is represented by this contact!
	 * @return contactOwner
	 */
	public Identity getContactOwner()
	{
		return contactOwner;
	}

	/**
	 * Sets the contact owning identity.
	 * Note: This is not the identity which is represented by this contact!
	 * Automatically updates the contactOwnerKeyId.
	 * @param identity
	 */
	public void setContactOwner (Identity identity) {
		this.contactOwner = identity;
		this.contactOwnerKeyId = identity.getKeyIdentifier();
	}

	/**
	 * The key identifier of the identity the contact belongs to.
	 * A key identifier is defined as the right-most 64 bit of the identity's public fingerprint.
	 * @return contactOwnerKeyId
	 */
	public String getContactOwnerKeyId() {
		return this.contactOwnerKeyId;
	}


	/**
	 * Creates an instance of Contact and sets the contactOwner and contactOwnerKeyId
	 */
	public Contact(Identity owner, String alias, Collection<DropURL> dropUrls, QblECPublicKey pubKey) {
		super(dropUrls);
		this.contactOwner = owner;
		this.contactOwnerKeyId = owner.getKeyIdentifier();
		this.setEcPublicKey(pubKey);
		this.alias = alias;
	}

	/**
	 * Creates an instance of Contact and sets the contactOwnerId.
	 * Attention: This constructor is intended for deserialization purposes. The contactOwner needs to be set afterwards
	 */
	protected Contact(String ownerKeyId, String alias, Collection<DropURL> dropUrls, QblECPublicKey pubKey) {
		super(dropUrls);
		this.contactOwnerKeyId = ownerKeyId;
		this.setEcPublicKey(pubKey);
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
				+ ((contactOwner == null) ? 0 : contactOwner.hashCode());
		result = prime * result
				+ ((contactOwnerKeyId == null) ? 0 : contactOwnerKeyId.hashCode());
		result = prime * result
				+ ((ecPublicKey == null) ? 0 : ecPublicKey.hashCode());
		result = prime * result
				+ ((alias == null) ? 0 : alias.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (contactOwner == null) {
			if (other.contactOwner != null)
				return false;
		} else if (!contactOwner.equals(other.contactOwner))
			return false;
		if (contactOwnerKeyId == null) {
			if (other.contactOwnerKeyId != null)
				return false;
		} else if (!contactOwnerKeyId.equals(other.contactOwnerKeyId))
			return false;
		if (ecPublicKey == null) {
			if (other.ecPublicKey != null)
				return false;
		} else if (!ecPublicKey.equals(other.ecPublicKey))
			return false;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		return true;
	}
}

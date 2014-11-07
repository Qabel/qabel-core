package de.qabel.core.config;

import de.qabel.core.crypto.*;
import de.qabel.core.drop.DropURL;

import java.security.InvalidKeyException;
import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contact
 */
public class Contact {
	/**
	 * Primary public key of the contact
	 */
	private QblPrimaryPublicKey primaryPublicKey;
	/**
	 * The owner identity which owns this contact.
	 * Note: This is not the identity which is represented by this contact!
	 */
	private Identity contactOwner;
	/**
	 * The key identifier of the identity the contact belongs to.
	 * A key identifier is defined as the right-most 64 bit of the identity's public fingerprint
	 */
	private String contactOwnerKeyId;
	/**
	 * List of drop urls of the contact
	 */
	private final Set<DropURL> dropUrls = new HashSet<DropURL>(); //TODO: Have drop urls management with add/remove/edit events etc.
	/**
	 * List of module specific settings for the contact
	 */
	private final Set<AbstractModuleSettings> moduleSettings = new HashSet<AbstractModuleSettings>(); //TODO: Will there be a module settings manager (and thus not a smimple set) as well?
	
	/**
	 * Returns the primary public key of the contact
	 * @return QblPrimaryPublicKey
	 */
	public QblPrimaryPublicKey getPrimaryPublicKey()
	{
		return primaryPublicKey;
	}
	
	/**
	 * Sets the primary public key of the contacts
	 * @param key
	 */
	public void setPrimaryPublicKey(QblPrimaryPublicKey key)
	{
		primaryPublicKey = key;
	}
	
	/**
	 * Returns the identity which owns the contact
	 * @return contactOwner
	 */
	public Identity getContactOwner()
	{
		return contactOwner;
	}
	
	/**
	 * Sets the contact owning identity
	 * @param identity
	 */
	public void setContactOwner (Identity identity) {
		this.contactOwner = identity;
		this.contactOwnerKeyId = identity.getKeyIdentifier();
	}
	
	/**
	 * Returns the key identifier of the contact owning identity
	 * @return contactOwnerKeyId
	 */
	public String getContactOwnerKeyId() {
		return this.contactOwnerKeyId;
	}
	
	/**
	 * Returns the public encryption key of the contact
	 * @return QblEncPublicKey
	 */
	public QblEncPublicKey getEncryptionPublicKey()
	{
		return primaryPublicKey.getEncPublicKey();
	}
	
	/**
	 * Sets the public encryption key of the contact
	 * @param key
	 * @throws InvalidKeyException
	 */
	public void setEncryptionPublicKey(QblEncPublicKey key) throws InvalidKeyException
	{
		primaryPublicKey.attachEncPublicKey(key);
	}
	
	/**
	 * Returns the public signing key of the contact
	 * @return QblSignPublicKey
	 */
	public QblSignPublicKey getSignaturePublicKey()
	{
		return primaryPublicKey.getSignPublicKey();
	}
	
	/**
	 * Sets the public signing key of the contact
	 * @param key
	 * @throws InvalidKeyException
	 */
	public void setSignaturePublicKey(QblSignPublicKey key) throws InvalidKeyException
	{
		primaryPublicKey.attachSignPublicKey(key);
	}
	
	/**
	 * Returns a collection of the drop urls of the contact
	 * @return Collection<DropURL>
	 */
	public Collection<DropURL> getDropUrls()
	{
		return dropUrls;
	}
	
	/**
	 * Returns a set of the module specific settings of the contact
	 * @return Set<AbstractModuleSettings>
	 */
	public Set<AbstractModuleSettings> getModuleSettings()
	{
		return moduleSettings;
	}
	
	/**
	 * Creates an instance of Contact and sets the contactOwner and contactOwnerKeyId
	 * @param owner
	 */
	public Contact(Identity owner)
	{
		this.contactOwner = owner;
		this.contactOwnerKeyId = owner.getKeyIdentifier();
	}
	
	/**
	 * Creates an instance of Contact and sets the contactOwnerId.
	 * Attention: This constructor is intended for deserialization purposes. The contactOwner needs to be set afterwards
	 * @param ownerKeyId
	 */
	protected Contact(String ownerKeyId) {
		this.contactOwnerKeyId = ownerKeyId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((contactOwner == null) ? 0 : contactOwner.hashCode());
		result = prime * result
				+ ((dropUrls == null) ? 0 : dropUrls.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Contact other = (Contact) obj;
		if (contactOwner == null) {
			if (other.contactOwner != null)
				return false;
		} else if (!contactOwner.equals(other.contactOwner))
			return false;
		if (dropUrls == null) {
			if (other.dropUrls != null)
				return false;
		} else if (!dropUrls.equals(other.dropUrls))
			return false;
		return true;
	}
}

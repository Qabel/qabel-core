package de.qabel.core.config;

import de.qabel.core.crypto.*;

import java.net.*;
import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contact
 */
public class Contact {
	
	private QblPrimaryPublicKey primaryPublicKey;
	private QblEncPublicKey encryptionPublicKey;
	private QblSignPublicKey signaturePublicKey;
	/**
	 * The owner identity which owns this Contact.
	 * Note: This is not the identity which is represented by this contact!
	 */
	private final Identity contactOwner;
	private final Set<URL> dropUrls = new HashSet<URL>(); //TODO: Have drop urls management with add/remove/edit events etc.
	private final Set<AbstractModuleSettings> moduleSettings = new HashSet<AbstractModuleSettings>(); //TODO: Will there be a module settings manager (and thus not a smimple set) as well?
	
	public QblPrimaryPublicKey getPrimaryPublicKey()
	{
		return primaryPublicKey;
	}
	
	public void setPrimaryPublicKey(QblPrimaryPublicKey key)
	{
		primaryPublicKey = key;
	}
	
	public Identity getContactOwner()
	{
		return contactOwner;
	}
	
	public QblEncPublicKey getEncryptionPublicKey()
	{
		return encryptionPublicKey;
	}
	
	public void setEncryptionPublicKey(QblEncPublicKey key)
	{
		encryptionPublicKey = key;
	}
	
	public QblSignPublicKey getSignaturePublicKey()
	{
		return signaturePublicKey;
	}
	
	public void setSignaturePublicKey(QblSignPublicKey key)
	{
		signaturePublicKey = key;
	}
	
	public Set<URL> getDropUrls()
	{
		return dropUrls;
	}
	
	public Set<AbstractModuleSettings> getModuleSettings()
	{
		return moduleSettings;
	}
	
	
	public Contact(Identity owner)
	{
		this.contactOwner = owner;
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

package de.qabel.core.config;

import de.qabel.core.crypto.*;
import java.net.*;
import java.util.*;

/** 
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Contact-Drop-Messages#contact
 */
public class Contact {
	
	private QblEncPublicKey encryptionPublicKey;
	private QblSignPublicKey signaturePublicKey;
	/**
	 * The owner identity which owns this Contact.
	 * Note: This is not the identity which is represented by this contact!
	 */
	private final Identity contactOwner;
	private final Set<URL> dropUrls = new HashSet<URL>(); //TODO: Have drop urls management with add/remove/edit events etc.
	private final Set<AbstractModuleSettings> moduleSettings = new HashSet<AbstractModuleSettings>(); //TODO: Will there be a module settings manager (and thus not a smimple set) as well?
	
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
	
}

package de.qabel.core.config;

import de.qabel.core.crypto.*;
import de.qabel.core.drop.DropURL;

import java.util.Collection;

import com.google.gson.annotations.SerializedName;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identity
 */
public class Identity extends Entity {
	/**
	 * Alias name of the identity
	 * Field name in serialized json: "alias"
	 */
	private String alias;
	/**
	 * Primary key pair of the identity
	 * Field name in serialized json: "keys"
	 */
	@SerializedName("keys")
	private QblPrimaryKeyPair primaryKeyPair;

	/**
	 * Creates an instance of Identity
	 * @param alias
	 * @param drops
	 * @param primaryKeyPair
	 */
	public Identity(String alias, Collection<DropURL> drops, 
			QblPrimaryKeyPair primaryKeyPair) {
		super(drops);
		this.setAlias(alias);
		this.setPrimaryKeyPair(primaryKeyPair);
	}
	
	public void setAlias(String value) {
		this.alias = value;
	}

	/**
	 * Returns the alias name of the identity
	 * @return alias
	 */
	public String getAlias() {
		return this.alias;
	}

	/**
	 * Sets the primary key pair of the identity
	 * @param key
	 */
	public void setPrimaryKeyPair(QblPrimaryKeyPair key)
	{
		this.primaryKeyPair = key;
	}
	
	/**
	 * Returns the primary key pair of the identity
	 * @return QblPrimaryKeyPair
	 */
	public QblPrimaryKeyPair getPrimaryKeyPair()
	{
		return this.primaryKeyPair;
	}

	@Override
	public QblPrimaryPublicKey getPrimaryPublicKey() {
		return this.getPrimaryKeyPair().getQblPrimaryPublicKey();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		
		result = super.hashCode();
		
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (super.equals(obj) == false) {
		    return (false);
		}

		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Identity other = (Identity) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (primaryKeyPair == null) {
			if (other.primaryKeyPair != null)
				return false;
		} else if (!primaryKeyPair.equals(other.primaryKeyPair))
			return false;

		return true;
	}
}

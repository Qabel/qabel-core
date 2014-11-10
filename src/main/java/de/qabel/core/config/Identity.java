package de.qabel.core.config;

import de.qabel.core.crypto.*;
import de.qabel.core.drop.DropURL;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.google.gson.annotations.SerializedName;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identity
 */


public class Identity {
	private final int id;
	private int updated;
	private int created;
	private int deleted;
	/**
	 * Alias name of the identity
	 */
	private String alias;
	/**
	 * Primary key pair of the identity
	 */
	@SerializedName("keys")
	private QblPrimaryKeyPair primaryKeyPair;
	/**
	 * List of drop urls of the identity
	 */
	private Collection<DropURL> drops = new ArrayList<DropURL>();

	/**
	 * Creates an instance of Identity
	 * @param alias
	 * @param drops
	 * @param primaryKeyPair
	 */
	public Identity(String alias, Collection<DropURL> drops, 
			QblPrimaryKeyPair primaryKeyPair) {
		this.id = 0; //just to set it somehow
		this.setAlias(alias);
		this.setPrimaryKeyPair(primaryKeyPair);
		this.setDrops(drops);
	}
	
	public int getId() {
		return this.id;
	}

	public void setUpdated(int value) {
		this.updated = value;
	}

	public int getUpdated() {
		return this.updated;
	}

	public void setCreated(int value) {
		this.created = value;
	}

	public int getCreated() {
		return this.created;
	}

	public void setDeleted(int value) {
		this.deleted = value;
	}

	public int getDeleted() {
		return this.deleted;
	}

	/**
	 * Sets the alias name of the identity
	 * @param value
	 */
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

	/**
	 * Sets the list of drop urls of the identity
	 * @param drops
	 */
	public void setDrops(Collection<DropURL> drops) {
		this.drops = drops;
	}

	/**
	 * Adds a drop url to the identity
	 * @param drop
	 */
	public void addDrop(DropURL drop) {
		this.drops.add(drop);
	}

	/**
	 * Returns unmodifiable collection of the identity's drop urls
	 * @return Collection<DropURL>
	 */
	public Collection<DropURL> getDrops() {
		return Collections.unmodifiableCollection(this.drops);
	}
	
	/**
	 * Returns the key identifier of the identity.
	 * The key identifier consists of the right-most 64 bit of the identity's public fingerprint
	 * @return key identifier
	 */
	public String getKeyIdentifier() {
		return this.primaryKeyPair.getQblPrimaryPublicKey().getReadableKeyIdentifier();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + created;
		result = prime * result + deleted;
		result = prime * result + id;
		result = prime * result + ((drops == null) ? 0 : drops.hashCode());
		result = prime * result + updated;
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
		Identity other = (Identity) obj;
		if (alias == null) {
			if (other.alias != null)
				return false;
		} else if (!alias.equals(other.alias))
			return false;
		if (created != other.created)
			return false;
		if (deleted != other.deleted)
			return false;
		if (id != other.id)
			return false;
		if (drops == null) {
			if (other.drops != null)
				return false;
		} else if (!drops.equals(other.drops))
			return false;
		if (updated != other.updated)
			return false;
		return true;
	}
}

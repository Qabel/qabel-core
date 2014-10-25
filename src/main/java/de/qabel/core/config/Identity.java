package de.qabel.core.config;

import de.qabel.core.crypto.*;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identity
 */


public class Identity {
	private final int id;
	private int updated;
	private int created;
	private int deleted;
	private String alias;
	private QblPrimaryKeyPair primaryKeyPair;
	private List<URL> drops = new ArrayList<URL>();

	/**
	 * <pre>
	 *           0..*     0..1
	 * Identity ------------------------- Identities
	 *           identity        &lt;       identities
	 * </pre>
	 */
	
	public Identity(String alias, List<URL> drops, 
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

	public void setAlias(String value) {
		this.alias = value;
	}

	public String getAlias() {
		return this.alias;
	}

	public void setPrimaryKeyPair(QblPrimaryKeyPair key)
	{
		this.primaryKeyPair = key;
	}
	
	public QblPrimaryKeyPair getPrimaryKeyPair()
	{
		return this.primaryKeyPair;
	}

	public void setDrops(List<URL> drops) {
		this.drops = drops;
	}

	public List<URL> getDrops() {
		return this.drops;
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

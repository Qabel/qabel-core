package de.qabel.core.config;

import de.qabel.core.crypto.*;

import java.net.URL;

public class Identity {
	private final int id;
	private int updated;
	private int created;
	private int deleted;
	private String alias;
	private QblPrimaryKeyPair primaryKeyPair;
	private URL inbox;
	/**
	 * <pre>
	 *           0..*     0..1
	 * Identity ------------------------- Identities
	 *           identity        &lt;       identities
	 * </pre>
	 */
	private Identities identities;
	
	public Identity(String alias, URL inbox) {
		this.id = 0; //just to set it somehow
		this.setAlias(alias);
		this.setPrimaryKeyPair(primaryKeyPair);
		this.setInbox(inbox);
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

	public void setInbox(URL inbox) {
		this.inbox = inbox;
	}

	public URL getInbox() {
		return this.inbox;
	}


	public void setIdentities(Identities value) {
		this.identities = value;
	}

	public Identities getIdentities() {
		return this.identities;
	}
	
	
	public Identity(int id)
	{
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((alias == null) ? 0 : alias.hashCode());
		result = prime * result + created;
		result = prime * result + deleted;
		result = prime * result + id;
		result = prime * result + ((inbox == null) ? 0 : inbox.hashCode());
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
		if (inbox == null) {
			if (other.inbox != null)
				return false;
		} else if (!inbox.equals(other.inbox))
			return false;
		if (updated != other.updated)
			return false;
		return true;
	}
}

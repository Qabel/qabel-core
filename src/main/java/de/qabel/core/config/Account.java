package de.qabel.core.config;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#account
 */
public class Account {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	/**
	 * Provider of the account
	 * Field name in serialized json: "provider"
	 */
	private String provider;
	/**
	 * User of the account
	 * Field name in serialized json: "user"
	 */
	private String user;
	/**
	 * Authentication of the account
	 * Field name in serialized json: "auth"
	 */
	private String auth;
	
	/**
	 * Creates an instance of Account
	 * @param provider
	 * @param user
	 * @param auth
	 */
	public Account(String provider, String user, String auth) {
		this.setProvider(provider);
		this.setUser(user);
		this.setAuth(auth);
	}

	public void setId(int value) {
		this.id = value;
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
	 * Sets the provider of the account
	 * @param value
	 */
	public void setProvider(String value) {
		this.provider = value;
	}

	/**
	 * Returns the provider of the account
	 * @return provider
	 */
	public String getProvider() {
		return this.provider;
	}

	/**
	 * Sets the user of the account
	 * @param value
	 */
	public void setUser(String value) {
		this.user = value;
	}

	/**
	 * Returns the user of the account
	 * @return user
	 */
	public String getUser() {
		return this.user;
	}

	/**
	 * Sets the authentication of the account
	 * @param value
	 */
	public void setAuth(String value) {
		this.auth = value;
	}

	/**
	 * Returns the authentication of the account
	 * @return auth
	 */
	public String getAuth() {
		return this.auth;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((auth == null) ? 0 : auth.hashCode());
		result = prime * result + created;
		result = prime * result + deleted;
		result = prime * result + id;
		result = prime * result
				+ ((provider == null) ? 0 : provider.hashCode());
		result = prime * result + updated;
		result = prime * result + ((user == null) ? 0 : user.hashCode());
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
		Account other = (Account) obj;
		if (auth == null) {
			if (other.auth != null)
				return false;
		} else if (!auth.equals(other.auth))
			return false;
		if (created != other.created)
			return false;
		if (deleted != other.deleted)
			return false;
		if (id != other.id)
			return false;
		if (provider == null) {
			if (other.provider != null)
				return false;
		} else if (!provider.equals(other.provider))
			return false;
		if (updated != other.updated)
			return false;
		if (user == null) {
			if (other.user != null)
				return false;
		} else if (!user.equals(other.user))
			return false;
		return true;
	}
}

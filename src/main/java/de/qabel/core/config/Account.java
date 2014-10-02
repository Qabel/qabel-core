package de.qabel.core.config;

public class Account {
	private int id;
	private int updated;
	private int created;
	private int deleted;
	private String provider;
	private String user;
	private String auth;
	/**
	 * <pre>
	 *           0..*     0..1
	 * Account ------------------------- Accounts
	 *           account        &lt;       accounts
	 * </pre>
	 */
	private Accounts accounts;
	
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


	public void setProvider(String value) {
		this.provider = value;
	}

	public String getProvider() {
		return this.provider;
	}


	public void setUser(String value) {
		this.user = value;
	}

	public String getUser() {
		return this.user;
	}


	public void setAuth(String value) {
		this.auth = value;
	}

	public String getAuth() {
		return this.auth;
	}


	public void setAccounts(Accounts value) {
		this.accounts = value;
	}

	public Accounts getAccounts() {
		return this.accounts;
	}

}

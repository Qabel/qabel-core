package de.qabel.core.config;

public class Account {
	private int id;

	public void setId(int value) {
		this.id = value;
	}

	public int getId() {
		return this.id;
	}

	private int updated;

	public void setUpdated(int value) {
		this.updated = value;
	}

	public int getUpdated() {
		return this.updated;
	}

	private int created;

	public void setCreated(int value) {
		this.created = value;
	}

	public int getCreated() {
		return this.created;
	}

	private int deleted;

	public void setDeleted(int value) {
		this.deleted = value;
	}

	public int getDeleted() {
		return this.deleted;
	}

	private String provider;

	public void setProvider(String value) {
		this.provider = value;
	}

	public String getProvider() {
		return this.provider;
	}

	private String user;

	public void setUser(String value) {
		this.user = value;
	}

	public String getUser() {
		return this.user;
	}

	private String auth;

	public void setAuth(String value) {
		this.auth = value;
	}

	public String getAuth() {
		return this.auth;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * Account ------------------------- Accounts
	 *           account        &lt;       accounts
	 * </pre>
	 */
	private Accounts accounts;

	public void setAccounts(Accounts value) {
		this.accounts = value;
	}

	public Accounts getAccounts() {
		return this.accounts;
	}

}

package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class Accounts {
	/**
	 * <pre>
	 *           0..1     0..1
	 * Accounts ------------------------- SyncedSettings
	 *           accounts        &lt;       syncedSettings
	 * </pre>
	 */
	private SyncedSettings syncedSettings;

	public void setSyncedSettings(SyncedSettings value) {
		this.syncedSettings = value;
	}

	public SyncedSettings getSyncedSettings() {
		return this.syncedSettings;
	}

	/**
	 * <pre>
	 *           0..1     0..*
	 * Accounts ------------------------- Account
	 *           accounts        &gt;       account
	 * </pre>
	 */
	private Set<Account> account;

	public Set<Account> getAccount() {
		if (this.account == null) {
			this.account = new HashSet<Account>();
		}
		return this.account;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((account == null) ? 0 : account.hashCode());
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
		Accounts other = (Accounts) obj;
		if (account == null) {
			if (other.account != null)
				return false;
		} else if (!account.equals(other.account))
			return false;
		return true;
	}
	
	

}

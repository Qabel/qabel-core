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

}

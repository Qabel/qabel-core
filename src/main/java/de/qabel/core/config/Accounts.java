package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class Accounts {
	
	/**
	 * <pre>
	 *           1     0..*
	 * Accounts ------------------------- Account
	 *           accounts        &gt;       account
	 * </pre>
	 */
	private final Set<Account> accounts = new HashSet<Account>();

	public Set<Account> getAccounts() {
		return Collections.unmodifiableSet(this.accounts);
	}
	
	public boolean add(Account account) {
		return this.accounts.add(account);
	}

	/**
	 * Removes account from list of accounts
	 * @param account
	 * @return true if account was contained in list, false if not
	 */
	public boolean remove(Account account) {
		return this.accounts.remove(account);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((accounts == null) ? 0 : accounts.hashCode());
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
		if (accounts == null) {
			if (other.accounts != null)
				return false;
		} else if (!accounts.equals(other.accounts))
			return false;
		return true;
	}
	
	

}

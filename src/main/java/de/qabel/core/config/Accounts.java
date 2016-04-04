package de.qabel.core.config;

import java.util.*;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#accounts
 */
public class Accounts {

    private final Map<String, Account> accounts = new HashMap<>();

    /**
     * Returns unmodifiable set of contained accounts
     *
     * @return Set<Account>
     */
    public Set<Account> getAccounts() {
        return Collections.unmodifiableSet(new HashSet<>(this.accounts.values()));
    }

    /**
     * Put an account
     *
     * @param account Account to put
     * @return True if newly added, false if updated
     */
    public boolean put(Account account) {
        if (this.accounts.put(account.getPersistenceID(), account) == null) {
            return true;
        }
        return false;
    }

    /**
     * Removes account from list of accounts
     *
     * @param account Account to be removed.
     * @return true if account was contained in list, false if not
     */
    public boolean remove(Account account) {
        if (this.accounts.remove(account.getPersistenceID()) == null) {
            return false;
        }
        return true;
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
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Accounts other = (Accounts) obj;
        if (accounts == null) {
            if (other.accounts != null) {
                return false;
            }
        } else if (!accounts.equals(other.accounts)) {
            return false;
        }
        return true;
    }
}

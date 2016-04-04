package de.qabel.core.config;

import org.meanbean.lang.Factory;

/**
 * AccountsTestFactory
 * Creates distinct instances of class Accounts
 * Attention: For testing purposes only!
 */
class AccountsTestFactory implements Factory<Accounts> {
    int i;

    @Override
    public Accounts create() {
        Accounts accounts = new Accounts();

        Account a = new Account("provider" + i, "user" + i, "auth" + i++);
        Account b = new Account("provider" + i, "user" + i, "auth" + i++);

        accounts.put(a);
        accounts.put(b);

        return accounts;
    }
}

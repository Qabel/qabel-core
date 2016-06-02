package de.qabel.core.config;

import de.qabel.core.config.Account;
import org.meanbean.lang.Factory;

/**
 * AccountTestFactory
 * Creates distinct instances of class Account
 * Attention: For testing purposes only!
 */
class AccountTestFactory implements Factory<Account> {
    int i;

    @Override
    public Account create() {
        return new Account("provider" + i, "user" + i, "auth" + i++);
    }
}

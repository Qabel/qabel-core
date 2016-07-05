package de.qabel.core.config.factory;

import de.qabel.core.config.Account;

public class DefaultAccountFactory implements AccountFactory {
    @Override
    public Account createAccount(String provider, String user, String auth) {
        return new Account(provider, user, auth);
    }
}

package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * AccountEquivalentTestFactory
 * Creates logically equivalent instances of class Account
 * Attention: For testing purposes only
 */
class AccountEquivalentTestFactory implements EquivalentFactory<Account>{
	@Override
	public Account create() {
		Account account = new Account("provider", "user", "auth");
		return account;
	}
}

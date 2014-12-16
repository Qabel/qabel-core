package de.qabel.core.config;

import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * AccountEquivalentTestFactory
 * Creates logically equivalent instances of class Account
 * Attention: For testing purposes only
 */
class AccountEquivalentTestFactory implements EquivalentFactory<Account>{
	long created = new Date().getTime();

	@Override
	public Account create() {
		Account account = new Account("provider", "user", "auth");
		account.setCreated(created);
		return account;
	}
}

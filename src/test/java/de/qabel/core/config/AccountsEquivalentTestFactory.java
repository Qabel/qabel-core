package de.qabel.core.config;

import org.meanbean.lang.EquivalentFactory;

/**
 * AccountEquivalentTestFactory
 * Creates logically equivalent instances of class Account
 * Attention: For testing purposes only
 */
class AccountsEquivalentTestFactory implements EquivalentFactory<Accounts>{
	Account a;
	Account b;

	AccountsEquivalentTestFactory() {
		AccountTestFactory accountFactory = new AccountTestFactory();
		a = accountFactory.create();
		b = accountFactory.create();
	}

	@Override
	public Accounts create() {
		Accounts accounts = new Accounts();

		accounts.add(a);
		accounts.add(b);

		return accounts;
	}
}

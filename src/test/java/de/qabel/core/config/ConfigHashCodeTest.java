package de.qabel.core.config;

import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

import de.qabel.core.ExtendedHashCodeMethodTester;
import de.qabel.core.crypto.QblPrimaryPublicKeyTestFactory;

public class ConfigHashCodeTest {
	@Test
	public void accountHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		tester.testHashCodeMethod(new AccountEquivalentTestFactory());
	}

	@Test
	public void contactHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		Configuration config = new ConfigurationBuilder()
		.overrideFactory("primaryPublicKey", new QblPrimaryPublicKeyTestFactory())
		.overrideFactory("contactOwner", new IdentityTestFactory())
		.ignoreProperty("contactOwnerKeyId") // depends on contactOwner, therefore not significant
		.build();
		tester.testHashCodeMethod(new ContactEquivalentTestFactory(), config);
	}
}

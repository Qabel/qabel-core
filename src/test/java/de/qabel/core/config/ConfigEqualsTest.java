package de.qabel.core.config;

import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;
import org.meanbean.test.EqualsMethodTester;

import de.qabel.core.crypto.QblPrimaryKeyPairTestFactory;
import de.qabel.core.crypto.QblPrimaryPublicKeyTestFactory;

public class ConfigEqualsTest {

	@Test
	public void accountEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(new AccountEquivalentTestFactory());
	}

	@Test
	public void identityEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("drops", new DropUrlListTestFactory())
			.overrideFactory("primaryKeyPair", new QblPrimaryKeyPairTestFactory())
			.iterations(10)
			.build();
		tester.testEqualsMethod(new IdentityEquivalentTestFactory(), config);
	}

	@Test
	public void dropServerEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("url", new UrlTestFactory())
			.build();
		tester.testEqualsMethod(new DropServerEquivalentTestFactory(), config);
	}

	@Test
	public void contactEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.iterations(10)
			.overrideFactory("primaryPublicKey", new QblPrimaryPublicKeyTestFactory())
			.overrideFactory("contactOwner", new IdentityTestFactory())
			.ignoreProperty("contactOwnerKeyId") // depends on contactOwner, therefore not significant
			.ignoreProperty("signaturePublicKey") // is already checked as part of primaryPublicKey
			.ignoreProperty("encryptionPublicKey") // is already checked as part of primaryPublicKey
			.build();
		tester.testEqualsMethod(new ContactEquivalentTestFactory(), config);
	}

	@Test
	public void storageServerEqualsTest () {
		EqualsMethodTester tester = new EqualsMethodTester();
		Configuration config = new ConfigurationBuilder()
			.overrideFactory("url", new UrlTestFactory())
			.build();
		tester.testEqualsMethod(new StorageServerEquivalentTestFactory(), config);
	}
}
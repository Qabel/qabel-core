package de.qabel.core.config;

import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;

import de.qabel.core.ExtendedHashCodeMethodTester;
import de.qabel.core.crypto.QblPrimaryKeyPairTestFactory;
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

	@Test
	public void dropServerHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		Configuration config = new ConfigurationBuilder()
		.overrideFactory("url", new UrlTestFactory())
		.build();
		tester.testHashCodeMethod(new DropServerEquivalentTestFactory(), config);
	}

	@Test
	public void identityHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		Configuration config = new ConfigurationBuilder()
		.overrideFactory("primaryKeyPair", new QblPrimaryKeyPairTestFactory())
		.overrideFactory("drops", new DropUrlListTestFactory())
		.build();
		tester.testHashCodeMethod(new IdentityEquivalentTestFactory(), config);
	}

	@Test
	public void storageServerHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		Configuration config = new ConfigurationBuilder()
		.overrideFactory("url", new UrlTestFactory())
		.build();
		tester.testHashCodeMethod(new StorageServerEquivalentTestFactory(), config);
	}

	@Test
	public void storageVolumeHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		tester.testHashCodeMethod(new StorageVolumeEquivalentTestFactory());
	}
}

package de.qabel.core.config;

import org.junit.Test;
import org.meanbean.test.Configuration;
import org.meanbean.test.ConfigurationBuilder;
import org.meanbean.test.EqualsMethodTester;

import de.qabel.core.crypto.QblPrimaryKeyPairTestFactory;

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
}
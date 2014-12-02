package de.qabel.core.config;

import org.junit.Test;
import org.meanbean.test.EqualsMethodTester;

public class ConfigEqualsTest {

	@Test
	public void accountEqualsTest() {
		EqualsMethodTester tester = new EqualsMethodTester();
		tester.testEqualsMethod(new AccountEquivalentTestFactory());
	}
}
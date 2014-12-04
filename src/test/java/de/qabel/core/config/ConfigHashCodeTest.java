package de.qabel.core.config;

import org.junit.Test;

import de.qabel.core.ExtendedHashCodeMethodTester;

public class ConfigHashCodeTest {
	@Test
	public void accountHashCodeTest() {
		ExtendedHashCodeMethodTester tester = new ExtendedHashCodeMethodTester();
		tester.testHashCodeMethod(new AccountEquivalentTestFactory());
	}
}

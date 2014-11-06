package de.qabel.core.config;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class ConfigEqualsTest {

	@Test
	public void AccountEqualsTest() {
		Account a = new Account("a", "s", "d");
		Account b = new Account("d", "e", "f");
		Account c = new Account("a", "s", "d");

		assertNotEquals(a, b);
		assertNotEquals(b, c);
		assertEquals(a, c);

		Accounts as = new Accounts();
		as.add(a);
		Accounts bs = new Accounts();
		bs.add(b);
		Accounts cs = new Accounts();
		cs.add(c);

		assertNotEquals(as, bs);
		assertNotEquals(bs, cs);
		assertEquals(as, cs);
	}
}
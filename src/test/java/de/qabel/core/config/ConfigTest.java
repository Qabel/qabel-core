package de.qabel.core.config;

import static org.junit.Assert.*;

import org.junit.Test;

public class ConfigTest {

	@Test
	public void qblKeyPairTest() {
		QblKeyPair qkp = new QblKeyPair();

		assertNotNull(qkp);
		assertNotNull(qkp.getPrivateKey());
		assertNotNull(qkp.getPublicKey());
		assertNotNull(qkp.getPublicKeyFingerprint());
	}

}

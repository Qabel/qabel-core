package de.qabel.core.crypto;

import org.junit.Assert;
import org.junit.Test;

public class QblPrimaryPublicKeyInconsistencyTest {
	@Test
	public void inconsistencyTest() {
		QblPrimaryKeyPair pkp = QblKeyFactory.getInstance().generateQblPrimaryKeyPair();
		QblPrimaryPublicKey ppp = pkp.getQblPrimaryPublicKey();
		Assert.assertFalse(ppp.getEncPublicKeys().isEmpty());
		Assert.assertFalse(ppp.getSignPublicKeys().isEmpty());
	}
}

package de.qabel.core.drop;

import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.core.config.Identity;

public class AcknowledgeIdGenerationTest {
	private Identity sender;

	@Before
	public void setup() {
		sender = new Identity("Bernd", null, new QblECKeyPair());
	}

	@Test
	public void testDefaultDisabledAck() {
		DropMessage dm = new DropMessage(sender, "", "");
		Assert.assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
	}

	@Test
	public void testSwitchAck() {
		DropMessage dm = new DropMessage(sender, "", "");
		dm.enableAcknowledgeing(true);
		Assert.assertNotEquals(DropMessage.NOACK, dm.getAcknowledgeID());
		dm.enableAcknowledgeing(false);
		Assert.assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
	}
}

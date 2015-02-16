package de.qabel.core.drop;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblKeyFactory;

public class AcknowledgeIdGenerationTest {
	private class DummyMessage extends ModelObject {
		// nothing just a dummy
	}

	private Identity sender;

	@Before
	public void setup() {
		sender = new Identity("Bernd", null, QblKeyFactory.getInstance().generateQblPrimaryKeyPair());
	}

	@Test
	public void testDefaultDisabledAck() {
		DropMessage<DummyMessage> dm = new DropMessage<>(sender, new DummyMessage());
		Assert.assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
	}

	@Test
	public void testSwitchAck() {
		DropMessage<DummyMessage> dm = new DropMessage<>(sender, new DummyMessage());
		dm.enableAcknowledgeing(true);
		Assert.assertNotEquals(DropMessage.NOACK, dm.getAcknowledgeID());
		dm.enableAcknowledgeing(false);
		Assert.assertEquals(DropMessage.NOACK, dm.getAcknowledgeID());
	}
}

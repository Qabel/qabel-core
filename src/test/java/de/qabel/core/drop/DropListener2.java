package de.qabel.core.drop;

import static org.junit.Assert.assertEquals;

public class DropListener2 implements DropListener {
	@Override
	public void onDropEvent(DropMessage<ModelObject> dropMessage) {
		assertEquals(1, dropMessage.getVersion());
		assertEquals("foo", dropMessage.getSender());
		assertEquals("bar", dropMessage.getAcknowledgeID());
		assertEquals(1412687357, dropMessage.getTime());
		assertEquals("payload data",
				((ModelObject1) dropMessage.getData()).content);

	}
}
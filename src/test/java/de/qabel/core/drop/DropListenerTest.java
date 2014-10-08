package de.qabel.core.drop;

import static org.junit.Assert.assertEquals;

import java.util.Date;

import org.junit.Test;

public class DropListenerTest {

	static public class ModelObject1 extends ModelObject {
		public String content;
	}

	static class DropListener1 implements DropListener {
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

	static class DropListener2 implements DropListener {
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

	@Test
	public void dropListenerTest() {

		DropListener dl1 = new DropListener1();
		DropListener dl2 = new DropListener2();

		ModelObject1 m = new ModelObject1();
		m.content = "payload data";

		DropController dc = new DropController();
		dc.register(m, dl1);
		dc.register(m, dl2);

		DropMessage<ModelObject1> dm = new DropMessage<ModelObject1>();
		Date date = new Date(1412687357);

		dm.setTime(date);
		dm.setSender("foo");
		dm.setData(m);
		dm.setAcknowledgeID("bar");
		dm.setVersion(1);
		dm.setModelObject(ModelObject1.class);

		dc.handleDrop(dm);
	}
}

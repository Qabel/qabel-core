package de.qabel.core.drop;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.concurrent.BlockingQueue;

import org.junit.Test;

public class DropListenerTest {

	final static int expectedTestMO1HandlerCalls = 2;
	final static int expectedTestMO2HandlerCalls = 0;

	static int testMO1HandlerCalled = 0;
	static int testMO2HandlerCalled = 0;

	static public class TestMO1 extends ModelObject {
		public String content;
	}

	static public class TestMO2 extends ModelObject {
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
					((TestMO1) dropMessage.getData()).content);
			testMO1HandlerCalled++;
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
					((TestMO1) dropMessage.getData()).content);
			testMO1HandlerCalled++;
		}
	}

	static class DropListener3 implements DropListener {
		@Override
		public void onDropEvent(DropMessage<ModelObject> dropMessage) {
			testMO2HandlerCalled++;
		}
	}

	@Test
	public void dropListenerTest() throws InterruptedException {

		DropListener dl1 = new DropListener1();
		DropListener dl2 = new DropListener2();
		DropListener dl3 = new DropListener3();

		TestMO1 mo1 = new TestMO1();
		mo1.content = "payload data";

		TestMO2 mo2 = new TestMO2();
		mo1.content = "payload data";

		DropController dc = new DropController();
		BlockingQueue<DropMessage<ModelObject>> bq1 = dc.register(mo1, dl1);
		BlockingQueue<DropMessage<ModelObject>> bq2 = dc.register(mo1, dl2);

		DropMessage<TestMO1> dm = new DropMessage<TestMO1>();
		Date date = new Date(1412687357);

		dm.setTime(date);
		dm.setSender("foo");
		dm.setData(mo1);
		dm.setAcknowledgeID("bar");
		dm.setVersion(1);
		dm.setModelObject(TestMO1.class);

		// DropListener1 and DropListener2 should be called for this DropMessage
		// type while DropListener3 should remain uncalled.
		dc.handleDrop(dm);
		assertEquals(expectedTestMO1HandlerCalls, testMO1HandlerCalled);
		assertEquals(expectedTestMO2HandlerCalls, testMO2HandlerCalled);
		
		assertEquals(dm, bq1.take());
		assertEquals(dm, bq2.take());
	}
}

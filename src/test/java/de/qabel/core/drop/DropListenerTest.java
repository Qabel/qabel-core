package de.qabel.core.drop;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import de.qabel.core.config.Identity;
import de.qabel.core.crypto.QblKeyFactory;

public class DropListenerTest {
	private Identity sender;

	static public class TestMO1 extends ModelObject {
		public String content;
	}

	static public class TestMO2 extends ModelObject {
		public String content;
	}
	
	@Before
	public void setup() {
		sender = new Identity("Bernd", new ArrayList<DropURL>(),
				QblKeyFactory.getInstance().generateQblPrimaryKeyPair());;
	}

	@Test
	public void dropListenerTest() throws InterruptedException {

		TestMO1 mo1 = new TestMO1();
		mo1.content = "payload data";

<<<<<<< HEAD
		DropController dc = new DropController();
=======
		TestMO2 mo2 = new TestMO2();
		mo1.content = "payload data";

		DropActor dc = new DropActor();
>>>>>>> Replace DropController by DropActor, an implementation of the Ackack EventActor.
		DropQueueCallback<TestMO1> bq1 = new DropQueueCallback<TestMO1>();
		dc.register(TestMO1.class, bq1);
		DropQueueCallback<TestMO1> bq2 = new DropQueueCallback<TestMO1>();
		dc.register(TestMO1.class, bq2);
		DropQueueCallback<TestMO2> bq3 = new DropQueueCallback<TestMO2>();
		dc.register(TestMO2.class, bq3);

		DropMessage<TestMO1> dm = new DropMessage<TestMO1>(sender, mo1);

		dc.handleDrop(dm);

		assertEquals(dm, bq1.take());
		assertEquals(dm, bq2.take());
		assertNull(bq3.poll(1L, TimeUnit.MILLISECONDS));
	}
}

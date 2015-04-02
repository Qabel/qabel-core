package de.qabel.core.drop;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import de.qabel.core.crypto.QblECKeyPair;
import org.junit.Before;
import org.junit.Test;

import de.qabel.core.config.Identity;

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
		sender = new Identity("Bernd", new ArrayList<DropURL>(), new QblECKeyPair());
	}

	@Test
	public void dropListenerTest() throws InterruptedException {

		TestMO1 mo1 = new TestMO1();
		mo1.content = "payload data";

		DropController dc = new DropController();
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

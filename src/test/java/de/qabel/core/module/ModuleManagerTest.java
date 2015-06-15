package de.qabel.core.module;

import static org.junit.Assert.*;

import de.qabel.ackack.MessageInfo;
import de.qabel.core.drop.DropActor;
import org.junit.Ignore;
import org.junit.Test;

public class ModuleManagerTest {
	static class TestModule extends Module {
		public boolean isInit = false;
		private boolean isRunning = false;

		public TestModule(ModuleManager moduleManager) {
			super(moduleManager);
			on(DropActor.EVENT_DROP_MESSAGE_RECEIVED_PREFIX, this);
		}

		@Override
		public void init() {
			isInit = true;
		}

		@Override
		public void moduleMain() {
			
		}

		@Override
		public void onEvent(String event, MessageInfo info, Object... data) {
		}

		@Override
		public void run() {
			setRunning(true);
			super.run();
		}

		public synchronized boolean isStarted() {
			return isRunning;
		}

		public synchronized void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}
	}

	@Test
	@Ignore
	public void lifeCycleTest() throws Exception {
		ModuleManager mm = new ModuleManager();
		TestModule module = mm.startModule(TestModule.class);
		assertTrue(module.isInit);
		Thread.sleep(2000);
		assertTrue(module.isStarted());
		mm.shutdown();
	}
}

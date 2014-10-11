package de.qabel.core.module;

import static org.junit.Assert.*;

import org.junit.Test;

public class ModuleManagerTest {
	static class TestModule extends Module {
		public boolean isInit = false;
		private boolean isRunning = false;

		public TestModule() {
			super(TestModule.class.getName());
		}

		@Override
		public void init() {
			isInit = true;
		}

		@Override
		public void run() {
			try {
				while (!this.isInterrupted()) {
					sleep(100);
					setRunning(true);
				}
				setRunning(false);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		public synchronized boolean isStarted() {
			return isRunning;
		}

		public synchronized void setRunning(boolean isRunning) {
			this.isRunning = isRunning;
		}
	}

	@Test
	public void liveCycleTest() throws Exception {
		ModuleManager mm = new ModuleManager();
		mm.startModule(TestModule.class);
		TestModule module = (TestModule) mm.getModules().iterator().next();
		assertTrue(module.isInit);
		assertFalse(module.isStarted());
		Thread.sleep(200);
		assertTrue(module.isStarted());
		mm.shutdown();
	}
}

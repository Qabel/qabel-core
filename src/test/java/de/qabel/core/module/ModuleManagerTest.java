package de.qabel.core.module;

import static org.junit.Assert.*;

import java.lang.reflect.Constructor;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class ModuleManagerTest {
	static class TestModule extends Module {
        public boolean isInit = false;
		public TestModule() {
		}
		
		@Override
		public void init() {
			isInit = true;
		}
	}
	@Test
	public void instanciateModuleTest() throws InstantiationException, IllegalAccessException {
		ModuleManager mm = new ModuleManager();
		Constructor<?>[] c = TestModule.class.getConstructors();
		mm.initModule(TestModule.class);
		assertTrue(((TestModule)mm.getModules().iterator().next()).isInit);
	}
}

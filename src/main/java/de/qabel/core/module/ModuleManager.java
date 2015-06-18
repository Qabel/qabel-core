package de.qabel.core.module;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.ResourceActor;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;

/**
 * The ModuleManager is responsible for loading and starting Modules as well as
 * stopping and removing them.
 */
public class ModuleManager {
	private static ModuleManager defaultModuleManager = null;
	public final static ClassLoader LOADER = new ClassLoader();

	private final EventEmitter eventEmitter;
	private final ResourceActor resourceActor;

	private HashMap<Module, ModuleThread> modules;

	/**
	 * Get default ModuleManager. Creates a new one if none exists.
	 * @return Default ModuleManager
	 */
	public static ModuleManager getDefault() {
		if (defaultModuleManager == null) {
			defaultModuleManager = new ModuleManager();
		}
		return defaultModuleManager;
	}

	static public class ClassLoader extends URLClassLoader{
		public ClassLoader() {
			super(new URL[0]);
		}

		@Override
		public void addURL(URL url) {
			super.addURL(url);
		}
	}

	public ModuleManager() {
		this(EventEmitter.getDefault(), ResourceActor.getDefault());
	}

	public ModuleManager(EventEmitter emitter, ResourceActor resourceActor) {
		eventEmitter = emitter;
		modules = new HashMap<>();
		this.resourceActor = resourceActor;
	}

	EventEmitter getEventEmitter() {
		return eventEmitter;
	}

	public ResourceActor getResourceActor() {
		return resourceActor;
	}

	/**
	 * Starts a given Module by its class
	 * @param module Module to start.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public <T extends Module> T startModule(Class<T> module) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		T m = module.getConstructor(ModuleManager.class).newInstance(this);
		m.init();
        ModuleThread t = new ModuleThread(m);
		modules.put(m, t);
		t.start();
		return m;
	}

	public void startModule(File jar, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			ClassLoader cld = LOADER;
			cld.addURL(jar.toURI().toURL());
			Class cls = Class.forName(className, true, cld);
			try {
				startModule(cls.asSubclass(Module.class));
			}
			catch (ClassCastException e) {
				throw new InstantiationError(className + " not a subclass of Module!");
			}
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	/**
	 * Shuts down all Modules
	 */
	public void shutdown() {
		while(!modules.isEmpty()) {
			modules.values().iterator().next().getModule().stopModule();
		}
	}

	public void removeModule(Module module) {
		modules.remove(module);
	}

	/**
	 * Wraps the EventListener on method. Allows to
	 * force unique namespaces for modules.
	 * @param event Event name to register for
	 * @param module Module to add as EventListener
	 * @return True if successfully registered
	 */
	public boolean on(String event, Module module) {
		if (true) { //TODO: This could enforce unique names and disallow registering to certain events.
			module.doOn(event, module);
			return true;
		}
		return false;
	}
}

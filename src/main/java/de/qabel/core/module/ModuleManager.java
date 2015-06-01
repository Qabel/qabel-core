package de.qabel.core.module;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.storage.StorageConnection;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;

import de.qabel.core.config.Settings;
import de.qabel.core.drop.DropActor;

public class ModuleManager {
	final private EventEmitter eventEmitter;

	private static ModuleManager defaultModuleManager = null;

	public ModuleManager() {
		this(new EventEmitter());
	}

	public ModuleManager(EventEmitter emitter) {
		eventEmitter = emitter;
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

	public final static ClassLoader LOADER = new ClassLoader();

	private Set<StorageConnection> storageConnection;

	public Set<StorageConnection> getStorageConnection() {
		if (this.storageConnection == null) {
			this.storageConnection = new HashSet<StorageConnection>();
		}
		return this.storageConnection;
	}

	private Settings settings;

	public Settings getSettings() {
		if (this.settings == null) {
			this.settings = new Settings();
		}
		return this.settings;
	}

	/**
	 * <pre>
	 *           1..1     0..*
	 * ModuleManager ------------------------- Module
	 *           moduleManager        &gt;       modules
	 * </pre>
	 */
	private HashMap<Module, ModuleThread> modules;

	Thread dropReceiverThread = new Thread() {
		public void run() {
			// TODO: run receiver, call callback on the Module-Thread.
		};
	};
	
	public HashMap<Module, ModuleThread> getModules() {
		if (this.modules == null) {
			this.modules = new HashMap<Module, ModuleThread>();
		}
		return this.modules;
	}

	/**
	 * Starts a given Module by its class
	 * @param module Module to start.
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void startModule(Class<?> module) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
		Module m = (Module) module.getConstructor(EventEmitter.class).newInstance(eventEmitter);
		m.setModuleManager(this);
		m.init();
        ModuleThread t = new ModuleThread(m);
		getModules().put(m, t);
		t.start();
	}
	
	public void startModule(File jar, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			ClassLoader cld = LOADER;
			cld.addURL(jar.toURI().toURL());
			startModule(Class.forName(className, true, cld));
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/**
	 * Shuts down all Modules
	 */
	public void shutdown() {
		while(getModules().isEmpty() == false) {
			getModules().values().iterator().next().getModule().stopModule();
		}
	}
	/**
	 * 
	 */
	public void stopModule(Module module) {
        // TODO Wait till the Module really exits
        module.stopModule();
	}
	
	public DropActor getDropActor() {
		return dropActor;
	}

	public void setDropActor(DropActor dropActor) {
		this.dropActor = dropActor;
	}

	private DropActor dropActor;
}

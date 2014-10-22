package de.qabel.core.module;

import de.qabel.core.storage.StorageConnection;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Set;
import java.util.HashSet;

import de.qabel.core.config.Settings;
import de.qabel.core.drop.DropController;

public class ModuleManager {
	static public class ClassLoader extends URLClassLoader{
	    public ClassLoader() {
	        super(new URL[0]);
	    }

	    @Override
	    public void addURL(URL url) {
	        super.addURL(url);
	    }
	}

	public final static ClassLoader LOADER = new ClassLoader();

	/**
	 * <pre>
	 *           0..*     0..*
	 * ModuleManager ------------------------- StorageConnection
	 *           moduleManager        &lt;       storageConnection
	 * </pre>
	 */
	private Set<StorageConnection> storageConnection;

	public Set<StorageConnection> getStorageConnection() {
		if (this.storageConnection == null) {
			this.storageConnection = new HashSet<StorageConnection>();
		}
		return this.storageConnection;
	}

	/**
	 * <pre>
	 *           0..*     0..*
	 * ModuleManager ------------------------- Settings
	 *           moduleManager        &lt;       settings
	 * </pre>
	 */
	private Set<Settings> settings;

	public Set<Settings> getSettings() {
		if (this.settings == null) {
			this.settings = new HashSet<Settings>();
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
	private Set<Module> modules;

	Thread dropReceiverThread = new Thread() {
		public void run() {
			// TODO: run receiver, call callback on the Module-Thread.
		};
	};
	
	public Set<Module> getModules() {
		if (this.modules == null) {
			this.modules = new HashSet<Module>();
		}
		return this.modules;
	}

	/**
	 * Starts a given Module by its class
	 * @param module
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 */
	public void startModule(Class<?> module) throws InstantiationException, IllegalAccessException {
		Module m = (Module) module.newInstance();
		m.setModuleManager(this);
		m.init();
		getModules().add(m);
		m.start();
	}
	
	public void startModule(File jar, String className) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		try {
			ClassLoader cld = LOADER;
			cld.addURL(jar.toURI().toURL());
			startModule(Class.forName(className, true, cld));
		} catch (MalformedURLException e) {
			e.printStackTrace();
			// Should not happen!
			System.exit(1);
		}
	}
	
	/**
	 * Shuts down all Modules
	 */
	public void shutdown() {
		while(getModules().isEmpty() == false) {
			getModules().iterator().next().stopModule();
		}
	}
	/**
	 * 
	 */
	public void stopModule(Module module) {
		module.stopModule();
	}
	
	public DropController getDropController() {
		return dropController;
	}

	public void setDropController(DropController dropController) {
		this.dropController = dropController;
	}

	private DropController dropController;
}

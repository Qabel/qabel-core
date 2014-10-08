package de.qabel.core.module;

import de.qabel.core.storage.StorageConnection;

import java.util.Set;
import java.util.HashSet;

import de.qabel.core.config.Settings;

public class ModuleManager {
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

	public Set<Module> getModules() {
		if (this.modules == null) {
			this.modules = new HashSet<Module>();
		}
		return this.modules;
	}

	public void startModule(Class<?> module) throws InstantiationException, IllegalAccessException {
		Module m = (Module) module.newInstance();
		m.setModuleManager(this);
		m.init();
		getModules().add(m);
		
	}
}

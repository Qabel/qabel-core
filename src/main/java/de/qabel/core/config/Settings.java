package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

import de.qabel.core.module.ModuleManager;

public class Settings {
	/**
	 * <pre>
	 *           0..1     0..1
	 * Settings ------------------------- LocalSettings
	 *           settings        &gt;       localSettings
	 * </pre>
	 */
	private LocalSettings localSettings;

	public void setLocalSettings(LocalSettings value) {
		this.localSettings = value;
	}

	public LocalSettings getLocalSettings() {
		return this.localSettings;
	}

	/**
	 * <pre>
	 *           0..1     0..1
	 * Settings ------------------------- SyncedSettings
	 *           settings        &gt;       syncedSettings
	 * </pre>
	 */
	private SyncedSettings syncedSettings;

	public SyncedSettings getSyncedSettings() {
		return this.syncedSettings;
	}

	/**
	 * <pre>
	 *           0..*     0..*
	 * Settings ------------------------- ModuleManager
	 *           settings        &gt;       moduleManager
	 * </pre>
	 */
	private Set<ModuleManager> moduleManager;

	public Set<ModuleManager> getModuleManager() {
		if (this.moduleManager == null) {
			this.moduleManager = new HashSet<ModuleManager>();
		}
		return this.moduleManager;
	}

}

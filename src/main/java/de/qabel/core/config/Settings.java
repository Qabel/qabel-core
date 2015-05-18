package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

import de.qabel.core.module.ModuleManager;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#settings-client-notes
 */
public class Settings {

	private LocalSettings localSettings;

	/**
	 * Sets the local settings
	 * @param value Value to set the local settings to.
	 */
	public void setLocalSettings(LocalSettings value) {
		this.localSettings = value;
	}

	/**
	 * Returns the local settings
	 * @return localSettings
	 */
	public LocalSettings getLocalSettings() {
		return this.localSettings;
	}

	/**
	 * <pre>
	 *           1     1
	 * Settings ------------------------- SyncedSettings
	 *           settings        &gt;       syncedSettings
	 * </pre>
	 */
	private SyncedSettings syncedSettings;

	/**
	 * Returns the synced settings
	 * @return SyncedSettings
	 */
	public SyncedSettings getSyncedSettings() {
		return this.syncedSettings;
	}

	public void setSyncedSettings(SyncedSettings syncedSettings) {
		this.syncedSettings = syncedSettings;
	}

	/**
	 * <pre>
	 *           1     0..*
	 * Settings ------------------------- ModuleManager
	 *           settings        &gt;       moduleManager
	 * </pre>
	 */
	private Set<ModuleManager> moduleManager;

	/**
	 * @return Returns a set of module manager
	 */
	public Set<ModuleManager> getModuleManager() {
		if (this.moduleManager == null) {
			this.moduleManager = new HashSet<ModuleManager>();
		}
		return this.moduleManager;
	}

}

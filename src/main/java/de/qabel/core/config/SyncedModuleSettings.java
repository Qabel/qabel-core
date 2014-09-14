package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class SyncedModuleSettings extends AbstractModuleSettings {
	/**
	 * <pre>
	 *           0..1     0..*
	 * SyncedModuleSettings ------------------------- SyncedSettings
	 *           syncedModuleSettings        &lt;       syncedSettings
	 * </pre>
	 */
	private Set<SyncedSettings> syncedSettings;

	public Set<SyncedSettings> getSyncedSettings() {
		if (this.syncedSettings == null) {
			this.syncedSettings = new HashSet<SyncedSettings>();
		}
		return this.syncedSettings;
	}

	/**
	 * <pre>
	 *           0..*     0..1
	 * SyncedModuleSettings ------------------------- SyncedSettings
	 *           syncedModuleSettings1        &gt;       syncedSettings1
	 * </pre>
	 */
	private SyncedSettings syncedSettings1;

	public void setSyncedSettings1(SyncedSettings value) {
		this.syncedSettings1 = value;
	}

	public SyncedSettings getSyncedSettings1() {
		return this.syncedSettings1;
	}

}

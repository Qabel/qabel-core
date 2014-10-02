package de.qabel.core.config;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class LocalSettings {
	/**
	 * <pre>
	 *           0..1     0..1
	 * LocalSettings ------------------------- Settings
	 *           localSettings        &lt;       settings
	 * </pre>
	 */
	private Settings settings;
	/**
	 * <pre>
	 *           0..1     0..*
	 * LocalSettings ------------------------> LocaleModuleSettings
	 *           localSettings        &gt;       localeModuleSettings
	 * </pre>
	 */
	private Set<LocaleModuleSettings> localeModuleSettings;
	private long pollInterval;
	private Date lastUpdate;
	
	public LocalSettings(Settings settings, long pollInterval, Date lastUpdate) {
		this.setSettings(settings);
		this.setPollInterval(pollInterval);
		this.setLastUpdate(lastUpdate);
	}

	public void setSettings(Settings value) {
		this.settings = value;
	}

	public Settings getSettings() {
		return this.settings;
	}


	public Set<LocaleModuleSettings> getLocaleModuleSettings() {
		if (this.localeModuleSettings == null) {
			this.localeModuleSettings = new HashSet<LocaleModuleSettings>();
		}
		return this.localeModuleSettings;
	}


	public void setPollInterval(long value) {
		this.pollInterval = value;
	}

	public long getPollInterval() {
		return this.pollInterval;
	}


	public void setLastUpdate(Date value) {
		this.lastUpdate = value;
	}

	public Date getLastUpdate() {
		return this.lastUpdate;
	}

}

package de.qabel.core.config;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.annotations.SerializedName;

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
	@SerializedName("poll_interval")
	private long pollInterval;
	@SerializedName("drop_last_update")
	private Date dropLastUpdate;
	
	public LocalSettings(Settings settings, long pollInterval, Date dropLastUpdate) {
		this.setSettings(settings);
		this.setPollInterval(pollInterval);
		this.setdropLastUpdate(dropLastUpdate);
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


	public void setdropLastUpdate(Date value) {
		this.dropLastUpdate = value;
	}

	public Date getLastUpdate() {
		return this.dropLastUpdate;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dropLastUpdate == null) ? 0 : dropLastUpdate.hashCode());
		result = prime
				* result
				+ ((localeModuleSettings == null) ? 0 : localeModuleSettings
						.hashCode());
		result = prime * result + (int) (pollInterval ^ (pollInterval >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalSettings other = (LocalSettings) obj;
		if (dropLastUpdate == null) {
			if (other.dropLastUpdate != null)
				return false;
		} else if (!dropLastUpdate.equals(other.dropLastUpdate))
			return false;
		if (localeModuleSettings == null) {
			if (other.localeModuleSettings != null)
				return false;
		} else if (!localeModuleSettings.equals(other.localeModuleSettings))
			return false;
		if (pollInterval != other.pollInterval)
			return false;
		return true;
	}

}

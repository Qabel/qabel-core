package de.qabel.core.config;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;
/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#local-settings
 */
public class LocalSettings {
	/**
	 * <pre>
	 *           1     0..*
	 * LocalSettings ------------------------> LocaleModuleSettings
	 *           localSettings        &gt;       localeModuleSettings
	 * </pre>
	 * Set of module specific local settings
	 * Field name in serialized json: "module_data"
	 */
	private Set<LocaleModuleSettings> localeModuleSettings;
	/**
	 * Poll interval of the client
	 * Field name in serialized json: "poll_interval"
	 */
	@SerializedName("poll_interval")
	private long pollInterval;
	/**
	 * Date of the last time the core asked the drop servers for new messages
	 * Field name in serialized json: "drop_last_update"
	 */
	@SerializedName("drop_last_update")
	private Date dropLastUpdate;
	/**
	 * Constant string which defines the date format in the serialized json
	 */
	final static String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";
	
	/**
	 * Creates an instance of LocalSettings
	 * @param pollInterval
	 * @param dropLastUpdate
	 */
	public LocalSettings(long pollInterval, Date dropLastUpdate) {
		this.setPollInterval(pollInterval);
		this.setdropLastUpdate(dropLastUpdate);
	}

	/**
	 * Returns a set of module specific local settings
	 * @return Set<LocaleModuleSettings>
	 */
	public Set<LocaleModuleSettings> getLocaleModuleSettings() {
		if (this.localeModuleSettings == null) {
			this.localeModuleSettings = new HashSet<LocaleModuleSettings>();
		}
		return this.localeModuleSettings;
	}

	/**
	 * Sets the poll interval
	 * @param value
	 */
	public void setPollInterval(long value) {
		this.pollInterval = value;
	}

	/**
	 * Returns the poll interval
	 * @return
	 */
	public long getPollInterval() {
		return this.pollInterval;
	}

	/**
	 * Sets the date of last drop update
	 * @param value
	 */
	public void setdropLastUpdate(Date value) {
		this.dropLastUpdate = value;
	}

	/**
	 * Returns the date of last drop update
	 * @return Date
	 */
	public Date getLastUpdate() {
		return this.dropLastUpdate;
	}

	/**
	 * Serializes this class to a Json String
	 * @return Json String
	 * @throws IOException
	 */
	public String toJson() throws IOException {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(dateFormat);
		Gson gson = builder.create();
		TypeAdapter<LocalSettings> adapter = gson.getAdapter(LocalSettings.class);
		return adapter.toJson(this);
	}

	/**
	 * Deserializes a Json String
	 * @param json
	 * @return LocalSettings
	 * @throws IOException
	 * @throws JsonParseException
	 */
	public static LocalSettings fromJson(String json) throws IOException, JsonParseException {
		GsonBuilder builder = new GsonBuilder();
		builder.setDateFormat(dateFormat);
		Gson gson = builder.create();
		TypeAdapter<LocalSettings> adapter = gson.getAdapter(LocalSettings.class);
		return adapter.fromJson(json);
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

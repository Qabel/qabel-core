package de.qabel.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.TypeAdapter;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.Date;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#local-settings
 */
public class LocalSettings extends Persistable {
    private static final long serialVersionUID = 354451411302690221L;

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
    static final String dateFormat = "yyyy-MM-dd HH:mm:ss.SSS";

    /**
     * Creates an instance of LocalSettings.
     *
     * @param pollInterval   Poll interval of the client.
     * @param dropLastUpdate Date of the last time the core asked the drop servers for new messages.
     */
    public LocalSettings(long pollInterval, Date dropLastUpdate) {
        setPollInterval(pollInterval);
        setdropLastUpdate(dropLastUpdate);
    }

    /**
     * Sets the poll interval.
     *
     * @param value Value to set the poll interval to.
     */
    public void setPollInterval(long value) {
        pollInterval = value;
    }

    /*
     * @return Returns the poll interval.
     */
    public long getPollInterval() {
        return pollInterval;
    }

    /**
     * Sets the date of last drop update.
     *
     * @param value Date of the last time the core asked the drop servers for new messages.
     */
    public void setdropLastUpdate(Date value) {
        dropLastUpdate = value;
    }

    /**
     * Returns the date of last drop update
     *
     * @return Date
     */
    public Date getLastUpdate() {
        return dropLastUpdate;
    }

    /**
     * Serializes this class to a Json String.
     *
     * @return Json String
     */
    public String toJson() throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setDateFormat(dateFormat);
        Gson gson = builder.create();
        TypeAdapter<LocalSettings> adapter = gson.getAdapter(LocalSettings.class);
        return adapter.toJson(this);
    }

    /**
     * Deserializes a Json String.
     *
     * @param json Json String to deserialize.
     * @return LocalSettings
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
            + (dropLastUpdate == null ? 0 : dropLastUpdate.hashCode());
        result = prime * result + (int) (pollInterval ^ pollInterval >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LocalSettings other = (LocalSettings) obj;
        if (dropLastUpdate == null) {
            if (other.dropLastUpdate != null) {
                return false;
            }
        } else if (!dropLastUpdate.equals(other.dropLastUpdate)) {
            return false;
        }
        return pollInterval == other.pollInterval;
    }

}

package de.qabel.core.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParseException
import com.google.gson.TypeAdapter
import com.google.gson.annotations.SerializedName

import java.io.IOException
import java.util.Date

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#local-settings
 */
class LocalSettings
/**
 * Creates an instance of LocalSettings.

 * @param pollInterval   Poll interval of the client.
 * *
 * @param dropLastUpdate Date of the last time the core asked the drop servers for new messages.
 */
(pollInterval: Long, dropLastUpdate: Date) : Persistable() {

    /**
     * Poll interval of the client
     * Field name in serialized json: "poll_interval"
     */
    /*
     * @return Returns the poll interval.
     */
    /**
     * Sets the poll interval.

     * @param value Value to set the poll interval to.
     */
    @SerializedName("poll_interval")
    var pollInterval: Long = 0
    /**
     * Date of the last time the core asked the drop servers for new messages
     * Field name in serialized json: "drop_last_update"
     */
    /**
     * Returns the date of last drop update

     * @return Date
     */
    @SerializedName("drop_last_update")
    var lastUpdate: Date? = null
        private set

    init {
        pollInterval = pollInterval
        setdropLastUpdate(dropLastUpdate)
    }

    /**
     * Sets the date of last drop update.

     * @param value Date of the last time the core asked the drop servers for new messages.
     */
    fun setdropLastUpdate(value: Date) {
        lastUpdate = value
    }

    /**
     * Serializes this class to a Json String.

     * @return Json String
     */
    @Throws(IOException::class)
    fun toJson(): String {
        val builder = GsonBuilder()
        builder.setDateFormat(dateFormat)
        val gson = builder.create()
        val adapter = gson.getAdapter(LocalSettings::class.java)
        return adapter.toJson(this)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (lastUpdate == null) 0 else lastUpdate!!.hashCode()
        result = prime * result + (pollInterval xor pollInterval.ushr(32)).toInt()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as LocalSettings?
        if (lastUpdate == null) {
            if (other!!.lastUpdate != null) {
                return false
            }
        } else if (lastUpdate != other!!.lastUpdate) {
            return false
        }
        return pollInterval == other.pollInterval
    }

    companion object {
        private val serialVersionUID = 354451411302690221L
        /**
         * Constant string which defines the date format in the serialized json
         */
        internal val dateFormat = "yyyy-MM-dd HH:mm:ss.SSS"

        /**
         * Deserializes a Json String.

         * @param json Json String to deserialize.
         * *
         * @return LocalSettings
         */
        @Throws(IOException::class, JsonParseException::class)
        fun fromJson(json: String): LocalSettings {
            val builder = GsonBuilder()
            builder.setDateFormat(dateFormat)
            val gson = builder.create()
            val adapter = gson.getAdapter(LocalSettings::class.java)
            return adapter.fromJson(json)
        }
    }

}

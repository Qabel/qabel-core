package de.qabel.core.config

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#settings-client-notes
 */
class Settings {

    /**
     * Returns the local settings

     * @return localSettings
     */
    /**
     * Sets the local settings

     * @param value Value to set the local settings to.
     */
    var localSettings: LocalSettings? = null

    /**
     *
     * 1     1
     * Settings ------------------------- SyncedSettings
     * settings        &gt;       syncedSettings
     *
     */
    /**
     * Returns the synced settings

     * @return SyncedSettings
     */
    var syncedSettings: SyncedSettings? = null
}

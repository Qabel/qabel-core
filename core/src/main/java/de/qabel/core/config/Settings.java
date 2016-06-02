package de.qabel.core.config;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#settings-client-notes
 */
public class Settings {

    private LocalSettings localSettings;

    /**
     * Sets the local settings
     *
     * @param value Value to set the local settings to.
     */
    public void setLocalSettings(LocalSettings value) {
        localSettings = value;
    }

    /**
     * Returns the local settings
     *
     * @return localSettings
     */
    public LocalSettings getLocalSettings() {
        return localSettings;
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
     *
     * @return SyncedSettings
     */
    public SyncedSettings getSyncedSettings() {
        return syncedSettings;
    }

    public void setSyncedSettings(SyncedSettings syncedSettings) {
        this.syncedSettings = syncedSettings;
    }
}

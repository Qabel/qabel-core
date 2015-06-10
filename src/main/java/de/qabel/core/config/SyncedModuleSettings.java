package de.qabel.core.config;

/**
 * Module settings that should be synchronized over multiple Qabel installations
 * should be grouped in a module-defined settings class that inherits from this
 * class.
 * 
 * @see LocaleModuleSettings
 */
public abstract class SyncedModuleSettings extends AbstractModuleSettings {
	private static final long serialVersionUID = 7632514690237416721L;
}

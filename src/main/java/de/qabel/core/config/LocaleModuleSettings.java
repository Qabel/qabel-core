package de.qabel.core.config;

/**
 * Module settings that don't need to be synchronized over multiple Qabel installations
 * and therefore can be kept locally should be grouped in a module-defined settings class
 * that inherits from this class.
 * 
 * @see SyncedModuleSettings
 */
public abstract class LocaleModuleSettings extends AbstractModuleSettings {
	private static final long serialVersionUID = -9126876223444231399L;

}

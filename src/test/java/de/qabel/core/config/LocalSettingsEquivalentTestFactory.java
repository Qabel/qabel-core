package de.qabel.core.config;

import java.util.Date;

import org.meanbean.lang.EquivalentFactory;

/**
 * LocalSettingsEquivalentTestFactory
 * Creates logically equivalent instances of class LocalSettings
 * Attention: For testing purposes only
 */
class LocalSettingsEquivalentTestFactory implements EquivalentFactory<LocalSettings>{
	Date dropLastUpdate;

	LocalSettingsEquivalentTestFactory() {
		dropLastUpdate = new Date(System.currentTimeMillis());
	}

	@Override
	public LocalSettings create() {
		return new LocalSettings(100, dropLastUpdate);
	}

}

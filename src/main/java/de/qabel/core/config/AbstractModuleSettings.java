package de.qabel.core.config;

public abstract class AbstractModuleSettings {
	private String type;

	protected AbstractModuleSettings() {
		this.type = this.getClass().getName();
	}

	public String getType() {
		return type;
	}
}

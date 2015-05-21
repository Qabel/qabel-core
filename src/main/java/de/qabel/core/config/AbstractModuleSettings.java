package de.qabel.core.config;

import java.io.Serializable;

public abstract class AbstractModuleSettings extends Persistable implements Serializable {
	private static final long serialVersionUID = -2650189278846561182L;
	private String type;

	protected AbstractModuleSettings() {
		this.type = this.getClass().getName();
	}

	public String getType() {
		return type;
	}
}

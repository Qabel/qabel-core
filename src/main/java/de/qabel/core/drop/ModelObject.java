package de.qabel.core.drop;


import java.io.Serializable;

public abstract class ModelObject implements Serializable {
	public <T extends ModelObject> T as(Class<T> cls) {
		return cls.cast(this); 
	}
}

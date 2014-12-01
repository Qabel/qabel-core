package de.qabel.core.drop;


public abstract class ModelObject {
	public <T extends ModelObject> T as(Class<T> cls) {
		return cls.cast(this); 
	}
}

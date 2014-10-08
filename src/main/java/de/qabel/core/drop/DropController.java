package de.qabel.core.drop;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class DropController {

	/**
	 * <pre>
	 *           0..*     0..*
	 * DropController ------------------------> DropListener
	 *           dropController        &gt;       dropListener
	 * </pre>
	 */
	private Map<Class<? extends ModelObject>, ArrayList<DropListener>> dropListeners;

	public DropController() {
		this.dropListeners = new HashMap<Class<? extends ModelObject>, ArrayList<DropListener>>();
	}

	public boolean register(ModelObject modelObject, DropListener dropListener) {
		ArrayList<DropListener> ar = dropListeners.get(modelObject.getClass());
		if (ar == null) {
			ar = new ArrayList<DropListener>();
			dropListeners.put(modelObject.getClass(), ar);
		}
		ar.add(dropListener);
		return true;
	}

	public void handleDrop(DropMessage<? extends ModelObject> dm) {
		for (DropListener dl : dropListeners.get(dm.getModelObject())) {
			dl.onDropEvent((DropMessage<ModelObject>) dm);
		}
	}
}

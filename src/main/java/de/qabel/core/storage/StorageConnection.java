package de.qabel.core.storage;

import de.qabel.core.module.ModuleManager;

import java.util.Set;
import java.util.HashSet;

public class StorageConnection {
/**
 * <pre>
 *           0..1     0..1
 * StorageConnection ------------------------- StorageResponseListener
 *           storageConnection        &lt;       storageResponseListener
 * </pre>
 */
private StorageResponseListener storageResponseListener;

public void setStorageResponseListener(StorageResponseListener value) {
   this.storageResponseListener = value;
}

public StorageResponseListener getStorageResponseListener() {
   return this.storageResponseListener;
}

public void cancel() {
   // TODO implement this operation
   throw new UnsupportedOperationException("not implemented");
}

	/**
	 * <pre>
	 *           0..*     0..*
	 * StorageConnection ------------------------- ModuleManager
	 *           storageConnection        &gt;       moduleManager
	 * </pre>
	 */
	private Set<ModuleManager> moduleManager;

	public Set<ModuleManager> getModuleManager() {
		if (this.moduleManager == null) {
			this.moduleManager = new HashSet<ModuleManager>();
		}
		return this.moduleManager;
	}

}

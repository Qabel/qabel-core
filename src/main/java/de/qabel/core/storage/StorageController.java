package de.qabel.core.storage;

import java.util.HashSet;
import java.util.Set;

public class StorageController {
	/**
	 * <pre>
	 *           1..1     0..*
	 * StorageController ------------------------> StorageConnection
	 *           &lt;       storageConnection
	 * </pre>
	 */
	// TODO implement this operation
public StorageResponseListener startRequest(StorageResponseListener listener) {
   // TODO implement this operation
   throw new UnsupportedOperationException("not implemented");
}


	private Set<StorageConnection> storageConnection;

	public Set<StorageConnection> getStorageConnection() {
		if (this.storageConnection == null) {
			this.storageConnection = new HashSet<StorageConnection>();
		}
		return this.storageConnection;
	}

}

package de.qabel.core.config;

public class StorageServer {
	/**
	 * <pre>
	 *           0..*     0..1
	 * StorageServer ------------------------- StorageServers
	 *           storageServer        &lt;       storageServers
	 * </pre>
	 */
	private StorageServers storageServers;

	public void setStorageServers(StorageServers value) {
		this.storageServers = value;
	}

	public StorageServers getStorageServers() {
		return this.storageServers;
	}

}

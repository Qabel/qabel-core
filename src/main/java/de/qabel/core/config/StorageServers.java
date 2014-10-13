package de.qabel.core.config;

import java.util.Set;
import java.util.HashSet;

public class StorageServers {
	/**
	 * <pre>
	 *           0..1     0..*
	 * StorageServers ------------------------- StorageServer
	 *           storageServers        &gt;       storageServer
	 * </pre>
	 */
	private Set<StorageServer> storageServer;

	public Set<StorageServer> getStorageServer() {
		if (this.storageServer == null) {
			this.storageServer = new HashSet<StorageServer>();
		}
		return this.storageServer;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((storageServer == null) ? 0 : storageServer.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		StorageServers other = (StorageServers) obj;
		if (storageServer == null) {
			if (other.storageServer != null)
				return false;
		} else if (!storageServer.equals(other.storageServer))
			return false;
		return true;
	}

	
}

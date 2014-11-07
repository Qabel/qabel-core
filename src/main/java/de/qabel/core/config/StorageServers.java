package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class StorageServers {
	/**
	 * <pre>
	 *           1     0..*
	 * StorageServers ------------------------- StorageServer
	 *           storageServers        &gt;       storageServer
	 * </pre>
	 */
	private final Set<StorageServer> storageServers = new HashSet<StorageServer>();

	public Set<StorageServer> getStorageServers() {
		return Collections.unmodifiableSet(this.storageServers);
	}
	
	public boolean add(StorageServer storageServer) {
		return this.storageServers.add(storageServer);
	}

	/**
	 * Removes storageServer from list of storageServers
	 * @param storageServer
	 * @return true if storageServer was contained in list, false if not
	 */
	public boolean remove(StorageServer storageServer) {
		return this.storageServers.remove(storageServer);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((storageServers == null) ? 0 : storageServers.hashCode());
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
		if (storageServers == null) {
			if (other.storageServers != null)
				return false;
		} else if (!storageServers.equals(other.storageServers))
			return false;
		return true;
	}

	
}

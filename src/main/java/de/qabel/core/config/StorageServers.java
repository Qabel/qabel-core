package de.qabel.core.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#storage-servers
 */
public class StorageServers {

	private final Map<String, StorageServer> storageServers = new HashMap<>();

	/**
	 * Returns an unmodifiable set of contained storage servers
	 * @return Set<StorageServer>
	 */
	public Set<StorageServer> getStorageServers() {
		return Collections.unmodifiableSet(new HashSet<>(this.storageServers.values()));
	}
	
	protected StorageServer getStorageServerByUrl(String serverUrl) {
		return this.storageServers.get(serverUrl);
	}
	
	/**
	 * Put a storage server
	 * @param storageServer StorageServer to put.
	 * @return true if newly added, false if updated
	 */
	public boolean put(StorageServer storageServer) {
		if (this.storageServers.put(storageServer.getUrl().toString(), storageServer) == null) {
			return true;
		}
		return false;
	}

	/**
	 * Removes storageServer from list of storageServers
	 * @param storageServer StorageServer to remove.
	 * @return true if storageServer was contained in list, false if not
	 */
	public boolean remove(StorageServer storageServer) {
		return storageServer != null
				&& this.storageServers.remove(storageServer.getUrl().toString()) != null;
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

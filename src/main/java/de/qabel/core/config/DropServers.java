package de.qabel.core.config;

import java.util.*;

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#drop-servers
 */
public class DropServers {
	/**
	 * <pre>
	 *           1     0..*
	 * DropServers ------------------------- DropServer
	 *           dropServers        &gt;       dropServer
	 * </pre>
	 */
	private final Map<String, DropServer> dropServers = new HashMap<>();

	/**
	 * @return Returns unmodifiable set of contained drop servers
	 */
	public Set<DropServer> getDropServers() {
		return Collections.unmodifiableSet(new HashSet<>(this.dropServers.values()));
	}
	
	/**
	 * Put a drop server.
	 * @param dropServer DropServer to put.
	 * @return True if newly added, false if updated
	 */
	public boolean put(DropServer dropServer) {
		if (this.dropServers.put(dropServer.getPersistenceID(), dropServer) == null) {
			return true;
		}
		return false;
	}

	/**
	 * Removes dropServer from list of dropServers
	 * @param dropServer DropServer to remove.
	 * @return true if dropServer was contained in list, false if not.
	 */
	public boolean remove(DropServer dropServer) {
		if (this.dropServers.remove(dropServer.getPersistenceID()) == null) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dropServers == null) ? 0 : dropServers.hashCode());
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
		DropServers other = (DropServers) obj;
		if (dropServers == null) {
			if (other.dropServers != null)
				return false;
		} else if (!dropServers.equals(other.dropServers))
			return false;
		return true;
	}

	
}

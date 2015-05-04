package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

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
	private final Set<DropServer> dropServers = new HashSet<DropServer>();

	/**
	 * @return Returns unmodifiable set of contained drop servers
	 */
	public Set<DropServer> getDropServers() {
		return Collections.unmodifiableSet(this.dropServers);
	}
	
	/**
	 * Adds a drop server.
	 * @param dropServer DropServer to add.
	 * @return true if successfully added, false if already contained.
	 */
	public boolean add(DropServer dropServer) {
		return this.dropServers.add(dropServer);
	}

	/**
	 * Removes dropServer from list of dropServers
	 * @param dropServer DropServer to remove.
	 * @return true if dropServer was contained in list, false if not.
	 */
	public boolean remove(DropServer dropServer) {
		return this.dropServers.remove(dropServer);
	}

	public boolean update(DropServer dropServer) {
		this.remove(dropServer);
		return this.add(dropServer);
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

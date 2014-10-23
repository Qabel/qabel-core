package de.qabel.core.config;

import java.util.Collections;
import java.util.Set;
import java.util.HashSet;

public class DropServers {
	/**
	 * <pre>
	 *           1     0..*
	 * DropServers ------------------------- DropServer
	 *           dropServers        &gt;       dropServer
	 * </pre>
	 */
	private final Set<DropServer> dropServer = new HashSet<DropServer>();

	public Set<DropServer> getDropServer() {
		return Collections.unmodifiableSet(this.dropServer);
	}
	
	public boolean add(DropServer dropServer) {
		return this.dropServer.add(dropServer);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((dropServer == null) ? 0 : dropServer.hashCode());
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
		if (dropServer == null) {
			if (other.dropServer != null)
				return false;
		} else if (!dropServer.equals(other.dropServer))
			return false;
		return true;
	}

	
}

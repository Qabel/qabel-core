package de.qabel.core.config;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public abstract class Persistable implements Serializable {

	private static final Set<UUID> uuids = new HashSet<>();

	private UUID persistenceID;

	public Persistable() {
		this.persistenceID = genPersistenceID();
	}

	public String getPersistenceID() { return persistenceID.toString(); }

	/**
	 * Generated a random UUID for persistent storage. It ensures that Entities are using a unique UUID.
	 * @return Unique UUID
	 */
	private UUID genPersistenceID(){
		UUID uuid = UUID.randomUUID();
		while (uuids.contains(uuid)) {
			uuid = UUID.randomUUID();
		}
		uuids.add(uuid);
		return uuid;
	}
}

package de.qabel.core.config

import java.util.*

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#drop-servers
 */
class DropServers {
    /**
     *
     * 1     0..*
     * DropServers ------------------------- DropServer
     * dropServers        &gt;       dropServer
     *
     */
    private val dropServers = HashMap<String, DropServer>()

    /**
     * @return Returns unmodifiable set of contained drop servers
     */
    fun getDropServers(): Set<DropServer> {
        return Collections.unmodifiableSet(HashSet(dropServers.values))
    }

    /**
     * Put a drop server.

     * @param dropServer DropServer to put.
     * *
     * @return True if newly added, false if updated
     */
    fun put(dropServer: DropServer): Boolean {
        return dropServers.put(dropServer.persistenceID, dropServer) == null
    }

    /**
     * Removes dropServer from list of dropServers

     * @param dropServer DropServer to remove.
     * *
     * @return true if dropServer was contained in list, false if not.
     */
    fun remove(dropServer: DropServer): Boolean {
        return dropServers.remove(dropServer.persistenceID) != null
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        result = prime * result + if (dropServers == null) 0 else dropServers.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as DropServers?
        if (dropServers == null) {
            if (other!!.dropServers != null) {
                return false
            }
        } else if (dropServers != other!!.dropServers) {
            return false
        }
        return true
    }


}

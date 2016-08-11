package de.qabel.core.config

/**
 * https://github.com/Qabel/qabel-doc/wiki/Qabel-Client-Configuration#identities
 */

class Identities : EntityMap<Identity>() {

    /**
     * Get an unmodifiable list of stored instances of Identity

     * @return unmodifiable set of Identity
     */
    val identities: Set<Identity>
        get() = entities

    companion object {
        private val serialVersionUID = -1644016734454696766L
    }
}

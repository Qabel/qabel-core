package de.qabel.core.config

import java.io.Serializable
import java.util.UUID

/**
 * Persistable manages a unique persistence ID for objects that
 * have to be persistable.
 */
abstract class Persistable : Serializable {
    val persistenceID: String by lazy { UUID.randomUUID().toString() }

}

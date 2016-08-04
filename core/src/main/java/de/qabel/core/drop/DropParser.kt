package de.qabel.core.drop

import de.qabel.core.config.Identities

interface DropParser {
    fun parse(message: ByteArray, receivers: Identities): DropMessage?
}



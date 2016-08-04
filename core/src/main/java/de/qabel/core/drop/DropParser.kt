package de.qabel.core.drop

import de.qabel.core.config.Identities
import de.qabel.core.config.Identity

interface DropParser {
    fun parse(message: ByteArray, receivers: Identities): Pair<Identity, DropMessage>
}



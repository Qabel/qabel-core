package de.qabel.core.drop

import de.qabel.core.config.Identities
import de.qabel.core.config.Identity
import de.qabel.core.crypto.BinaryDropMessageV0
import de.qabel.core.exceptions.*

class DefaultDropParser: DropParser {

    @Throws(QblException::class)
    override fun parse(message: ByteArray, receivers: Identities): Pair<Identity, DropMessage> {
        val binaryFormatVersion = message[0]
        if (binaryFormatVersion != 0.toByte()) {
            throw QblUnkownVersionException()
        }
        val binaryMessage = BinaryDropMessageV0(message)
        receivers.identities.forEach { identity ->
           binaryMessage.disassembleMessage(identity)?.let {
               return Pair(identity, it)
           }
        }
        throw QblDropParseException()
    }

}



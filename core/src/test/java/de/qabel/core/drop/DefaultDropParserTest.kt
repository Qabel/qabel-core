package de.qabel.core.drop

import de.qabel.core.config.Identities
import de.qabel.core.config.IdentityTestFactory
import de.qabel.core.crypto.BinaryDropMessageV0
import org.junit.Test

import org.junit.Assert.*

class DefaultDropParserTest {

    @Test
    fun parse() {
        val sender = IdentityTestFactory().create()
        val receiver = IdentityTestFactory().create()
        val dropMessage = DropMessage(sender, "payload", "text")
        val msg = BinaryDropMessageV0(dropMessage)
            .assembleMessageFor(receiver.toContact(), sender)
        val parser = DefaultDropParser()
        val (identity, parsedMessage) = parser.parse(msg, Identities().apply { put(receiver) })
        assertEquals(identity.keyIdentifier, receiver.keyIdentifier)
        assertEquals(dropMessage.senderKeyId, parsedMessage.senderKeyId)
        assertEquals(dropMessage.dropPayload, parsedMessage.dropPayload)
    }

}

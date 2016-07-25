package de.qabel.core.http

import de.qabel.core.config.factory.DropUrlGenerator
import de.qabel.core.config.factory.IdentityBuilder
import de.qabel.core.drop.DropMessage
import de.qabel.core.extensions.toContact
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.DropState
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.equalTo
import org.junit.Assert.assertThat
import org.junit.Test

class DropServerTest {

    val dropGenerator = DropUrlGenerator("http://localhost")
    val identityA = IdentityBuilder(dropGenerator).withAlias("identityA").build()
    val contactA = identityA.toContact()
    val identityB = IdentityBuilder(dropGenerator).withAlias("identityB").build()
    val contactB = identityA.toContact()

    private fun createTextPayload(text: String) = "{\"msg\": \"$text\"}"

    @Test
    fun testSend() {
        val message = DropMessage(identityA, createTextPayload("test213"), ChatDropMessage.MessageType.BOX_MESSAGE.type)
        val dropServer = DropServer()
        val targetDrop = identityB.dropUrls.first()
        dropServer.sendDropMessage(identityA, contactB, message, targetDrop)

        val result = dropServer.receiveDropMessages(identityB, targetDrop, DropState(targetDrop.toString()))
        assertThat(result.dropMessages, contains(message))
        assertThat(result.statusCode, equalTo(DropServer.QblStatusCodes.OK))
    }

    @Test
    fun testReceiveMessages() {
        val messages = listOf(DropMessage(identityA, createTextPayload("message 1"), ChatDropMessage.MessageType.BOX_MESSAGE.type),
            DropMessage(identityA, createTextPayload("message 2"), ChatDropMessage.MessageType.BOX_MESSAGE.type),
            DropMessage(identityA, createTextPayload("message 3"), ChatDropMessage.MessageType.BOX_MESSAGE.type))
        val dropServer = DropServer()
        val targetDrop = identityB.dropUrls.first()
        messages.forEach { dropServer.sendDropMessage(identityA, contactB, it, targetDrop) }

        val result = dropServer.receiveDropMessages(identityB, targetDrop, DropState(targetDrop.toString()))
        assertThat(result.dropMessages, contains(*messages.toTypedArray()))
    }
}

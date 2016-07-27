package de.qabel.core.repository.entities

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test

class ChatDropMessageTest {

    var textPayload = "{msg:\"This is a test message.\"}"
    var shareMessagePayload = "{msg:\"This is a shareMessage.\", url:\"http://test.qabel.de\", key:[" + "thisisthekey".toByteArray().joinToString(",") + "] }"

    @Test
    fun testTextPayload() {
        val payload = ChatDropMessage(0, 0, ChatDropMessage.Direction.INCOMING,
            ChatDropMessage.Status.NEW, ChatDropMessage.MessageType.BOX_MESSAGE, textPayload, 0).payload

        assertThat(payload as ChatDropMessage.MessagePayload.TextMessage, notNullValue())
        assertThat(payload.msg, equalTo("This is a test message."))
    }

    @Test
    fun testSharePayload() {
        val payload = ChatDropMessage(0, 0, ChatDropMessage.Direction.INCOMING,
            ChatDropMessage.Status.NEW, ChatDropMessage.MessageType.SHARE_NOTIFICATION, shareMessagePayload, 0).payload

        assertThat(payload as ChatDropMessage.MessagePayload.ShareMessage, notNullValue())
        assertThat(payload.msg, equalTo("This is a shareMessage."))
        assertThat(payload.getUrl().toString(), equalTo("http://test.qabel.de"))
        assertThat(payload.getKey().byteList, equalTo("thisisthekey".toByteArray().toList()))
    }
}

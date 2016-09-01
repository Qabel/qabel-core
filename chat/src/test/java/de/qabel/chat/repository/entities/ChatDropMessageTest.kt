package de.qabel.chat.repository.entities

import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.notNullValue
import org.junit.Assert.assertThat
import org.junit.Test
import org.spongycastle.util.encoders.Hex

class ChatDropMessageTest {

    var textPayload = "{msg:\"This is a test message.\"}"
    var shareMessagePayload = "{msg:\"This is a shareMessage.\", file:\"testfile.png\", " +
        "url:\"http://test.qabel.de\", key:\"" + Hex.toHexString("thisisthekey".toByteArray()) + "\", " +
        "size:\"300\" }"

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
        assertThat(payload.shareData.metaUrl.toString(), equalTo("http://test.qabel.de"))
        assertThat(payload.shareData.metaKey.byteList, equalTo("thisisthekey".toByteArray().toList()))
        assertThat(payload.shareData.name, equalTo("testfile.png"))
        assertThat(payload.shareData.size, equalTo(300L))
    }
}

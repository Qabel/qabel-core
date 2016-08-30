package de.qabel.chat.repository.entities

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import de.qabel.core.config.SymmetricKey
import de.qabel.core.extensions.getString
import org.spongycastle.util.encoders.Hex
import java.net.URI

object PayloadSerializer {

    private val MESSAGE = "msg"
    private val FILE_NAME = "file"
    private val KEY = "key"
    private val URL = "url"
    private val SIZE = "size"

    private val jsonSerializer = jsonSerializer<ChatDropMessage.MessagePayload.ShareMessage> {
        JsonObject().apply {
            it.src.let {
                set(MESSAGE, it.msg)
                set(FILE_NAME, it.fileName)
                set(URL, it.url.toString())
                set(KEY, Hex.toHexString(it.key.byteList.toByteArray()))
                set(SIZE, it.size)
            }
        }
    }

    private val jsonDeserializer: JsonDeserializer<ChatDropMessage.MessagePayload.ShareMessage> = jsonDeserializer {
        it.json.obj.let {
            ChatDropMessage.MessagePayload.ShareMessage(
                it.getString(MESSAGE),
                URI(it.getString(URL)),
                SymmetricKey.Factory.fromHex(it.getString(KEY)),
                it.get(FILE_NAME).nullString ?: "",
                it.get(SIZE).nullLong ?: 0)
        }
    }

    fun gson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter<ChatDropMessage.MessagePayload.ShareMessage>(jsonSerializer)
        .registerTypeAdapter<ChatDropMessage.MessagePayload.ShareMessage>(jsonDeserializer)
        .create()
}

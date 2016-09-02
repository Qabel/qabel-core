package de.qabel.chat.repository.entities

import com.github.salomonbrys.kotson.*
import com.google.gson.*
import de.qabel.chat.repository.entities.ChatDropMessage.MessagePayload.ShareMessage
import de.qabel.core.config.SymmetricKey
import de.qabel.core.extensions.getString
import de.qabel.core.extensions.letApply
import org.spongycastle.util.encoders.Hex
import java.net.URI

object PayloadSerializer {

    private val MESSAGE = "msg"
    private val FILE_NAME = "file"
    private val KEY = "key"
    private val URL = "url"
    private val SIZE = "size"

    private val jsonSerializer = jsonSerializer<ShareMessage> {
        JsonObject().apply {
            it.src.let {
                set(MESSAGE, it.msg)
                set(FILE_NAME, it.shareData.name)
                set(URL, it.shareData.metaUrl.toString())
                set(KEY, it.shareData.metaKey.toHexString())
                set(SIZE, it.shareData.size)
            }
        }
    }

    private val jsonDeserializer: JsonDeserializer<ShareMessage> = jsonDeserializer {
        it.json.obj.let {
            val shareData = BoxFileChatShare(
                ShareStatus.NEW,
                it.get(FILE_NAME).nullString ?: "",
                it.get(SIZE).nullLong ?: 0,
                SymmetricKey.Factory.fromHex(it.getString(KEY)),
                it.getString(URL))

            ShareMessage(it.getString(MESSAGE), shareData)
        }
    }

    fun gson(): Gson = GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter<ShareMessage>(jsonSerializer)
        .registerTypeAdapter<ShareMessage>(jsonDeserializer)
        .create()
}

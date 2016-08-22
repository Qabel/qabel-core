package de.qabel.core.drop


import com.google.gson.*
import de.qabel.core.config.Identity
import de.qabel.core.extensions.getInt
import de.qabel.core.extensions.getLong
import de.qabel.core.extensions.getString
import de.qabel.core.extensions.safeObject

import java.lang.reflect.Type
import java.util.Date

class DropSerializer : JsonSerializer<DropMessage>, JsonDeserializer<DropMessage> {

    companion object {
        const val META_DATA = "meta_data"
        private const val VERSION = "version"
        private const val TIME_STAMP = "time_stamp"
        private const val SENDER = "sender"
        private const val PAYLOAD_TYPE = "drop_payload_type"
        private const val PAYLOAD = "drop_payload"
        private const val ACK_ID = "acknowledge_id"
    }

    override fun serialize(src: DropMessage, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonObject().apply {
            addProperty(VERSION, DropMessage.getVersion())
            addProperty(TIME_STAMP, src.creationDate.time)
            addProperty(SENDER, src.sender.keyIdentifier)
            add(PAYLOAD, context.serialize(src.dropPayload))
            add(PAYLOAD_TYPE, context.serialize(src.dropPayloadType))
            add(ACK_ID, context.serialize(src.acknowledgeID))

            src.dropMessageMetadata?.let {
                add(META_DATA, context.serialize(it))
            }
        }

    override fun deserialize(json: JsonElement, type: Type,
                             context: JsonDeserializationContext): DropMessage =
        with(json.safeObject()) {
            val version = getInt(VERSION)
            if (version != DropMessage.getVersion()) {
                throw JsonParseException("Unexpected version: " + version)
            }
            val time = getLong(TIME_STAMP)
            val sender = getString(SENDER)
            val dropPayload = getString(PAYLOAD)
            val dropPayloadType = getString(PAYLOAD_TYPE)

            var dropMessageMetadata: DropMessageMetadata? = null
            if (has(META_DATA)) {
                dropMessageMetadata = context.deserialize<DropMessageMetadata>(
                    get(META_DATA), DropMessageMetadata::class.java)
            }

            DropMessage(sender, dropPayload, dropPayloadType, Date(time), DropMessage.NOACK, dropMessageMetadata)
        }
}

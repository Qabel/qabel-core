package de.qabel.core.drop


import com.google.gson.*
import de.qabel.core.config.Identity

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
    }

    override fun serialize(src: DropMessage, typeOfSrc: Type, context: JsonSerializationContext): JsonElement =
        JsonObject().apply {
            addProperty(VERSION, DropMessage.getVersion())
            addProperty(TIME_STAMP, src.creationDate.time)
            addProperty(SENDER, src.sender.keyIdentifier)
            add(PAYLOAD, context.serialize(src.dropPayload))
            add(PAYLOAD_TYPE, context.serialize(src.dropPayloadType))

            src.dropMessageMetadata?.let {
                add(META_DATA, context.serialize(it))
            }
        }

    override fun deserialize(json: JsonElement, type: Type,
                             context: JsonDeserializationContext): DropMessage =
        json.asJsonObject?.let {
            val version = it.get(VERSION)?.asInt ?: throw JsonSyntaxException("version missing")
            if (version != DropMessage.getVersion()) {
                throw JsonParseException("Unexpected version: " + version)
            }
            val time = it.get(TIME_STAMP)?.asLong ?: throw JsonSyntaxException("time is not a long")
            val sender = it.get(SENDER)?.asString ?: throw JsonSyntaxException("sender not defined")
            val dropPayload = it.get(PAYLOAD)?.asString ?: throw JsonSyntaxException("payload not defined")
            val dropPayloadType = it.get(PAYLOAD_TYPE)?.asString ?: throw JsonSyntaxException("payload type not defined")

            var dropMessageMetadata: DropMessageMetadata? = null
            if (it.has(META_DATA)) {
                dropMessageMetadata = context.deserialize<DropMessageMetadata>(
                    it.get(META_DATA), DropMessageMetadata::class.java)
            }

            DropMessage(sender, dropPayload, dropPayloadType, Date(time), DropMessage.NOACK, dropMessageMetadata)
        } ?: throw JsonSyntaxException("Not a json object")
}

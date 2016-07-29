package de.qabel.core.drop

import com.google.gson.*
import de.qabel.core.crypto.QblECPublicKey
import java.lang.reflect.Type


class DropMessageMetaDataSerializer : JsonSerializer<DropMessageMetadata>, JsonDeserializer<DropMessageMetadata> {

    companion object {
        private const val ALIAS = "alias"
        private const val PUBLIC_KEY = "public_key"
        private const val DROP_URL = "drop_url"
        private const val EMAIL = "email"
        private const val PHONE = "phone"
    }

    override fun serialize(src: DropMessageMetadata, typeOfSrc: Type?, context: JsonSerializationContext): JsonElement =
        JsonObject().apply {
            addProperty(ALIAS, src.alias)
            add(DROP_URL, context.serialize(src.dropUrl))
            add(PUBLIC_KEY, context.serialize(src.publicKey))
            addProperty(PHONE, src.phone)
            addProperty(EMAIL, src.email)
        }

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): DropMessageMetadata =
        with(json.asJsonObject) {
            DropMessageMetadata(get(ALIAS).asString,
                context.deserialize(get(PUBLIC_KEY), QblECPublicKey::class.java),
                context.deserialize(get(DROP_URL), DropURL::class.java),
                get(EMAIL)?.asString ?: "",
                get(PHONE)?.asString ?: "")
        }

}

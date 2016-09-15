package de.qabel.core.index

import com.github.salomonbrys.kotson.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.drop.DropURL
import java.util.*


/**
 * Data class representing a public identity in the index.
 */
data class IndexContact(
    val publicKey: QblECPublicKey,
    val dropUrl: DropURL,
    val alias: String
) {
    fun toContact(): Contact {
        return Contact(alias, listOf(dropUrl), publicKey)
    }
}

/**
 * [Identity] in the qabel-index sense. This only holds a key pair, the hello protocol drop URL,
 * the alias and arbitrary [UpdateField]s.
 *
 * [UpdateIdentity.fromIdentity] fills the [UpdateIdentity.fields] by default from [Identity.email]
 * and [Identity.phone] when they are set (with the specified [UpdateAction]).
 *
 * This class is immutable. To change the fields, call [UpdateIdentity.copy] with the new
 * fields (e.g. as a keyword argument).
 */
data class UpdateIdentity(
    val keyPair: QblECKeyPair,
    val dropURL: DropURL,
    val alias: String,
    val fields: List<UpdateField>
) {

    constructor(identity: Identity, fields: List<UpdateField>) :
    this(identity.primaryKeyPair, identity.helloDropUrl, identity.alias, fields)

    companion object {
        fun fromIdentity(identity: Identity, action: UpdateAction): UpdateIdentity {
            val fields = ArrayList<UpdateField>()
            if (!identity.email.isNullOrBlank()) {
                fields += UpdateField(action, FieldType.EMAIL, identity.email)
            }
            if (!identity.phone.isNullOrBlank()) {
                fields += UpdateField(action, FieldType.PHONE, identity.phone)
            }
            return UpdateIdentity(
                keyPair = identity.primaryKeyPair,
                dropURL = identity.helloDropUrl,
                alias = identity.alias,
                fields = fields
            )
        }
    }

    fun toIndexContact(): IndexContact {
        return IndexContact(
            publicKey = keyPair.pub,
            dropUrl = dropURL,
            alias = alias
        )
    }
}

enum class UpdateAction {
    @SerializedName("create")
    CREATE,

    @SerializedName("delete")
    DELETE,
}

/**
 * Available field types. All values are strings, independent of FieldType.
 */
enum class FieldType {
    @SerializedName("email")
    EMAIL,

    @SerializedName("phone")
    PHONE,
}

data class UpdateField(
    val action: UpdateAction,
    val field: FieldType,
    val value: String
)

/**
 * Result of an update request issued by [IndexServer.publishIdentity] or [IndexServer.unpublishIdentity].
 */
enum class UpdateResult {
    /* Request accepted and is effective immediately */
    ACCEPTED_IMMEDIATE,
    /* Request accepted and takes effect when the user confirms it */
    ACCEPTED_DEFERRED,
}


private val IndexContactDeserializer = jsonDeserializer {
    /* By default gson will set fields not contained in the input data to null silently and I see no way
     * to change that behaviour without writing the required verification by hand. So here it goes.
     */
    val obj = it.json.obj
    if (!obj.contains("public_key") || !obj.contains("alias") || !obj.contains("drop_url")) {
        throw IllegalArgumentException("missing key in identity")
    }
    /* If a custom TypeAdapter is around, at least this level has to be spelled out, since we don't have access to
     * the generic type adapter here.
     */
    IndexContact(
        publicKey = it.context.deserialize(obj["public_key"], QblECPublicKey::class.java),
        dropUrl = it.context.deserialize(obj["drop_url"], DropURL::class.java),
        alias = obj["alias"].string
    )
}

/**
 * Return Gson instance with necessary TypeAdapters to serdes JSON according to the spec
 * http://qabel.github.io/docs/Qabel-Index/
 */
internal fun createGson(): Gson {
    return GsonBuilder()
        .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
        .registerTypeAdapter<IndexContact>(IndexContactDeserializer)
        .create()
}

/**
 * Interactor Models
 */
data class IndexSearch(val fieldType: FieldType, val value: String) {
    fun toMap(): Map<FieldType, String> = mapOf(Pair(fieldType, value))
}

enum class IndexSyncAction {
    CREATE, UPDATE
}

data class IndexSyncResult(val contact: Contact, val action: IndexSyncAction)

/**
 * Representing a external contact
 */
data class RawContact(val displayName: String,
                      val mobilePhoneNumbers: MutableList<String>,
                      val emailAddresses: MutableList<String>,
                      val identifier: String //external identifier
)

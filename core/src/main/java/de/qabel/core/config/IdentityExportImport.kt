package de.qabel.core.config

import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.spongycastle.util.encoders.Hex

import java.net.URISyntaxException
import java.util.ArrayList

object IdentityExportImport {

    private val KEY_ID = "id"
    private val KEY_ALIAS = "alias"
    private val KEY_EMAIL = "email"
    private val KEY_PHONE = "phone"
    private val KEY_PRIVATE_KEY = "private_key"
    private val KEY_PUBLIC_KEY = "public_key"
    private val KEY_PREFIXES = "prefixes"
    private val KEY_DROP_URLS = "drop_urls"

    /**
     * @param identity [Identity] to export
     * *
     * @return [Identity] information as JSON string
     */
    fun exportIdentity(identity: Identity): String {

        val jsonObject = JSONObject()
        val jsonDropUrls = JSONArray()

        try {
            jsonObject.put(KEY_ID, identity.id)
            jsonObject.put(KEY_ALIAS, identity.alias)
            jsonObject.put(KEY_EMAIL, identity.email)
            jsonObject.put(KEY_PHONE, identity.phone)
            jsonObject.put(KEY_PRIVATE_KEY, Hex.toHexString(identity.primaryKeyPair.privateKey))
            val jsonPrefixes = JSONArray()
            for (prefix in identity.prefixes) {
                jsonPrefixes.put(prefix)
            }
            jsonObject.put(KEY_PREFIXES, jsonPrefixes)
            jsonObject.put(KEY_PUBLIC_KEY, Hex.toHexString(identity.ecPublicKey.key))

            for (dropURL in identity.dropUrls) {
                jsonDropUrls.put(dropURL)
            }
            jsonObject.put(KEY_DROP_URLS, jsonDropUrls)
        } catch (e: JSONException) {
            // Shouldn't be possible to trigger this exception
            throw RuntimeException("Cannot build JSONObject", e)
        }

        return jsonObject.toString()
    }

    /**
     * Parse a [Identity] from a [Identity] JSON string

     * @param json [Identity] JSON string
     * *
     * @return [Identity] parsed from JSON string
     */
    @Throws(JSONException::class, URISyntaxException::class, QblDropInvalidURL::class)
    fun parseIdentity(json: String): Identity {

        val dropURLs = ArrayList<DropURL>()

        val jsonObject = JSONObject(json)
        val alias = jsonObject.getString(KEY_ALIAS)
        val jsonDropURLS = jsonObject.getJSONArray(KEY_DROP_URLS)
        for (i in 0..jsonDropURLS.length() - 1) {
            dropURLs.add(DropURL(jsonDropURLS.getString(i)))
        }

        val qblECKeyPair = QblECKeyPair(Hex.decode(jsonObject.getString(KEY_PRIVATE_KEY)))

        val identity = Identity(alias, dropURLs, qblECKeyPair)

        if (jsonObject.has(KEY_PREFIXES)) {
            val jsonPrefixes = jsonObject.getJSONArray(KEY_PREFIXES)
            for (i in 0..jsonPrefixes.length() - 1) {
                identity.prefixes.add(jsonPrefixes.getString(i))
            }
        }
        if (jsonObject.has(KEY_ID)) {
            identity.id = jsonObject.getInt(KEY_ID)
        }
        if (jsonObject.has(KEY_EMAIL)) {
            identity.email = jsonObject.getString(KEY_EMAIL)
        }
        if (jsonObject.has(KEY_PHONE)) {
            identity.phone = jsonObject.getString(KEY_PHONE)
        }
        return identity
    }
}

package de.qabel.core.index.server

import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.crypto.QblECPublicKey
import de.qabel.core.index.IndexContact
import de.qabel.core.index.UpdateField
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase
import org.apache.http.entity.ByteArrayEntity

internal fun encryptJson(json: String, senderKeyPair: QblECKeyPair, serverPublicKey: QblECPublicKey): ByteArray {
    val box = CryptoUtils().createBox(senderKeyPair, serverPublicKey, json.toByteArray(), 0)
    return box
}


internal fun encryptJsonIntoRequest(json: String,
                                    senderKeyPair: QblECKeyPair,
                                    serverPublicKey: QblECPublicKey,
                                    request: HttpEntityEnclosingRequestBase) {
    val encryptedJson = encryptJson(json, senderKeyPair, serverPublicKey)
    request.addHeader("Content-Type", "application/vnd.qabel.noisebox+json")
    request.entity = ByteArrayEntity(encryptedJson)
}


internal data class EncryptedApiRequest(
    val api: String,
    val timestamp: Long
)

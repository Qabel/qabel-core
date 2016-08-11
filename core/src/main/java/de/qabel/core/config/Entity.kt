package de.qabel.core.config

import de.qabel.core.drop.DropURL
import de.qabel.core.crypto.QblECPublicKey
import java.util.Collections
import java.util.HashSet

/**
 * Entity is an abstract class for a participant in a Qabel Drop
 * communication.
 */
abstract class Entity(drops: Collection<DropURL>?) : SyncSettingItem() {

    private val privateDropUrls: MutableSet<DropURL>

    init {
        if (drops != null) {
            privateDropUrls = HashSet(drops)
        } else {
            privateDropUrls = HashSet<DropURL>()
        }
    }

    abstract val ecPublicKey: QblECPublicKey

    /**
     * Returns the key identifier. The key identifier consists of the right-most 64 bit of the public fingerprint

     * @return key identifier
     */
    val keyIdentifier: String
        get() = ecPublicKey.readableKeyIdentifier

    val dropUrls: Set<DropURL>
        get() = privateDropUrls.toSet()


    fun addDrop(drop: DropURL) {
        privateDropUrls.add(drop)
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = super.hashCode()
        result = prime * result + privateDropUrls.hashCode()
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (!super.equals(obj)) {
            return false
        }
        if (javaClass != obj!!.javaClass) {
            return false
        }
        val other = obj as Entity?
        return privateDropUrls == other!!.privateDropUrls
    }

    companion object {
        private val serialVersionUID = -1239476740864486761L
    }
}

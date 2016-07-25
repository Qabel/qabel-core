package de.qabel.box.storage

import java.util.*

open class BoxFile(
    val prefix: String,
    val block: String,
    name: String,
    override val size: Long,
    mtime: Long,
    key: ByteArray,
    var hashed: Hash? = null,
    var shared: Share? = null
) : BoxObject(name), BoxFileState {

    constructor (prefix: String,
                 block: String,
                 name: String,
                 size: Long,
                 mtime: Long,
                 key: ByteArray) : this(prefix, block, name, size, mtime, key, null, null)


    init {
        this.key = key
    }

    val meta: String? get() = shared?.meta
    val metakey: ByteArray? get () = shared?.metaKey

    override var mtime: Long = mtime
        set (mtime) {
            field = mtime
            setChanged()
            notifyObservers()
        }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is BoxFile) {
            return false
        }

        val boxFile = other

        if (prefix != boxFile.prefix) {
            return false
        }
        if (block != boxFile.block) {
            return false
        }
        if (name != boxFile.name) {
            return false
        }
        if (size != boxFile.size) {
            return false
        }
        if (mtime != boxFile.mtime) {
            return false
        }
        if (!Arrays.equals(key, boxFile.key)) {
            return false
        }
        if (if (meta != null) meta != boxFile.meta else boxFile.meta != null) {
            return false
        }
        return Arrays.equals(metakey, boxFile.metakey)
    }

    override fun hashCode(): Int {
        var result = prefix.hashCode()
        result = 31 * result + block.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + size.hashCode()
        result = 31 * result + mtime.hashCode()
        result = 31 * result + if (key != null) Arrays.hashCode(key) else 0
        result = 31 * result + (meta?.hashCode() ?: 0)
        result = 31 * result + if (metakey != null) Arrays.hashCode(metakey) else 0
        return result
    }

    @Throws(CloneNotSupportedException::class)
    protected fun clone(): BoxFile {
        return BoxFile(prefix, block, name, size, mtime, key, hashed, shared)
    }

    override fun getRef(): String? {
        return meta
    }

    fun isSame(expectedFile: BoxFile?): Boolean {
        return expectedFile?.block == block
    }

    fun setHash(hash: ByteArray, algorithm: String) {
        hashed = Hash.create(hash, algorithm)
    }

    fun isShared() = shared != null
    fun isHashed() = hashed != null
}

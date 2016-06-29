package de.qabel.box.storage

import java.util.*

open class BoxFile (val prefix: String, val block: String, name: String, override val size: Long?, override var mtime: Long?, key: ByteArray) : BoxObject(name), BoxFileState {
    init {
        this.key = key;
    }
    var meta: String? = null
        set(meta) {
            field = meta
            setChanged()
            notifyObservers()
        }
    var metakey: ByteArray? = null
        set(metakey) {
            field = metakey
            setChanged()
            notifyObservers()
        }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val boxFile = o as BoxFile?

        if (if (prefix != null) prefix != boxFile!!.prefix else boxFile!!.prefix != null) {
            return false
        }
        if (if (block != null) block != boxFile.block else boxFile.block != null) {
            return false
        }
        if (if (name != null) name != boxFile.name else boxFile.name != null) {
            return false
        }
        if (if (size != null) size != boxFile.size else boxFile.size != null) {
            return false
        }
        if (if (mtime != null) mtime != boxFile.mtime else boxFile.mtime != null) {
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
        var result = if (prefix != null) prefix!!.hashCode() else 0
        result = 31 * result + if (block != null) block!!.hashCode() else 0
        result = 31 * result + if (name != null) name.hashCode() else 0
        result = 31 * result + if (size != null) size!!.hashCode() else 0
        result = 31 * result + if (mtime != null) mtime!!.hashCode() else 0
        result = 31 * result + if (key != null) Arrays.hashCode(key) else 0
        result = 31 * result + if (meta != null) meta!!.hashCode() else 0
        result = 31 * result + if (metakey != null) Arrays.hashCode(metakey) else 0
        return result
    }

    constructor(prefix: String, block: String, name: String, size: Long?, mtime: Long?, key: ByteArray, meta: String?, metaKey: ByteArray?) : this(prefix, block, name, size, mtime, key) {
        this.meta = meta
        metakey = metaKey
    }

    @Throws(CloneNotSupportedException::class)
    protected fun clone(): BoxFile {
        return BoxFile(prefix, block, name, size, mtime, key, meta, metakey)
    }

    /**
     * Get if BoxFile is shared. Tests only if meta and metakey is not null, not if a share has been
     * successfully send to another user.

     * @return True if BoxFile might be shared.
     */
    val isShared: Boolean
        get() = meta != null && metakey != null

    fun setMtime(mtime: Long) {
        this.mtime = mtime
        setChanged()
        notifyObservers()
    }

    override fun getRef(): String? {
        return meta
    }

    fun isSame(expectedFile: BoxFile?): Boolean {
        return expectedFile?.block == block
    }
}

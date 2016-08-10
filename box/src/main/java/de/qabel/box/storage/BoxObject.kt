package de.qabel.box.storage

import java.util.Observable

abstract class BoxObject(var name: String) : Observable(), Comparable<BoxObject> {
    abstract var key: ByteArray

    override fun compareTo(another: BoxObject): Int {
        if (this is BoxFile && another is BoxFile) {
            return name.compareTo(another.name)
        }
        if (this is BoxFolder && another is BoxFolder) {
            return name.compareTo(another.name)
        }
        if (this is BoxFile) {
            return -1
        }
        return 1
    }

    abstract val ref: String
}

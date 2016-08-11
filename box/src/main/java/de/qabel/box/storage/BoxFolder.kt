package de.qabel.box.storage

import java.util.Arrays

open class BoxFolder(override val ref: String, name: String, override var key: ByteArray) : BoxObject(name) {

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val boxFolder = o as BoxFolder?

        if (if (name != null) name != boxFolder!!.name else boxFolder!!.name != null) {
            return false
        }
        if (!Arrays.equals(key, boxFolder.key)) {
            return false
        }
        return !if (ref != null) ref != boxFolder.ref else boxFolder.ref != null

    }

    override fun hashCode(): Int {
        var result = if (name != null) name.hashCode() else 0
        result = 31 * result + if (key != null) Arrays.hashCode(key) else 0
        result = 31 * result + if (ref != null) ref!!.hashCode() else 0
        return result
    }
}

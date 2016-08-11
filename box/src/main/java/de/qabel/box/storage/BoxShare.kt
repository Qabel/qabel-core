package de.qabel.box.storage

class BoxShare @JvmOverloads constructor(val ref: String, val recipient: String, val type: String = BoxShare.TYPE_READ) {
    companion object {
        val TYPE_READ = "READ"
    }
}

package de.qabel.box.storage

interface BoxFileState {
    val size: Long?
    val mtime: Long?
}

package de.qabel.client.box.interactor

import de.qabel.box.storage.dto.BoxPath
import de.qabel.core.config.Contact
import org.apache.commons.lang3.builder.HashCodeBuilder
import java.io.InputStream
import java.util.*

data class VolumeRoot(val rootID: String, val documentID: String, val alias: String)
data class UploadSource(val source: InputStream, val entry: BrowserEntry.File)
data class DownloadSource(val entry: BrowserEntry.File, val source: InputStream)

data class FileOperationState(val ownerKey: BoxReadFileBrowser.KeyAndPrefix,
                              val entryName: String, val path: BoxPath.FolderLike,
                              val time: Long = System.currentTimeMillis(),
                              var done: Long = 0, var size: Long = 0,
                              var status: Status = Status.PREPARE) {

    enum class Status {
        PREPARE, LOADING, COMPLETING, COMPLETE, ERROR, CANCELED, HIDDEN
    }

    val loadDone: Boolean
        get() = (done == size)

}

sealed class BrowserEntry(val name: String) {

    val sharedTo: MutableList<Contact?> = mutableListOf()

    class File(name: String, val size: Long, val mTime: Date) : BrowserEntry(name) {
        override fun toString(): String {
            return "File($name, $size, $mTime)"
        }

        override fun equals(other: Any?): Boolean = when (other) {
            is File -> name == other.name && size == other.size && mTime == other.mTime
            else -> false
        }

        override fun hashCode(): Int {
            return HashCodeBuilder().append(size).append(mTime).build()
        }
    }

    class Folder(name: String) : BrowserEntry(name) {
        override fun toString(): String {
            return "Folder($name)"
        }

        override fun equals(other: Any?): Boolean = when (other) {
            is Folder -> name == other.name
            else -> false
        }

        override fun hashCode(): Int {
            return name.hashCode()
        }
    }
}

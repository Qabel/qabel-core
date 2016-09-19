package de.qabel.box.storage.dto

import org.apache.commons.lang3.builder.HashCodeBuilder

sealed class BoxPath() {

    abstract val name: String

    abstract val parent: FolderLike

    class File(override val name: String, override val parent: FolderLike): BoxPath()

    abstract class FolderLike(): BoxPath() {
        operator fun div(name: String) = Folder(name, this)
        operator fun div(path: File) = File(path.name, this)
        operator fun div(path: Folder) = Folder(path.name, this)

        operator fun times(name: String) = File(name, this)
    }

    class Folder(override val name: String, override val parent: FolderLike): FolderLike()

    object Root : FolderLike() {
        override val name: String
            get() = ""
        override val parent: FolderLike
            get() = this

        override fun hashCode(): Int = name.hashCode()

    }

    override fun equals(other: Any?): Boolean = when(other) {
        is Root -> (this is Root)
        is BoxPath -> (name == other.name) && parent.equals(other.parent)
        else -> false
    }

    override fun hashCode(): Int =
        HashCodeBuilder().append(name).append(parent.hashCode()).toHashCode()


    fun toList(): List<String> = if (this is Root) emptyList() else parent.toList() + name
}

package de.qabel.client.box.documentId

import de.qabel.box.storage.dto.BoxPath

data class DocumentId(val identityKey: String, val prefix: String, val path: BoxPath) {

    companion object {
        private val PATH_SEPARATOR = "/"
        val DOC_ID_SEPARATOR = "::::"
    }

    val pathString: String
        get() = path.parent.toList().joinToString(PATH_SEPARATOR)

    override fun toString(): String {
        return identityKey + DOC_ID_SEPARATOR +
            prefix + DOC_ID_SEPARATOR +
            pathString + PATH_SEPARATOR +
            if (path is BoxPath.FolderLike && path.name != "") {
                path.name + PATH_SEPARATOR
            } else {
                path.name
            }
    }

}

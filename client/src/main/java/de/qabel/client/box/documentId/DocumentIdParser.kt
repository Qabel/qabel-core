package de.qabel.client.box.documentId

import de.qabel.box.storage.dto.BoxPath
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.client.box.documentId.DocumentIdParser.Companion
import java.io.FileNotFoundException
import javax.inject.Inject


/**
 * Document IDs are built like this:
 * public-key::::prefix::::/filepath
 */
class DocumentIdParser @Inject constructor() {

    @Throws(FileNotFoundException::class)
    fun getIdentity(documentId: String): String {
        val split = documentId.split(DocumentId.DOC_ID_SEPARATOR.toRegex(), MAX_TOKEN_SPLITS).toTypedArray()
        if (split.isNotEmpty() && split[0].isNotEmpty()) {
            return split[0]
        }
        throw FileNotFoundException("Could not find identity in document id")
    }


    @Throws(FileNotFoundException::class)
    fun getPrefix(documentId: String): String {
        val split = documentId.split(DocumentId.DOC_ID_SEPARATOR.toRegex(), MAX_TOKEN_SPLITS).toTypedArray()
        if (split.size > 1 && split[1].isNotEmpty()) {
            return split[1]
        }
        throw FileNotFoundException("Could not find volume prefix in document id")
    }


    @Throws(FileNotFoundException::class)
    fun getFilePath(documentId: String): String {
        val split = documentId.split(DocumentId.DOC_ID_SEPARATOR.toRegex(), MAX_TOKEN_SPLITS).toTypedArray()
        if (split.size > 2 && split[2].isNotEmpty()) {
            return split[2]
        }
        throw FileNotFoundException("Could not find file path in document id")
    }

    @Throws(FileNotFoundException::class)
    fun getPath(documentId: String): String {
        var filepath = getFilePath(documentId)
        filepath = filepath.substring(0, filepath.lastIndexOf('/') + 1)
        // TODO: Workaround for wrong formatted document IDs
        if (filepath.startsWith("//")) {
            return filepath.substring(1, filepath.length)
        }
        return filepath
    }

    @Throws(FileNotFoundException::class)
    fun getBaseName(documentID: String): String {
        val filepath = getFilePath(documentID)
        return filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length)
    }


    fun buildId(identity: String, prefix: String?, filePath: String? = "/"): String {
        if (prefix != null && filePath != null) {
            return identity + DocumentId.DOC_ID_SEPARATOR + prefix + DocumentId.DOC_ID_SEPARATOR + filePath
        } else if (prefix != null) {
            return identity + DocumentId.DOC_ID_SEPARATOR + prefix
        } else {
            return identity
        }
    }

    @Throws(QblStorageException::class)
    fun parse(documentId: String): DocumentId {
        return documentId.toDocumentId()
    }

    companion object {
        val MAX_TOKEN_SPLITS = 3
    }
}

fun String.toDocumentId(): DocumentId {
    val parts = this.split(DocumentId.DOC_ID_SEPARATOR.toRegex(), DocumentIdParser.MAX_TOKEN_SPLITS)
    if (parts.size != 3) {
        throw QblStorageException("Invalid documentId: " + this)
    }
    val (identityKey, prefix, completePath) = parts
    val pathParts = completePath.split("/").filter(String::isNotEmpty)
    if (pathParts.isEmpty()) {
        return DocumentId(identityKey, prefix, BoxPath.Root)
    }
    val last = pathParts.last()
    val parents = pathParts.dropLast(1).fold<String, BoxPath.FolderLike>(BoxPath.Root) {
        path, part ->
        BoxPath.Folder(part, path)
    }
    val path = if (this.endsWith('/')) {
        BoxPath.Folder(last, parents)
    } else {
        BoxPath.File(last, parents)
    }
    return DocumentId(identityKey, prefix, path)
}

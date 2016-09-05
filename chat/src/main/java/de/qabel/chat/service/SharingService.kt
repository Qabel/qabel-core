package de.qabel.chat.service

import de.qabel.box.storage.BoxExternalFile
import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import java.io.File

interface SharingService {

    fun getOrCreateOutgoingShare(identity: Identity, contact: Contact, boxFile: BoxFile,
                                 boxNavigation: BoxNavigation): BoxFileChatShare
    fun revokeFileShare(share: BoxFileChatShare, boxFile: BoxFile, boxNavigation: BoxNavigation)

    @Throws(QblStorageNotFound::class, QblStorageException::class)
    fun getBoxExternalFile(share: BoxFileChatShare, boxReadBackend: StorageReadBackend, forceReload: Boolean = false) : BoxExternalFile
    fun updateShare(share: BoxFileChatShare, boxReadBackend: StorageReadBackend)

    fun getOrCreateIncomingShare(identity: Identity, message: ChatDropMessage, payload: ChatDropMessage.MessagePayload.ShareMessage): BoxFileChatShare
    fun acceptShare(chatDropMessage: ChatDropMessage, boxReadBackend: StorageReadBackend): BoxExternalFile
    fun downloadShare(share: BoxFileChatShare, targetFile: File, boxReadBackend: StorageReadBackend)
}

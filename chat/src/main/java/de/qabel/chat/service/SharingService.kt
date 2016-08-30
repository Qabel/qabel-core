package de.qabel.chat.service

import de.qabel.box.storage.BoxExternalFile
import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxNavigation
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import java.io.File

interface SharingService {

    fun getOrCreateOutgoingShare(identity: Identity, contact: Contact, boxFile: BoxFile,
                                 boxNavigation: BoxNavigation): BoxFileChatShare
    fun revokeFileShare(share: BoxFileChatShare, boxFile: BoxFile, boxNavigation: BoxNavigation)

    fun refreshShare(share: BoxFileChatShare, boxNavigation: BoxNavigation): BoxExternalFile

    fun getOrCreateIncomingShare(identity: Identity, message: ChatDropMessage, payload: ChatDropMessage.MessagePayload.ShareMessage): BoxFileChatShare
    fun acceptShare(chatDropMessage: ChatDropMessage, boxNavigation: BoxNavigation): BoxExternalFile
    fun downloadShare(share: BoxFileChatShare, targetFile: File, boxNavigation: BoxNavigation)
}

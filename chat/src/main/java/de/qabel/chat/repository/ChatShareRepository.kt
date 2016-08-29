package de.qabel.chat.repository

import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.framework.Repository

interface ChatShareRepository : Repository<BoxFileChatShare> {

    fun findByBoxReference(identity: Identity, metaUrl : String, metaKey : ByteArray): BoxFileChatShare?
    fun find(identity: Identity, contact: Contact? = null) : List<BoxFileChatShare>

    fun connectWithMessage(chatDropMessage: ChatDropMessage, share: BoxFileChatShare)
    fun findShareChatDropMessageIds(share: BoxFileChatShare): List<Int>

    fun findByMessage(chatDropMessage: ChatDropMessage): BoxFileChatShare

    fun findIncoming(identity: Identity): List<BoxFileChatShare>
    fun findOutgoing(identity: Identity): List<BoxFileChatShare>

}

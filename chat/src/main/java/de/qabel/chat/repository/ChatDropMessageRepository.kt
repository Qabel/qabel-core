package de.qabel.chat.repository

import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.repository.framework.Repository
import de.qabel.core.repository.framework.PagingResult

interface ChatDropMessageRepository : Repository<ChatDropMessage> {

    fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage>

    fun findNew(identityId: Int): List<ChatDropMessage>
    fun findLatest(identityId: Int): List<ChatDropMessage>

    fun exists(chatDropMessage : ChatDropMessage): Boolean

    fun markAsRead(contact: Contact, identity: Identity)
    fun findByContact(contactId: Int, identityId: Int, offset: Int, pageSize: Int): PagingResult<ChatDropMessage>

    open fun findByShare(share: BoxFileChatShare): List<ChatDropMessage>
}

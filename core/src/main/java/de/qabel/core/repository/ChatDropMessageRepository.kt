package de.qabel.core.repository

import de.qabel.core.chat.ChatDropMessage
import de.qabel.core.repository.framework.BaseRepository

interface ChatDropMessageRepository : BaseRepository<ChatDropMessage> {

    fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage>

}

package de.qabel.core.repository

import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.framework.BaseRepository

interface ChatDropMessageRepository : BaseRepository<ChatDropMessage> {

    fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage>

    open fun findNew(identityId: Int): List<ChatDropMessage>
    open fun findLatest(identityId: Int): List<ChatDropMessage>
}

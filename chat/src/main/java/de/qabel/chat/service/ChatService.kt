package de.qabel.chat.service

import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.repository.entities.DropState


interface ChatService {

    fun sendMessage(message: ChatDropMessage)
    fun refreshMessages(): Map<String, List<ChatDropMessage>>

    fun handleDropUpdate(identity: Identity, dropState: DropState, messages: List<DropMessage>): List<ChatDropMessage>
}

package de.qabel.core.service

import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.DropState


interface ChatService {

    fun sendMessage(message: ChatDropMessage)
    fun refreshMessages(): Map<Identity, List<ChatDropMessage>>

    fun handleDropUpdate(identity: Identity, dropState: DropState, messages: List<DropMessage>): List<ChatDropMessage>
}

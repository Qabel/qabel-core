package de.qabel.core.service

import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.http.DropConnector
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.DropState


interface ChatService {

    fun sendMessage(dropConnector: DropConnector, message: ChatDropMessage)
    fun refreshMessages(dropConnector: DropConnector): Map<Identity, List<ChatDropMessage>>

    open fun handleDropUpdate(identity: Identity, dropState: DropState, messages: List<DropMessage>): List<ChatDropMessage>
}

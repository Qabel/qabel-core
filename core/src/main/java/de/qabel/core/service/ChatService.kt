package de.qabel.core.service

import de.qabel.core.config.Identity
import de.qabel.core.repository.entities.ChatDropMessage


interface ChatService {

    fun sendMessage(message: ChatDropMessage)
    fun refreshMessages(): Map<Identity, List<ChatDropMessage>>

}

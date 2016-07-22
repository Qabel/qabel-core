package de.qabel.core.chat

import de.qabel.core.config.Contact

data class ChatConversation(val contact: Contact, val lastMessage: ChatDropMessage, var newCount: Int) {
}

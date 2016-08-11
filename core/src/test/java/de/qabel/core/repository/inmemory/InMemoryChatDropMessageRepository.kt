package de.qabel.core.repository.inmemory

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.exception.EntityNotFoundException

class InMemoryChatDropMessageRepository : ChatDropMessageRepository {

    val messages = mutableListOf<ChatDropMessage>()

    override fun findById(id: Int): ChatDropMessage = messages.find { it.id == id } ?: throw EntityNotFoundException("ChatDropMessage not found")

    override fun persist(model: ChatDropMessage) {
        model.id = messages.size + 1
        messages.add(model)
    }

    override fun update(model: ChatDropMessage) {
        delete(model.id)
        persist(model)
    }

    override fun delete(id: Int) {
        findById(id).let { messages.remove(it) }
    }

    override fun markAsRead(contact: Contact, identity: Identity) {
        messages.filter { it.contactId == contact.id && it.identityId == identity.id && it.status == ChatDropMessage.Status.NEW }.forEach {
            it.status = ChatDropMessage.Status.READ
        }
    }

    override fun findByContact(contactId: Int, identityId: Int): List<ChatDropMessage> =
        messages.filter { chatDropMessage -> chatDropMessage.contactId == contactId && chatDropMessage.identityId == identityId }

    override fun findNew(identityId: Int): List<ChatDropMessage> =
        messages.filter { it.identityId == identityId && it.status == ChatDropMessage.Status.NEW }

    override fun findLatest(identityId: Int): List<ChatDropMessage> {
        val latest = mutableMapOf<Int, ChatDropMessage>()
        messages.forEach {
            if (it.identityId == identityId) {
                val contactLatest = latest.getOrElse(it.contactId, { it })
                if (it.createdOn >= contactLatest.createdOn) {
                    latest[it.contactId] = it
                }
            }
        }
        return latest.values.toList()
    }

    override fun exists(chatDropMessage: ChatDropMessage): Boolean {
        return messages.any {
            chatDropMessage.direction.type == it.direction.type &&
                chatDropMessage.contactId == it.contactId &&
                chatDropMessage.identityId == it.identityId &&
                chatDropMessage.messageType.type == it.messageType.type &&
                chatDropMessage.payload.toString() == it.payload.toString()
        }
    }

}

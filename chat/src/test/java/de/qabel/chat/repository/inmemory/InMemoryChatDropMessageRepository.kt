package de.qabel.chat.repository.inmemory

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.chat.repository.ChatDropMessageRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.repository.framework.PagingResult

open class InMemoryChatDropMessageRepository : ChatDropMessageRepository {

    val messages = mutableListOf<ChatDropMessage>()
    override fun findByIds(ids: List<Int>): List<ChatDropMessage> =
        messages.filter { ids.contains(it.id) }

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

    override fun findByContact(contactId: Int, identityId: Int, offset: Int, pageSize: Int): PagingResult<ChatDropMessage> =
        findByContact(contactId, identityId).let {
            PagingResult(it.size, it.filterIndexed { i, chatDropMessage -> i >= offset && i < (offset + pageSize) })
        }

    override fun findByShare(share: BoxFileChatShare): List<ChatDropMessage> =
        messages.filter {
            it.payload is ChatDropMessage.MessagePayload.ShareMessage &&
                it.identityId == share.identityId &&
                (it.payload as ChatDropMessage.MessagePayload.ShareMessage).shareData.metaUrl == share.metaUrl
        }

}

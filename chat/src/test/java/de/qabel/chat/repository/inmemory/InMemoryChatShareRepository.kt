package de.qabel.chat.repository.inmemory

import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.repository.exception.EntityNotFoundException


class InMemoryChatShareRepository() : ChatShareRepository {

    private val shares = mutableListOf<BoxFileChatShare>()
    private val shareMessages = mutableMapOf<ChatDropMessage, BoxFileChatShare>()

    override fun findById(id: Int): BoxFileChatShare =
        shares.find { it.id == id } ?: throw EntityNotFoundException("")

    override fun findByIds(ids: List<Int>): List<BoxFileChatShare> =
        shares.filter { ids.contains(it.id) }

    override fun persist(model: BoxFileChatShare) {
        model.id == shares.size + 1
        shares.add(model)
    }

    override fun update(model: BoxFileChatShare) =
        findById(model.id).let {
            shares.remove(it)
            shares.add(model)
            Unit
        }

    override fun delete(id: Int) =
        findById(id).let {
            shares.remove(it)
            Unit
        }

    override fun findByBoxReference(identity: Identity, metaUrl: String, metaKey: ByteArray): BoxFileChatShare? =
        shares.find {
            it.identityId == identity.id &&
                it.metaKey.byteList == metaKey.toList() &&
                it.metaUrl == metaUrl
        }

    override fun find(identity: Identity, contact: Contact?): List<BoxFileChatShare> =
        shareMessages.filter {
            it.value.identityId == identity.id && contact?.let {
                c ->
                c.id == it.key.contactId
            } ?: true
        }.values.toList()

    override fun findIncoming(identity: Identity): List<BoxFileChatShare> =
        shareMessages.filter {
            it.value.identityId == identity.id && it.key.direction == ChatDropMessage.Direction.OUTGOING
        }.values.toList()

    override fun findOutgoing(identity: Identity): List<BoxFileChatShare> =
        shareMessages.filter {
            it.value.identityId == identity.id && it.key.direction == ChatDropMessage.Direction.INCOMING
        }.values.toList()
}

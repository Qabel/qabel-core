package de.qabel.core.service

import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.http.DropConnector
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropStateRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.ChatDropMessage.*
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.util.DefaultHashMap


class MainChatService(val dropConnector: DropConnector,
                      val identityRepository: IdentityRepository, val contactRepository: ContactRepository,
                      val chatDropMessageRepository: ChatDropMessageRepository, val dropStateRepository: DropStateRepository) : ChatService {

    fun sendMessage(message: ChatDropMessage) {
        val sender = identityRepository.find(message.identityId)
        val receiver = contactRepository.find(message.contactId)

        val dropMessage = message.toDropMessage(sender);
        chatDropMessageRepository.persist(message)
        //TODO Send to all dropUrls?
        dropConnector.sendDropMessage(sender, receiver, dropMessage, receiver.dropUrls.first())
        message.status = Status.SENT
        chatDropMessageRepository.update(message)
    }

    fun refreshMessages(): Map<Identity, List<ChatDropMessage>> {
        val identities = identityRepository.findAll()
        val resultMap = DefaultHashMap<Identity, MutableList<ChatDropMessage>>({ mutableListOf() })
        identities.entities.forEach { identity ->
            val iMessages = mutableListOf<ChatDropMessage>()
            identity.dropUrls.forEach { dropUrl ->
                val dropState = dropStateRepository.getDropState(dropUrl)
                val dropResult = dropConnector.receiveDropMessages(identity, dropUrl, dropState)

                dropResult.dropMessages.forEach {
                    iMessages.add(createChatDropMessage(identity, it))
                }
            }
            iMessages.forEach {
                if (!chatDropMessageRepository.exists(it)) {
                    chatDropMessageRepository.persist(it)
                    resultMap.getOrDefault(identity).add(it)
                }
            }
        }
        return resultMap
    }

    private fun createChatDropMessage(identity: Identity, dropMessage: DropMessage): ChatDropMessage {
        val contactId = try {
            contactRepository.findByKeyId(dropMessage.senderKeyId).id
        } catch (ex: EntityNotFoundException) {
            0
        }
        val type = if (dropMessage.dropPayload.equals(MessageType.SHARE_NOTIFICATION))
            MessageType.SHARE_NOTIFICATION else MessageType.BOX_MESSAGE

        return ChatDropMessage(contactId, identity.id, Direction.INCOMING,
            Status.NEW, type, dropMessage.dropPayload, dropMessage.creationDate.time)
    }

    fun ChatDropMessage.toDropMessage(identity: Identity): DropMessage = DropMessage(identity, payload, messageType.type)

}

package de.qabel.core.service

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropMessageMetadata
import de.qabel.core.http.DropConnector
import de.qabel.core.repository.ChatDropMessageRepository
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropStateRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.entities.ChatDropMessage
import de.qabel.core.repository.entities.ChatDropMessage.*
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.util.DefaultHashMap


open class MainChatService(val dropConnector: DropConnector,
                           val identityRepository: IdentityRepository, val contactRepository: ContactRepository,
                           val chatDropMessageRepository: ChatDropMessageRepository, val dropStateRepository: DropStateRepository) : ChatService {

    override fun sendMessage(message: ChatDropMessage) {
        val sender = identityRepository.find(message.identityId)
        val receiver = contactRepository.find(message.contactId)

        val dropMessage = message.toDropMessage(sender);

        var email = ""
        var phone = ""
        if (receiver.status != Contact.ContactStatus.UNKNOWN) {
            email = sender.email ?: ""
            phone = sender.phone ?: ""
        }
        dropMessage.dropMessageMetadata =
            DropMessageMetadata(sender.alias, sender.ecPublicKey, sender.dropUrls.first(), email, phone)

        chatDropMessageRepository.persist(message)
        dropConnector.sendDropMessage(sender, receiver, dropMessage, receiver.dropUrls.first())
        message.status = Status.SENT
        chatDropMessageRepository.update(message)
    }

    override fun refreshMessages(): Map<Identity, List<ChatDropMessage>> {
        val identities = identityRepository.findAll()
        val resultMap = DefaultHashMap<Identity, MutableList<ChatDropMessage>>({ mutableListOf() })
        identities.entities.forEach { identity ->
            val iMessages = mutableListOf<ChatDropMessage>()
            identity.dropUrls.forEach { dropUrl ->
                val dropState = dropStateRepository.getDropState(dropUrl)
                val dropResult = dropConnector.receiveDropMessages(identity, dropUrl, dropState)

                dropResult.dropMessages.forEach {
                    val contact = getMessageContact(it, identity)
                    contact?.apply {
                        iMessages.add(createChatDropMessage(identity, this, it))
                    }
                }
                dropStateRepository.setDropState(dropResult.dropState)
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

    private fun getMessageContact(dropMessage: DropMessage, identity: Identity): Contact? = try {
        val contact = contactRepository.findByKeyId(dropMessage.senderKeyId)
        if (contact.isIgnored) null else contact
    } catch (ex: EntityNotFoundException) {
        //If DropMessageMetadata is given, we create a new unknown contact
        dropMessage.dropMessageMetadata?.let {
            val contact = it.toContact()
            contact.status = Contact.ContactStatus.UNKNOWN
            contactRepository.save(contact, identity)
            contact
        } ?: null
    }

    private fun createChatDropMessage(identity: Identity, contact: Contact, dropMessage: DropMessage): ChatDropMessage {
        val type = if (dropMessage.dropPayload.equals(MessageType.SHARE_NOTIFICATION))
            MessageType.SHARE_NOTIFICATION else MessageType.BOX_MESSAGE

        return ChatDropMessage(contact.id, identity.id, Direction.INCOMING,
            Status.NEW, type, dropMessage.dropPayload, dropMessage.creationDate.time)
    }

    fun ChatDropMessage.toDropMessage(identity: Identity): DropMessage =
        DropMessage(identity, MessagePayload.encode(messageType, payload), messageType.type)

}

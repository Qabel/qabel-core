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
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.util.DefaultHashMap
import org.slf4j.LoggerFactory


open class MainChatService(val dropConnector: DropConnector, val identityRepository: IdentityRepository, val contactRepository: ContactRepository,
                           val chatDropMessageRepository: ChatDropMessageRepository, val dropStateRepository: DropStateRepository) : ChatService {

    companion object {
        private val logger = LoggerFactory.getLogger(MainChatService::class.java)
    }

    override fun sendMessage(message: ChatDropMessage) {
        val sender = identityRepository.find(message.identityId)
        val receiver = contactRepository.find(message.contactId)

        val dropMessage = message.toDropMessage(sender)

        var email = ""
        var phone = ""
        if (receiver.status != Contact.ContactStatus.UNKNOWN) {
            email = sender.email ?: ""
            phone = sender.phone ?: ""
        }
        dropMessage.dropMessageMetadata = DropMessageMetadata(sender.alias, sender.ecPublicKey,
            sender.dropUrls.first(), email, phone)

        logger.info("Send DropMessage...")
        chatDropMessageRepository.persist(message)
        dropConnector.sendDropMessage(sender, receiver, dropMessage, receiver.dropUrls.first())
        logger.info("DropMessage sent")
        message.status = Status.SENT
        chatDropMessageRepository.update(message)
    }

    override fun refreshMessages(): Map<String, List<ChatDropMessage>> {
        val resultMap = DefaultHashMap<String, List<ChatDropMessage>>({ emptyList() })
        identityRepository.findAll().entities.forEach { identity ->
            identity.dropUrls.forEach { dropUrl ->
                val dropState = dropStateRepository.getDropState(dropUrl)
                logger.info("Fetching DropMessages from {} with eTag {}", dropState.drop, dropState.eTag)
                try {
                    val dropResult = dropConnector.receiveDropMessages(identity, dropUrl, dropState)
                    val newMessages = handleDropUpdate(identity, dropResult.dropState, dropResult.dropMessages)
                    resultMap.put(identity.keyIdentifier, resultMap.getOrDefault(identity.keyIdentifier).plus(newMessages))
                } catch(ex: Throwable) {
                    logger.warn("Cannot receive messages from {}", dropState.drop, ex)
                }
            }
        }
        return resultMap.filter { !it.value.isEmpty() }
    }

    override fun handleDropUpdate(identity: Identity, dropState: DropState, messages: List<DropMessage>): List<ChatDropMessage> {
        val resultList = mutableListOf<ChatDropMessage>()
        messages.forEach {
            getMessageContact(it, identity)?.apply {
                val message = createChatDropMessage(identity, this, it)
                if (!chatDropMessageRepository.exists(message)) {
                    chatDropMessageRepository.persist(message)
                    resultList.add(message)
                } else {
                    logger.debug("Ignoring duplicated msg to " + identity.keyIdentifier)
                }
            }
        }
        dropStateRepository.setDropState(dropState)
        logger.info("Handle DropMessages ({}) from {} with eTag {}", messages.size,
            dropState.drop, dropState.eTag)
        return resultList
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

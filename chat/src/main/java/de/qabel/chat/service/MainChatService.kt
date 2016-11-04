package de.qabel.chat.service

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxNavigation
import de.qabel.chat.repository.ChatDropMessageRepository
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.*
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropConnector
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropMessageMetadata
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.DropStateRepository
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.repository.entities.DropState
import de.qabel.core.repository.exception.EntityNotFoundException
import de.qabel.core.util.DefaultHashMap
import org.slf4j.LoggerFactory
import rx.Observable
import rx.lang.kotlin.observable
import rx.schedulers.Schedulers


open class MainChatService(val dropConnector: DropConnector, val identityRepository: IdentityRepository, val contactRepository: ContactRepository,
                           val chatDropMessageRepository: ChatDropMessageRepository, val dropStateRepository: DropStateRepository,
                           val sharingService: SharingService) : ChatService {

    companion object {
        private val logger = LoggerFactory.getLogger(MainChatService::class.java)
    }

    override fun sendTextMessage(text: String, identity: Identity, contact: Contact): Observable<ChatDropMessage> =
        observable<ChatDropMessage> { subscriber ->
            val textMessage = createOutgoingMessage(identity, contact,
                MessageType.BOX_MESSAGE, MessagePayload.TextMessage(text))
            chatDropMessageRepository.persist(textMessage)

            subscriber.onNext(textMessage)
            sendMessage(textMessage)
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())

    override fun sendShareMessage(text: String, identity: Identity, contact: Contact, boxFile: BoxFile, boxNavigation: BoxNavigation): Observable<ChatDropMessage> =
        observable<ChatDropMessage> { subscriber ->
            val boxShare = sharingService.getOrCreateOutgoingShare(identity, contact, boxFile, boxNavigation)
            val shareMessage = createOutgoingMessage(identity, contact, MessageType.SHARE_NOTIFICATION,
                MessagePayload.ShareMessage(text, boxShare))
            chatDropMessageRepository.persist(shareMessage)
            subscriber.onNext(shareMessage)
            sendMessage(shareMessage)
            subscriber.onCompleted()
        }.subscribeOn(Schedulers.io())

    private fun createOutgoingMessage(identity: Identity, contact: Contact,
                                      messageType: MessageType, payload: MessagePayload) =
        ChatDropMessage(contact.id, identity.id,
            Direction.OUTGOING, Status.PENDING,
            messageType, payload,
            System.currentTimeMillis())


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

        if (message.id == 0) {
            chatDropMessageRepository.persist(message)
        }

        logger.info("Send DropMessage...")
        dropConnector.sendDropMessage(sender, receiver, dropMessage, receiver.dropUrls.first())
        logger.info("DropMessage sent")
        message.status = Status.SENT
        chatDropMessageRepository.update(message)
    }

    override fun refreshMessages(): Map<String, List<ChatDropMessage>> {
        val resultMap = DefaultHashMap<String, MutableList<ChatDropMessage>>({ mutableListOf() })
        identityRepository.findAll().entities.forEach { identity ->
            identity.dropUrls.forEach { dropUrl ->
                val dropState = dropStateRepository.getDropState(dropUrl)
                logger.info("Fetching DropMessages from {} with eTag {}", dropState.drop, dropState.eTag)
                try {
                    val dropResult = dropConnector.receiveDropMessages(identity, dropUrl, dropState)
                    val newMessages = handleDropUpdate(identity, dropResult.dropState, dropResult.dropMessages)

                    resultMap.getOrDefault(identity.keyIdentifier).addAll(newMessages)
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
                try {
                    val message = it.toChatDropMessage(identity, this)
                    if (!chatDropMessageRepository.exists(message)) {
                        if (message.payload is MessagePayload.ShareMessage) {
                            message.payload.apply {
                                shareData = sharingService.getOrCreateIncomingShare(identity, message, message.payload)
                            }
                        }
                        chatDropMessageRepository.persist(message)
                        resultList.add(message)
                    } else {
                        logger.debug("Ignoring duplicated msg to " + identity.keyIdentifier)
                    }
                } catch (ex: Throwable) {
                    logger.error("Error parsing DropMessage ${it.dropPayloadType}${it.dropPayload}")
                }
            }
        }
        dropStateRepository.setDropState(dropState)
        logger.info("Handle DropMessages ({}) from {} with eTag {}", messages.size,
            dropState.drop, dropState.eTag)
        return resultList
    }

    private fun getMessageContact(dropMessage: DropMessage, identity: Identity): Contact? = try {
        val contactDetails = contactRepository.findContactWithIdentities(dropMessage.senderKeyId)
        //Filter ignored
        if (contactDetails.contact.isIgnored) null
        //Dont receive messages from known identities
        else if (contactDetails.isIdentity) null
        //Add connection if required, TODO currently in discussion #629
        else if (!contactDetails.identities.contains(identity)) {
            contactRepository.save(contactDetails.contact, identity)
            contactDetails.contact
        } else {
            contactDetails.contact
        }
    } catch (ex: EntityNotFoundException) {
        //If DropMessageMetadata is given, we create a new unknown contact
        dropMessage.dropMessageMetadata?.let {
            val contact = it.toContact()
            contact.status = Contact.ContactStatus.UNKNOWN
            contactRepository.save(contact, identity)
            contact
        } ?: null
    }

    fun ChatDropMessage.toDropMessage(identity: Identity): DropMessage =
        DropMessage(identity, payload.toString(), messageType.type)

    fun DropMessage.toChatDropMessage(identity: Identity, contact: Contact): ChatDropMessage {
        val type = if (dropPayloadType.equals(MessageType.SHARE_NOTIFICATION.type))
            MessageType.SHARE_NOTIFICATION else MessageType.BOX_MESSAGE

        return ChatDropMessage(contact.id, identity.id, Direction.INCOMING,
            Status.NEW, type, dropPayload, creationDate.time)
    }

}

package de.qabel.core.drop

import de.qabel.core.config.Entity
import de.qabel.core.config.Identity

import java.io.Serializable
import java.util.Date
import java.util.UUID

class DropMessage : Serializable {


    var creationDate: Date
        private set
    var acknowledgeID: String
        private set
    var sender: Entity? = null
        private set
    var senderKeyId: String
        private set
    var dropPayload: String
        private set
    var dropPayloadType: String
        private set

    var dropMessageMetadata: DropMessageMetadata? = null

    constructor(sender: Entity, dropPayload: String, dropPayloadType: String) {
        this.sender = sender
        this.senderKeyId = sender.keyIdentifier
        this.dropPayload = dropPayload
        this.dropPayloadType = dropPayloadType
        creationDate = Date()
        acknowledgeID = NOACK
    }

    /**
     * Constructor used for deserialization.
     * registerSender has to be called to complete creation.
     */
    internal constructor(senderKeyId: String, dropPayload: String, dropPayloadType: String, created: Date,
                         acknowledgeId: String, dropMessageMetadata: DropMessageMetadata?) {
        this.senderKeyId = senderKeyId
        this.dropPayload = dropPayload
        this.dropPayloadType = dropPayloadType
        this.creationDate = created
        this.acknowledgeID = acknowledgeId
        this.dropMessageMetadata = dropMessageMetadata
    }

    /**
     * Register the given Entity as sender of this drop message
     * and check if it matches the senderKeyId.
     * This is used to complete the deserialization of DropMessage.

     * @param sender Entity to be registered as sender.
     * *
     * @return true if the given sender matches the senderKeyId, otherwise false.
     */
    fun registerSender(sender: Entity): Boolean {
        if (senderKeyId != sender.keyIdentifier) {
            return false
        }
        this.sender = sender
        return true
    }

    /**
     * Enable/disable acknowledge request for this drop message.
     * Acknowledging is disabled by default.

     * @param enabled true enables acknowledging
     */
    fun enableAcknowledging(enabled: Boolean) {
        if (enabled && acknowledgeID == NOACK) {
            acknowledgeID = generateAcknowledgeId()
        } else if (!enabled) {
            acknowledgeID = NOACK
        }
    }

    companion object {
        /**
         * Acknowledge ID indicating that the sender does not
         * wish to receive an acknowledgement.
         */
        val NOACK = "0"
        /**
         * Model object reserved for internal drop protocol purposes only.
         */
        val INTERNAL_MODEL_OBJECT = "drop"

        val version = 1

        private fun generateAcknowledgeId(): String {
            return UUID.randomUUID().toString()
        }
    }
}

package de.qabel.core.drop

import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.drop.DropURL
import de.qabel.core.drop.http.DropServerHttp
import de.qabel.core.repository.entities.DropState

interface DropConnector {

    fun sendDropMessage(identity: Identity, contact: Contact, message: DropMessage, server: DropURL)
    fun receiveDropMessages(identity: Identity, dropUrl: DropURL, dropState: DropState): DropServerHttp.DropServerResponse<DropMessage>

}

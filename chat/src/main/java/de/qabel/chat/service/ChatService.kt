package de.qabel.chat.service

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.BoxNavigation
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.drop.DropMessage
import de.qabel.core.repository.entities.DropState
import rx.Observable


interface ChatService {

    fun sendTextMessage(text : String, identity: Identity, contact : Contact) : Observable<ChatDropMessage>
    fun sendShareMessage(text : String, identity: Identity, contact: Contact,
                         boxFile: BoxFile, boxNavigation: BoxNavigation) : Observable<ChatDropMessage>


    fun sendMessage(message: ChatDropMessage)
    fun refreshMessages(): Map<String, List<ChatDropMessage>>

    fun handleDropUpdate(identity: Identity, dropState: DropState, messages: List<DropMessage>): List<ChatDropMessage>
}

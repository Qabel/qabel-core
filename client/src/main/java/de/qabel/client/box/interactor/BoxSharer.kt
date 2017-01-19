package de.qabel.client.box.interactor

import de.qabel.box.storage.BoxFile
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.service.ChatService
import de.qabel.chat.service.SharingService
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.BoxSchedulers
import de.qabel.client.box.interactor.VolumeNavigator
import de.qabel.client.box.interactor.Sharer
import rx.Observable
import rx.lang.kotlin.single
import rx.schedulers.Schedulers
import java.io.FileNotFoundException
import javax.inject.Inject

class BoxSharer @Inject constructor(private val volumeNavigator: VolumeNavigator,
                                    private val chatService: ChatService,
                                    private val owner: Identity,
                                    private val sharingService: SharingService,
                                    private val sharingRepo: ChatShareRepository,
                                    private val boxSchedulers: BoxSchedulers) : Sharer {

    override fun sendFileShare(contact: Contact, path: BoxPath): Observable<ChatDropMessage> {
        val (boxObject, nav) = volumeNavigator.queryObjectAndNav(path)
        if (boxObject !is BoxFile) {
            throw FileNotFoundException("Not a file")
        } else {
            return chatService.sendShareMessage(boxObject.name, owner, contact, boxObject, nav)
        }
    }

    override fun revokeFileShare(path: BoxPath) = single<Unit> { single ->
        val (boxObject, nav) = volumeNavigator.queryObjectAndNav(path)
        if (boxObject !is BoxFile) {
            throw FileNotFoundException("Not a file")
        } else if (boxObject.meta.isNullOrBlank() || boxObject.metakey?.isEmpty() ?: false) {
            throw QblStorageException("File is not shared!")
        }

        val chatShare = sharingRepo.findByBoxReference(owner, boxObject.meta!!, boxObject.metakey!!)

        //Use sharingservice if share is sent, else directly unshare with BoxNavigation
        chatShare?.apply {
            sharingService.revokeFileShare(chatShare, boxObject, nav)
        } ?: nav.unshare(boxObject)
        single.onSuccess(Unit)
    }.subscribeOn(boxSchedulers.io)

}

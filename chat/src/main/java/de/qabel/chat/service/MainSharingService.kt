package de.qabel.chat.service

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.BoxFileChatShare
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.config.SymmetricKey
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.repository.ContactRepository
import org.spongycastle.crypto.params.KeyParameter
import java.io.File
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.security.InvalidKeyException

class MainSharingService(private val chatShareRepository: ChatShareRepository,
                         private val contactRepository: ContactRepository,
                         private val tmpDir: File,
                         private val fileMetadataFactory: FileMetadataFactory,
                         private val cryptoUtils: CryptoUtils = CryptoUtils()
                         ) : SharingService {

    override fun getOrCreateOutgoingShare(identity: Identity, contact: Contact,
                                          boxFile: BoxFile, boxNavigation: BoxNavigation): BoxFileChatShare =
        (boxNavigation.getSharesOf(boxFile).find { it.recipient == contact.keyIdentifier }?.let {
            boxNavigation.getExternalReference(identity.ecPublicKey, boxFile)
        } ?: boxNavigation.share(identity.ecPublicKey, boxFile, contact.keyIdentifier)).let {
            chatShareRepository.findByBoxReference(identity, it.url, it.key) ?:
                createNewBoxFileShare(it, boxFile, identity, ShareStatus.CREATED).apply {
                    chatShareRepository.persist(this)
                }
        }

    override fun getOrCreateIncomingShare(identity: Identity, message: ChatDropMessage, payload: ChatDropMessage.MessagePayload.ShareMessage): BoxFileChatShare =
        chatShareRepository.findByBoxReference(identity, payload.shareData.metaUrl, payload.shareData.metaKey.toByteArray()) ?:
            payload.shareData.apply {
                ownerContactId = message.contactId
                identityId = identity.id
                chatShareRepository.persist(payload.shareData)
            }

    override fun revokeFileShare(share: BoxFileChatShare,
                                 boxFile: BoxFile, boxNavigation: BoxNavigation) {
        boxNavigation.unshare(boxFile)
        share.status = ShareStatus.REVOKED
        chatShareRepository.update(share)
    }

    override fun acceptShare(chatDropMessage: ChatDropMessage, boxReadBackend: StorageReadBackend): BoxExternalFile =
        if (chatDropMessage.payload is ChatDropMessage.MessagePayload.ShareMessage) {
            val share = chatShareRepository.findById(chatDropMessage.payload.shareData.id)
            share.status = ShareStatus.ACCEPTED
            updateShare(share, boxReadBackend)
            getBoxExternalFile(share, boxReadBackend)
        } else throw RuntimeException("No share drop message")

    override fun getBoxExternalFile(share: BoxFileChatShare,
                                    boxReadBackend: StorageReadBackend,
                                    forceReload: Boolean): BoxExternalFile {
        if (share.status == ShareStatus.DELETED) {
            throw QblStorageNotFound("Share has been deleted!")
        } else if (share.status == ShareStatus.NEW || forceReload) {
            return downloadFileMetadata(share, boxReadBackend)
        } else {
            val owner = contactRepository.find(share.ownerContactId)
            return BoxExternalFile(owner.ecPublicKey, share.prefix!!, share.block!!,
                share.name, share.size, share.modifiedOn, share.key!!.toByteArray(), share.hashed)
        }
    }

    override fun updateShare(share: BoxFileChatShare, boxReadBackend: StorageReadBackend) {
        try {
            val external = getBoxExternalFile(share, boxReadBackend, true)
            applyBoxFile(share, external)
        } catch (deleted: QblStorageNotFound) {
            share.status = ShareStatus.DELETED
        } catch (notReachable: QblStorageException) {
            share.status = ShareStatus.UNREACHABLE
        }
        chatShareRepository.update(share)
    }

    @Throws(IOException::class, InvalidKeyException::class, QblStorageException::class)
    override fun downloadShare(share: BoxFileChatShare, targetFile: File,
                               boxReadBackend: StorageReadBackend) {
        try {
            if (share.key == null) {
                updateShare(share, boxReadBackend)
            }
            val rootUri = URI(share.metaUrl)
            val url = rootUri.resolve("blocks/").resolve(share.block).toString()
            boxReadBackend.download(url, null).use { download ->
                cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download.inputStream, targetFile,
                    KeyParameter(share.key!!.byteList.toByteArray()))
            }
        } catch (e: URISyntaxException) {
            throw QblStorageException("no valid uri: " + share.metaUrl)
        }
    }

    private fun applyBoxFile(share: BoxFileChatShare, boxFile: BoxFile) {
        share.name = boxFile.name
        share.size = boxFile.size
        share.hashed = boxFile.hashed
        share.prefix = boxFile.prefix
        share.modifiedOn = boxFile.mtime
        share.key = SymmetricKey.Factory.fromBytes(boxFile.key)
        share.block = boxFile.block
    }

    private fun createNewBoxFileShare(boxFileRef: BoxExternalReference, boxFile: BoxFile,
                                      identity: Identity, status: ShareStatus): BoxFileChatShare =
        BoxFileChatShare(status, boxFileRef.name, boxFile.size, SymmetricKey.Factory.fromBytes(boxFileRef.key),
            boxFileRef.url, boxFile.hashed, boxFile.prefix, boxFile.mtime, SymmetricKey(boxFile.key.toList()),
            boxFile.block, contactRepository.findByKeyId(identity.keyIdentifier).id,
            identity.id)

    private fun downloadFileMetadata(share: BoxFileChatShare, boxReadBackend: StorageReadBackend): BoxExternalFile {
        val tmpFile = createTempFile("tmp_", "_fm", tmpDir)
        boxReadBackend.download(share.metaUrl, null).use({ download ->
            cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(download.inputStream,
                tmpFile, KeyParameter(share.metaKey.toByteArray()))
        })
        return fileMetadataFactory.open(tmpFile).file
    }
}

package de.qabel.chat.service

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.box.storage.jdbc.JdbcFileMetadataFactory
import de.qabel.chat.repository.ChatDropMessageRepository
import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.MessagePayload.ShareMessage
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.inmemory.InMemoryChatDropMessageRepository
import de.qabel.chat.repository.inmemory.InMemoryChatShareRepository
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.extensions.*
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.apache.commons.io.FileUtils
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SharingServiceTest() : CoreTestCase {

    val identityA: Identity = createIdentity("Bob").apply { id = 1 }
    val contactA: Contact = createContact(identityA.alias, identityA.helloDropUrl, identityA.ecPublicKey)
    val identityB: Identity = createIdentity("Alice").apply { id = 2 }
    val contactB: Contact = createContact(identityB.alias, identityB.helloDropUrl, identityB.ecPublicKey)

    lateinit var contactRepo: ContactRepository
    lateinit var shareRepo: ChatShareRepository
    lateinit var chatDropRepo : ChatDropMessageRepository

    lateinit var deviceID: ByteArray
    lateinit var volumeA: BoxVolume
    lateinit var navigationA: BoxNavigation

    lateinit var deviceID2: ByteArray
    lateinit var volumeB: BoxVolume
    lateinit var navigationB: BoxNavigation

    lateinit var tempFolder: File
    lateinit var readBackend: LocalReadBackend
    lateinit var volumeTmpDir: File

    lateinit var sharingService: SharingService

    lateinit var testFile: File

    @Before
    fun setUp() {
        contactRepo = InMemoryContactRepository().apply {
            save(contactA, identityB)
            save(contactB, identityA)
        }
        shareRepo = InMemoryChatShareRepository()
        chatDropRepo = InMemoryChatDropMessageRepository()

        tempFolder = createTempDir()
        volumeTmpDir = Files.createTempDirectory("qbl_test").toFile()

        readBackend = LocalReadBackend(tempFolder)
        val cryptoUtil = CryptoUtils()
        deviceID = cryptoUtil.getRandomBytes(16)
        volumeA = BoxVolumeImpl(readBackend, LocalWriteBackend(tempFolder),
            identityA.primaryKeyPair, deviceID, volumeTmpDir, "")
        volumeA.createIndex("qabel", "test123")
        navigationA = volumeA.navigate()

        deviceID2 = cryptoUtil.getRandomBytes(16)
        volumeB = BoxVolumeImpl(LocalReadBackend(tempFolder), LocalWriteBackend(tempFolder),
            identityB.primaryKeyPair, deviceID2, volumeTmpDir, "")
        volumeB.createIndex("qabel", "test456")
        navigationB = volumeB.navigate()

        sharingService = MainSharingService(shareRepo, contactRepo, tempFolder,
            JdbcFileMetadataFactory(tempFolder))

        testFile = randomFile(100)
    }

    @After
    fun cleanUp() {
        FileUtils.deleteDirectory(tempFolder)
        FileUtils.deleteDirectory(volumeTmpDir)
    }

    @Test
    fun testGetOrCreateFileShare() {
        val boxFile = navigationA.upload(testFile.name, testFile)

        val chatShare = sharingService.getOrCreateOutgoingShare(identityA, contactB, boxFile, navigationA)
        assertThat(chatShare.name, equalTo(boxFile.name))
        assertThat(chatShare.size, equalTo(100L))
        assertThat(chatShare.status, equalTo(ShareStatus.CREATED))

        val recreated = sharingService.getOrCreateOutgoingShare(identityA, contactB, boxFile, navigationA)
        assertThat(recreated.id, equalTo(chatShare.id))
    }

    @Test
    fun testReceiveShare() {
        val boxFileA = navigationA.upload(testFile.name, testFile)
        val chatShareA = sharingService.getOrCreateOutgoingShare(identityA, contactB, boxFileA, navigationA)

        val receivedPayload = ShareMessage("newMessage", chatShareA).toString()

        val shareDropMessage = ChatDropMessage(contactA.id, identityB.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.NEW,
            ChatDropMessage.MessageType.SHARE_NOTIFICATION, receivedPayload, System.currentTimeMillis())
        val sharePayload = shareDropMessage.payload as ShareMessage

        sharingService.getOrCreateIncomingShare(identityB, shareDropMessage, sharePayload)
        chatDropRepo.persist(shareDropMessage)

        val received = chatDropRepo.findByShare(sharePayload.shareData)
        assertThat(received, hasSize(1))
        val receivedMsg = received.first()
        val rMsgPayload= receivedMsg.payload as ShareMessage
        assertThat(rMsgPayload.shareData, equalTo(sharePayload.shareData))
        assertThat(rMsgPayload.shareData.status, equalTo(ShareStatus.NEW))

        val copy = ChatDropMessage(contactA.id, identityB.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.NEW,
            ChatDropMessage.MessageType.SHARE_NOTIFICATION, ShareMessage("ups", chatShareA).toString(), System.currentTimeMillis())
        val copyPayload = copy.payload as ShareMessage
        sharingService.getOrCreateIncomingShare(identityB, copy, copyPayload)
        chatDropRepo.persist(copy)
        assertThat(copyPayload.shareData.id, equalTo(rMsgPayload.shareData.id))

        //Check both messages connected
        assertThat(chatDropRepo.findByShare(sharePayload.shareData), hasSize(2))

        val boxExternalFile = sharingService.acceptShare(shareDropMessage, readBackend)
        assertThat(boxExternalFile.block, equalTo(boxFileA.block))
        assertThat(boxExternalFile.hashed, equalTo(boxFileA.hashed))
        assertThat(boxExternalFile.mtime, equalTo(boxFileA.mtime))

        val sharedFile = createTempFile()
        sharingService.downloadShare(rMsgPayload.shareData, sharedFile, readBackend)
        assertArrayEquals(FileUtils.readFileToByteArray(testFile), FileUtils.readFileToByteArray(sharedFile))

        //IdentityA revoke share
        sharingService.revokeFileShare(chatShareA, boxFileA, navigationA)
        assertThrows(QblStorageNotFound::class){
            sharingService.getBoxExternalFile(rMsgPayload.shareData, readBackend, true)
        }

        sharingService.updateShare(rMsgPayload.shareData, readBackend)
        assertThat(rMsgPayload.shareData.status, equalTo(ShareStatus.DELETED))
    }

}

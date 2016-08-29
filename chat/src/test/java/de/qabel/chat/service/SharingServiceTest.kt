package de.qabel.chat.service

import de.qabel.box.storage.*
import de.qabel.box.storage.exceptions.QblStorageException
import de.qabel.chat.repository.ChatShareRepository
import de.qabel.chat.repository.entities.ChatDropMessage
import de.qabel.chat.repository.entities.ChatDropMessage.MessagePayload.ShareMessage
import de.qabel.chat.repository.entities.ShareStatus
import de.qabel.chat.repository.inmemory.InMemoryChatShareRepository
import de.qabel.core.config.Contact
import de.qabel.core.config.Identity
import de.qabel.core.crypto.CryptoUtils
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createContact
import de.qabel.core.extensions.createIdentity
import de.qabel.core.repository.ContactRepository
import de.qabel.core.repository.inmemory.InMemoryContactRepository
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.hamcrest.Matchers.equalTo
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Path

class SharingServiceTest() : CoreTestCase {

    val identityA: Identity = createIdentity("Bob").apply { id = 1 }
    val contactA: Contact = createContact(identityA.alias, identityA.helloDropUrl, identityA.ecPublicKey)
    val identityB: Identity = createIdentity("Alice").apply { id = 2 }
    val contactB: Contact = createContact(identityB.alias, identityB.helloDropUrl, identityB.ecPublicKey)

    lateinit var contactRepo: ContactRepository
    lateinit var shareRepo: ChatShareRepository


    lateinit var deviceID: ByteArray
    lateinit var volumeA: BoxVolume
    lateinit var navigationA: BoxNavigation

    lateinit var deviceID2: ByteArray
    lateinit var volumeB: BoxVolume
    lateinit var navigationB: BoxNavigation

    lateinit var tempFolder: Path
    lateinit var readBackend: LocalReadBackend
    lateinit var volumeTmpDir: File

    lateinit var sharingService: SharingService

    lateinit var testFile: File

    private fun randomFile(size: Long): File =
        Files.createTempFile("qabel_", ".tmp").apply {
            IOUtils.write(CryptoUtils().getRandomBytes(size.toInt()), FileOutputStream(this.toFile()))
        }.toFile()

    @Before
    fun setUp() {
        contactRepo = InMemoryContactRepository().apply {
            save(contactA, identityB)
            save(contactB, identityA)
        }
        shareRepo = InMemoryChatShareRepository()

        tempFolder = Files.createTempDirectory("")
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

        sharingService = MainSharingService(shareRepo, contactRepo, readBackend)

        testFile = randomFile(100)
    }

    @After
    fun cleanUp() {
        FileUtils.deleteDirectory(tempFolder.toFile())
        FileUtils.deleteDirectory(volumeTmpDir)
    }

    @Test
    fun testGetOrCreateFileShare() {
        val boxFile = navigationA.upload(testFile.name, testFile)

        val chatShare = sharingService.getOrCreateFileShare(identityA, contactB, boxFile, navigationA)
        assertThat(chatShare.name, equalTo(boxFile.name))
        assertThat(chatShare.size, equalTo(100L))
        assertThat(chatShare.status, equalTo(ShareStatus.CREATED))

        val recreated = sharingService.getOrCreateFileShare(identityA, contactB, boxFile, navigationA)
        assertThat(recreated.id, equalTo(chatShare.id))
    }

    @Test
    fun testAddMessageToShare() {
        val boxFile = navigationA.upload(testFile.name, testFile)
        val chatShare = sharingService.getOrCreateFileShare(identityA, contactB, boxFile, navigationA)
        val chatMessage = ChatDropMessage(contactB.id, identityA.id,
            ChatDropMessage.Direction.OUTGOING, ChatDropMessage.Status.PENDING,
            ChatDropMessage.MessageType.SHARE_NOTIFICATION, ShareMessage("message", chatShare),
            System.currentTimeMillis())

        sharingService.addMessageToShare(chatShare, chatMessage)

        assertThat(shareRepo.findByMessage(chatMessage), equalTo(chatShare))
    }

    @Test
    fun testMarkShareSent() {
        val boxFile = navigationA.upload(testFile.name, testFile)
        val chatShare = sharingService.getOrCreateFileShare(identityA, contactB, boxFile, navigationA)
        sharingService.markShareSent(chatShare)
        assertThat(chatShare.status, equalTo(ShareStatus.SENT))
    }

    @Test
    fun testReceiveShare() {
        val boxFileA = navigationA.upload(testFile.name, testFile)
        val chatShareA = sharingService.getOrCreateFileShare(identityA, contactB, boxFileA, navigationA)

        val receivedPayload = ShareMessage("newMessage", chatShareA).toString()

        val shareDropMessage = ChatDropMessage(contactA.id, identityB.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.NEW,
            ChatDropMessage.MessageType.SHARE_NOTIFICATION, receivedPayload, System.currentTimeMillis())
        val sharePayload = shareDropMessage.payload as ShareMessage

        sharingService.receiveShare(identityB, shareDropMessage, sharePayload)

        val shareB = shareRepo.findByMessage(shareDropMessage)
        assertThat(shareB, equalTo(sharePayload.shareData))
        assertThat(shareB.status, equalTo(ShareStatus.NEW))

        val copy = ChatDropMessage(contactA.id, identityB.id, ChatDropMessage.Direction.INCOMING, ChatDropMessage.Status.NEW,
            ChatDropMessage.MessageType.SHARE_NOTIFICATION, ShareMessage("ups", chatShareA).toString(), System.currentTimeMillis())
        val copyPayload = copy.payload as ShareMessage
        sharingService.receiveShare(identityB, copy, copyPayload)
        assertThat(copyPayload.shareData.id, equalTo(shareB.id))

        //Check both messages connected
        assertThat(shareRepo.findShareChatDropMessageIds(shareB), hasSize(2))

        val boxExternalFile = sharingService.acceptShare(shareDropMessage, navigationB)
        assertThat(boxExternalFile.block, equalTo(boxFileA.block))
        assertThat(boxExternalFile.hashed, equalTo(boxFileA.hashed))
        assertThat(boxExternalFile.mtime, equalTo(boxFileA.mtime))

        val sharedFile = createTempFile()
        sharingService.downloadShare(shareB, sharedFile, navigationB)
        assertArrayEquals(FileUtils.readFileToByteArray(testFile), FileUtils.readFileToByteArray(sharedFile))

        //IdentityA revoke share
        sharingService.revokeFileShare(contactB, chatShareA, boxFileA, navigationA)
        try {
            sharingService.refreshShare(shareB, navigationB)
            fail("QblStorageException expected")
        } catch (ex: QblStorageException) {
        }
        assertThat(shareB.status, equalTo(ShareStatus.DELETED))
    }

}

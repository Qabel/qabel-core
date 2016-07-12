package de.qabel.box.storage.command

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.DirectoryMetadata
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadata
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.core.crypto.CryptoUtils
import org.spongycastle.crypto.params.KeyParameter

class CreateFolderChange(private val name: String, private val deviceId: ByteArray, val navigationFactory: FolderNavigationFactory) : DirectoryMetadataChange<ChangeResult<BoxFolder>> {
    private val cryptoUtils = CryptoUtils()
    private val secretKey: KeyParameter

    init {
        secretKey = cryptoUtils.generateSymmetricKey()
    }

    private var result : ChangeResult<BoxFolder>? = null

    @Synchronized
    override fun execute(dm: DirectoryMetadata): ChangeResult<BoxFolder> {
        if (dm !is JdbcDirectoryMetadata) {
            throw IllegalStateException("cannot create folder without a JdbcDirectoryMetadata")
        }
        for (folder in dm.listFolders()) {
            if (folder.name == name) {
                return ChangeResult(folder).apply { isSkipped = true }
            }
        }

        return prepareNewDM(dm).apply { dm.insertFolder(boxObject) }
    }

    private fun prepareNewDM(dm: JdbcDirectoryMetadata) = (result ?: createAndUploadDM(dm))

    private fun createAndUploadDM(dm: JdbcDirectoryMetadata): ChangeResult<BoxFolder> {
        val childDM = JdbcDirectoryMetadataFactory(dm.tempDir, deviceId).create()
        val folder = BoxFolder(childDM.fileName, name, secretKey.key)
        childDM.commit()

        with(navigationFactory.fromDirectoryMetadata(childDM, folder)) {
            setAutocommit(false)
            commit()
        }

        return ChangeResult(childDM, folder)
    }
}

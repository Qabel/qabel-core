package de.qabel.box.storage.command

import de.qabel.box.storage.*
import de.qabel.core.crypto.CryptoUtils
import org.spongycastle.crypto.params.KeyParameter

class CreateFolderChange(
    val parentNav: BoxNavigation,
    val name: String,
    val navigationFactory: FolderNavigationFactory,
    val directoryFactory: DirectoryMetadataFactory
) : DMChange<ChangeResult<BoxFolder>> {
    private val secretKey: KeyParameter by lazy { CryptoUtils().generateSymmetricKey() }
    private val result : ChangeResult<BoxFolder> by lazy { createAndUploadDM() }
    val folder: BoxFolder
        get() = result.boxObject

    @Synchronized
    override fun execute(dm: DirectoryMetadata): ChangeResult<BoxFolder> {
        for (folder in dm.listFolders()) {
            if (folder.name == name) {
                return ChangeResult(folder).apply { isSkipped = true }
            }
        }

        return result.apply { dm.insertFolder(boxObject) }
    }

    private fun createAndUploadDM(): ChangeResult<BoxFolder> {
        val childDM = directoryFactory.create()
        val folder = BoxFolder(childDM.fileName, name, secretKey.key)
        childDM.commit()

        navigationFactory.fromDirectoryMetadata(parentNav.path / folder.name, childDM, folder).commit()

        return ChangeResult(childDM, folder)
    }
}

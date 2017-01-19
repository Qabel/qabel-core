package de.qabel.client.box.interactor

import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.documentId.DocumentId
import de.qabel.core.repository.IdentityRepository
import java.io.FileNotFoundException

class BoxVolumeManager(private val identityRepository: IdentityRepository,
                       private val readFileBrowserFactory: (VolumeRoot) -> ReadFileBrowser,
                       private val operationFileBrowserFactory: (VolumeRoot) -> OperationFileBrowser) : VolumeManager {

    override val roots: List<VolumeRoot>
        get() = identityRepository.findAll().identities.map {
            val docId = DocumentId(it.keyIdentifier, it.prefixes.first().prefix, BoxPath.Root)
            VolumeRoot(docId.toString().dropLast(1), docId.toString(), it.alias)
        }

    override fun readFileBrowser(rootID: String) =
        readFileBrowserFactory(roots.find { it.documentID == rootID }
            ?: throw FileNotFoundException("No filebrowser for root id found: " + rootID))

    override fun operationFileBrowser(rootID: String): OperationFileBrowser =
        operationFileBrowserFactory(roots.find { it.documentID == rootID }
            ?: throw FileNotFoundException("No filebrowser for root id found: " + rootID))
}


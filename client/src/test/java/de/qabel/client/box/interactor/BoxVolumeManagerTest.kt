package de.qabel.client.box.interactor

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockito_kotlin.mock
import de.qabel.box.storage.dto.BoxPath
import de.qabel.client.box.documentId.DocumentId
import de.qabel.core.config.Prefix
import de.qabel.core.extensions.CoreTestCase
import de.qabel.core.extensions.createIdentity
import de.qabel.core.repository.inmemory.InMemoryIdentityRepository
import org.junit.Before
import org.junit.Test

class BoxVolumeManagerTest : CoreTestCase {

    val identity = createIdentity("name").apply { prefixes.add(Prefix("prefix")) }
    val repo = InMemoryIdentityRepository().apply { save(identity) }
    val docId = DocumentId(identity.keyIdentifier, identity.prefixes.first().prefix, BoxPath.Root)
    val volume = VolumeRoot(docId.toString().dropLast(1), docId.toString(), identity.alias)
    lateinit var manager: VolumeManager
    lateinit var readFileBrowser: ReadFileBrowser
    lateinit var operationFileBrowser: OperationFileBrowser

    @Before
    fun setUp() {
        readFileBrowser = mock()
        operationFileBrowser = mock()
    }

    @Test
    fun testGetRoots() {
        manager = BoxVolumeManager(repo, { readFileBrowser }, { operationFileBrowser })
        manager.roots shouldMatch equalTo(listOf(volume))
    }

    @Test
    fun testFileBrowser() {
        manager = BoxVolumeManager(repo, {
            it shouldMatch equalTo(volume)
            readFileBrowser
        }, {
            it shouldMatch equalTo(volume)
            operationFileBrowser
        })
        manager.readFileBrowser(volume.documentID) shouldMatch sameInstance(readFileBrowser)
        manager.operationFileBrowser(volume.documentID) shouldMatch sameInstance(operationFileBrowser)
    }
}

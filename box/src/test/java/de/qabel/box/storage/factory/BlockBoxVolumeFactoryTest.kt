package de.qabel.box.storage.factory

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.nhaarman.mockito_kotlin.*
import de.qabel.box.storage.RootRefCalculator
import de.qabel.box.storage.StorageDownload
import de.qabel.box.storage.StubReadBackend
import de.qabel.box.storage.jdbc.JdbcDirectoryMetadataFactory
import de.qabel.core.accounting.BoxClient
import de.qabel.core.config.Account
import de.qabel.core.config.Prefix
import de.qabel.core.config.Prefix.TYPE.CLIENT
import de.qabel.core.config.Prefix.TYPE.USER
import de.qabel.core.extensions.letApply
import de.qabel.core.repository.IdentityRepository
import de.qabel.core.testIdentity
import org.junit.Test
import org.mockito.stubbing.OngoingStubbing
import java.io.ByteArrayInputStream
import java.net.URI
import java.util.*

class BlockBoxVolumeFactoryTest {
    val account = Account("accounting.qabel.org", "testuser", "testpass")
    val boxClient: BoxClient = spy()
    val identity = testIdentity()
    val blockUri = URI("http://localhost:9697")
    val identityRepository: IdentityRepository = mock()
    val readBackend = StubReadBackend()
    val tmpDir = createTempDir("qbltest")
    val dmf = JdbcDirectoryMetadataFactory(tmpDir, "deviceId".toByteArray())
    val factory = BlockBoxVolumeFactory(
        "deviceId".toByteArray(),
        boxClient,
        identityRepository,
        dmf,
        blockUri,
        tmpDir,
        { readBackend },
        { mock() }
    )
    val boxClientPrefixes: OngoingStubbing<ArrayList<String>>
        = whenever(boxClient.prefixes).thenReturn(ArrayList(mutableListOf("prefix1", "prefix2", "prefix3")))
    val rootCalculator = RootRefCalculator()
    var prefix1 = Prefix("prefix1")
    var prefix2 = Prefix("prefix2")
    var prefix3 = Prefix("prefix3")
    var prefix4 = Prefix("prefix4")

    private fun ref(prefix: Prefix) = ref(prefix.prefix, prefix.type)

    private fun ref(prefix: String, type: Prefix.TYPE)
        = rootCalculator.rootFor(identity.primaryKeyPair.privateKey, type, prefix)

    private fun hasRef(prefix: Prefix) {
        val rootRef = ref(prefix)
        readBackend.respond(rootRef) {
            StorageDownload(ByteArrayInputStream("test".toByteArray()), "asd", 4)
        }
    }

    @Test
    fun prefersMainPrefix() {
        hasRef(prefix1); hasRef(prefix2)
        prefix3.account = account.user

        identity.prefixes = listOf(prefix1, prefix2, prefix3)

        assertThat(choose(), equalTo("prefix3"))
    }

    @Test
    fun prefersMainPrefixOfType() {
        prefix3 = Prefix("prefix3", CLIENT).letApply { it.account = account.user }
        prefix2.account = account.user
        hasRef(prefix1); hasRef(prefix2); hasRef(prefix3)

        identity.prefixes = listOf(prefix1, prefix2, prefix3)

        assertThat(choose(CLIENT), equalTo("prefix3"))
    }

    @Test
    fun returnsFirstUserPrefixOnUserRequest() {
        hasRef(prefix1); hasRef(prefix2); hasRef(prefix4)
        identity.prefixes = mutableListOf(prefix1, prefix2, prefix4)

        assertThat(choose(), equalTo("prefix1"))
        assertThat(prefix1.account ?: "", equalTo("testuser"))
        verify(identityRepository).save(identity)
    }

    @Test
    fun returnsFirstClientPrefixOnClientRequest() {
        prefix3 = Prefix("prefix3", CLIENT)
        hasRef(prefix1); hasRef(prefix3)
        identity.prefixes = mutableListOf(prefix1, prefix3)

        assertThat(choose(CLIENT), equalTo("prefix3"))
    }

    private fun choose(type: Prefix.TYPE = USER) = factory.choosePrefix(identity, account, type)

    @Test
    fun addsNewClientPrefixIfNoneMatches() {
        identity.prefixes = mutableListOf(prefix1, prefix2)
        hasRef(prefix1)
        boxClientPrefixes.thenReturn(   // on second call
            ArrayList(mutableListOf("prefix1", "prefix2", "prefix3", "prefix4"))
        )

        assertThat(choose(CLIENT), equalTo("prefix4"))
        verify(boxClient, times(1)).createPrefix()
        assertThat(identity.prefixes.last().account ?: "", equalTo("testuser"))
        verify(identityRepository, atLeastOnce()).save(identity)
    }

    @Test
    fun usesRemotePrefixWithIndexIfExistingAndNoLocalIsAvailable() {
        hasRef(prefix2)
        assertThat(choose(), equalTo("prefix2"))
    }

    @Test
    fun usesRemoteClientPrefixWithIndexIfExistingAndNoLocalIsAvailable() {
        prefix3 = Prefix("prefix3", CLIENT)
        hasRef(prefix2); hasRef(prefix3)

        assertThat(choose(CLIENT), equalTo("prefix3"))
    }

    @Test
    fun usesUnindexedLocalPrefixIfNoneIsIndexed() {
        identity.prefixes = listOf(prefix2)

        assertThat(choose(), equalTo("prefix2"))
    }
}

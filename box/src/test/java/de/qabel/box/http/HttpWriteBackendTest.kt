package de.qabel.box.http

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.should.shouldMatch
import de.qabel.box.storage.ModifiedException
import de.qabel.core.accounting.AccountingProfile
import de.qabel.core.accounting.BoxHttpClient
import de.qabel.core.config.AccountingServer
import org.apache.commons.io.IOUtils
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI

class HttpWriteBackendTest {
    val boxClient = BoxHttpClient(
        AccountingServer(URI("http://localhost:9696"), URI("http://localhost:9697"), "testuser", "testuser"),
        AccountingProfile()
    )
    val prefix = boxClient.apply { if (prefixes.isEmpty()) { createPrefix() } }.prefixes.first()
    val root = boxClient.buildBlockUri("api/v0/files/" + prefix).build().toString()
    val writeBackend = BlockWriteBackend(root, boxClient)
    val readBackend = BlockReadBackend(root, boxClient)

    @Test(expected = ModifiedException::class)
    fun throwsModifiedExceptionOnOldEtag() {
        writeBackend.upload("file", fakeDataStream())
        writeBackend.upload("file", fakeDataStream(), "not the current etag")
    }

    @Test
    fun uploadsSuccessfulIfEtagIsUpToDate() {
        val upload = writeBackend.upload("file", fakeDataStream())
        writeBackend.upload("file", fakeDataStream("new"), upload.etag)

        loadFileContent("file") shouldMatch equalTo("new")
    }

    private fun loadFileContent(filename: String): String {
        readBackend.download(filename).use {
            return IOUtils.toString(it.inputStream)
        }
    }

    private fun fakeDataStream(content: String = "test") = ByteArrayInputStream(content.toByteArray())
}

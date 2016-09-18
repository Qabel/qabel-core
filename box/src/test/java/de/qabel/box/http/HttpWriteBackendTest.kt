package de.qabel.box.http

import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.isNullOrEmptyString
import com.natpryce.hamkrest.should.shouldMatch
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import de.qabel.box.storage.ModifiedException
import de.qabel.core.accounting.AccountingProfile
import de.qabel.core.accounting.BoxHttpClient
import de.qabel.core.accounting.CloseableHttpResponseStub
import de.qabel.core.config.AccountingServer
import de.qabel.core.extensions.shouldNotMatch
import org.apache.commons.io.IOUtils
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI
import java.text.SimpleDateFormat
import java.util.*

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
        upload.etag shouldNotMatch isNullOrEmptyString
    }

    @Test
    fun parsesHeaders() {
        val format = SimpleDateFormat("dd.MM.yyyy HH:mm:ss zzz")
        format.timeZone = TimeZone.getTimeZone("GMT")

        writeBackend.httpclient = mock()
        whenever(writeBackend.httpclient.execute(any())).then {
                CloseableHttpResponseStub().apply {
                    addHeader("Date", "Fri, 01 Jan 2016 01:02:03 GMT")
                    addHeader("Etag", "e t a g")
                }}

        val upload = writeBackend.upload("file", fakeDataStream())
        upload.etag shouldMatch equalTo("e t a g")
        format.format(upload.time) shouldMatch equalTo("01.01.2016 01:02:03 GMT")
    }

    private fun loadFileContent(filename: String): String {
        readBackend.download(filename).use {
            return IOUtils.toString(it.inputStream)
        }
    }

    private fun fakeDataStream(content: String = "test") = ByteArrayInputStream(content.toByteArray())
}

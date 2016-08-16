package de.qabel.box.http

import de.qabel.core.accounting.AccountingProfile
import de.qabel.core.accounting.BoxHttpClient
import de.qabel.core.config.AccountingServer
import org.junit.Test
import java.io.ByteArrayInputStream
import java.net.URI

class HttpWriteBackendTest {
    val boxClient = BoxHttpClient(
        AccountingServer(URI("http://localhost:9696"), URI("http://localhost:9697"), "testuser", "testuser"),
        AccountingProfile()
    )
    val root = boxClient.buildBlockUri("api/v0/files/" + "prefix").build().toString();
    val writeBackend = BlockWriteBackend(root, boxClient)
    val readBaclend = BlockReadBackend(root, boxClient)

    @Test
    fun throwsModifiedExceptionOnOldEtag() {
        writeBackend.upload("file", ByteArrayInputStream("test".toByteArray()))
        writeBackend.uploadIfOld("name", )
    }
}

package de.qabel.client.box

import de.qabel.box.storage.StorageDownload
import de.qabel.box.storage.StorageReadBackend
import de.qabel.box.storage.StorageWriteBackend
import de.qabel.box.storage.UnmodifiedException
import de.qabel.box.storage.exceptions.QblStorageNotFound
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.util.*

class MockStorageBackend : StorageReadBackend, StorageWriteBackend {

    companion object {
        const val BASE = "local://"
    }

    val storage = mutableMapOf<String, Pair<String, ByteArray>>()

    override fun upload(name: String, content: InputStream): StorageWriteBackend.UploadResult {
        return upload(name, content, null)
    }

    override fun getUrl(meta: String) = BASE + meta

    override fun download(name: String) = download(name, null)

    override fun download(name: String, ifModifiedVersion: String?): StorageDownload {
        val (hash, content) = storage[name] ?: throw QblStorageNotFound("$name is not there")
        if (hash == ifModifiedVersion) {
            throw UnmodifiedException()
        }
        return StorageDownload(ByteArrayInputStream(content), hash, content.size.toLong())
    }

    override fun upload(name: String, content: InputStream, eTag: String?): StorageWriteBackend.UploadResult {
        val time = System.nanoTime()
        val result = mutableListOf<Byte>().apply {
            val it = content.buffered().iterator()
            while (it.hasNext()) {
                add(it.next())
            }
        }
        storage[name] = Pair(time.toString(), result.toByteArray())
        return StorageWriteBackend.UploadResult(Date(time / 1000), time.toString())
    }

    override fun delete(name: String) {
        storage.remove(name)
    }

}


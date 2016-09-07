package de.qabel.box.storage

import de.qabel.core.util.DefaultHashMap
import java.io.InputStream
import java.util.*

class StubWriteBackend : StorageWriteBackend {
    val handlers = DefaultHashMap<String, List<(name: String?) -> StorageWriteBackend.UploadResult>>({listOf<(name: String?) -> StorageWriteBackend.UploadResult>()})

    fun respond(fileName: String, uploadHandler: (name: String?) -> StorageWriteBackend.UploadResult) {
        handlers[fileName]!!.plus(uploadHandler)
    }

    private fun pop(name: String?) = handlers[name]!!.drop(1).first()

    override fun upload(name: String, content: InputStream): StorageWriteBackend.UploadResult {
        if (!handlers.containsKey(name)) {
            return generateSomeResponse()
        }

        return pop(name).invoke(name)
    }

    override fun upload(name: String, content: InputStream, eTag: String?): StorageWriteBackend.UploadResult {
        return upload(name, content)
    }

    override fun delete(name: String) {
        throw UnsupportedOperationException("not implemented")
    }

    private fun generateSomeResponse() = StorageWriteBackend.UploadResult(Date(), "etag");
}

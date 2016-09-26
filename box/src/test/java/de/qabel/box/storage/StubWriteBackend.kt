package de.qabel.box.storage

import de.qabel.core.util.DefaultHashMap
import java.io.InputStream
import java.util.*

class StubWriteBackend : StorageWriteBackend {
    val handlers = DefaultHashMap<String, MutableList<(name: String?) -> StorageWriteBackend.UploadResult>>({
        mutableListOf<(name: String?) -> StorageWriteBackend.UploadResult>()
    })

    fun respond(fileName: String, uploadHandler: (name: String?) -> StorageWriteBackend.UploadResult)
        = handlers[fileName]!!.add(uploadHandler)

    private fun pop(name: String?) = handlers[name]!!.drop(1).first()

    override fun upload(name: String, content: InputStream): StorageWriteBackend.UploadResult {
        if (!handlers.containsKey(name)) {
            return generateSomeResponse()
        }

        return pop(name).invoke(name)
    }

    override fun upload(name: String, content: InputStream, eTag: String?) = upload(name, content)

    override fun delete(name: String) {}

    private fun generateSomeResponse() = StorageWriteBackend.UploadResult(Date(), "etag")
}

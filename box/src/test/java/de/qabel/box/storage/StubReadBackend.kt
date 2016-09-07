package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.util.DefaultHashMap

class StubReadBackend : StorageReadBackend {
    val handlers = DefaultHashMap<String, List<(name: String?) -> StorageDownload>>({listOf<(name: String?) -> StorageDownload>()})

    fun respond(fileName: String, downloadHandler: (name: String?) -> StorageDownload) {
        handlers[fileName]!!.plus(downloadHandler)
    }

    private fun pop(name: String?) = handlers[name]!!.drop(1).first()

    override fun download(name: String?, ifModifiedVersion: String?): StorageDownload {
        return download(name)
    }

    override fun getUrl(meta: String?): String {
        throw UnsupportedOperationException("not implemented")
    }

    override fun download(name: String?): StorageDownload {

        if (!handlers.containsKey(name)) {
            throw QblStorageNotFound("no entry named $name")
        }

        return pop(name).invoke(name)
    }
}

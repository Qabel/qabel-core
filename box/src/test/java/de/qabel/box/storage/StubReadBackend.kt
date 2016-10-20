package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageNotFound
import de.qabel.core.logging.QabelLog
import de.qabel.core.util.DefaultHashMap

class StubReadBackend : StorageReadBackend, QabelLog {
    val uploadHandlers = DefaultHashMap<String, MutableList<(name: String?) -> StorageDownload>>({
        mutableListOf<(name: String?) -> StorageDownload>()
    })

    fun respond(fileName: String, downloadHandler: (name: String?) -> StorageDownload)
        = uploadHandlers[fileName]!!.add(downloadHandler)

    private fun pop(name: String?) = uploadHandlers[name]!!.drop(1).first()

    override fun download(name: String?, ifModifiedVersion: String?) = download(name)

    override fun getUrl(meta: String?): String = "http://some.url/" + meta

    override fun download(name: String?): StorageDownload {
        debug("downloading $name")
        if (!uploadHandlers.containsKey(name)) {
            throw QblStorageNotFound("no entry named $name")
        }

        return pop(name).invoke(name)
    }
}
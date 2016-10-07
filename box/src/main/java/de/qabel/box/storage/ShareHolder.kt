package de.qabel.box.storage

import de.qabel.box.storage.exceptions.QblStorageException

interface ShareHolder {
    @Throws(QblStorageException::class) fun listShares(): List<BoxShare>
    @Throws(QblStorageException::class) fun insertShare(share: BoxShare)
    @Throws(QblStorageException::class) fun deleteShare(share: BoxShare)
    @Throws(QblStorageException::class) fun getSharesOf(boxObject: BoxObject): List<BoxShare>
}

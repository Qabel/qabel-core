package de.qabel.box.storage

class DelayedIndexNavigation(val indexNavigation: IndexNavigation): ShareHolder {
    private val insertedShares = mutableListOf<BoxShare>()
    private val deletedShares = mutableListOf<BoxShare>()

    override fun listShares() = indexNavigation.listShares()

    override fun getSharesOf(boxObject: BoxObject) = indexNavigation.getSharesOf(boxObject)

    override fun insertShare(share: BoxShare) {
        insertedShares += share
    }

    override fun deleteShare(share: BoxShare) {
        deletedShares += share
    }

    fun execute() {
        insertedShares.forEach { indexNavigation.insertShare(it) }
        deletedShares.forEach { indexNavigation.deleteShare(it) }
    }
}

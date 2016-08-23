package de.qabel.core.ui

import de.qabel.core.repository.framework.PagingResult
import rx.Observable

class DataViewProxy<in T>(private val loader: (offset: Int, pageSize: Int) -> Observable<PagingResult<T>>,
                          private val dataView: DataView<T>,
                          private val pageSize: Int = 25) {

    private var loading = false
    private var total = -1

    fun loadMore() {
        if (canLoadMore()) {
            loadNext(dataView.getCount())
        }
    }

    fun load() {
        dataView.reset()
        total = -1
        loadNext(0)
    }

    private fun loadNext(offset: Int) {
        if (loading) return

        loading = true
        loader(offset, pageSize).subscribe({
            loading = false
            total = it.availableRange
            dataView.prependData(it.result)
        }, {
            loading = false
            dataView.handleLoadError(it)
        })
    }

    fun canLoadMore() = !loading && (total < 0 || dataView.getCount() < total)

    fun incRange(addedItemCount: Int) {
        total += addedItemCount
    }

    fun decRange(removedItemCount: Int) {
        total -= removedItemCount
    }

}

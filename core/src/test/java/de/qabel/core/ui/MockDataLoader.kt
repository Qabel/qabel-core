package de.qabel.core.ui

import de.qabel.core.repository.framework.PagingResult
import rx.Observable
import rx.lang.kotlin.toSingletonObservable
import java.util.*

class MockDataLoader(val size: Int, val data: MutableList<String> = ArrayList<String>(size).apply {
    for (i in 0 until size) {
        add(i, MockDataLoader.DATA_PREFIX + i)
    }
}) {

    companion object {
        const val DATA_PREFIX = "mockData_"
    }


    fun load(offset: Int, pageSize: Int): Observable<PagingResult<String>> {
        val limit = offset + pageSize
        return PagingResult(data.size, data.filterIndexed { i, s -> i >= offset && i < limit }).toSingletonObservable()
    }

}

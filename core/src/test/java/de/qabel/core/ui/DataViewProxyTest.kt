package de.qabel.core.ui

import org.junit.Before
import org.junit.Test
import org.hamcrest.Matchers.*
import org.junit.Assert.*

class DataViewProxyTest {

    val testPageSize = 25
    val dataView = MockDataView()
    val dataLoader = MockDataLoader(100)
    val dataProxy = DataViewProxy({ o, p -> dataLoader.load(o, p) }, dataView, testPageSize)

    @Before
    fun setUp() {
        dataView.reset()
    }

    @Test
    fun loadMore() {
        dataProxy.loadMore()
        assertThat(dataView.getCount(), equalTo(testPageSize))
        dataProxy.loadMore()
        assertThat(dataView.getCount(), equalTo(testPageSize * 2))
        dataProxy.loadMore()
        assertThat(dataView.getCount(), equalTo(testPageSize * 3))
        dataProxy.loadMore()
        assertThat(dataView.getCount(), equalTo(testPageSize * 4))
        dataProxy.loadMore()
        assertThat(dataView.getCount(), equalTo(testPageSize * 4))
    }

    @Test
    fun load() {
        dataProxy.load()
        assertThat(dataView.getCount(), equalTo(testPageSize))
    }

    @Test
    fun canLoadMore() {
        dataProxy.load()
        assertTrue(dataProxy.canLoadMore())
        (0.until(3)).forEach { dataProxy.loadMore() }
        assertFalse(dataProxy.canLoadMore())
    }

    @Test
    fun testModifyRange() {
        (0.until(4)).forEach { dataProxy.loadMore() }
        dataLoader.data.add(0, "Blubb")
        dataView.appendData(listOf("Blubb"))
        dataProxy.incRange(1)
        assertFalse(dataProxy.canLoadMore())
        dataLoader.data.add("Blubbb")
        dataProxy.incRange(1)
        assertTrue(dataProxy.canLoadMore())
        dataLoader.data.remove("Blubbb")
        dataProxy.decRange(1)
        assertFalse(dataProxy.canLoadMore())
    }

}

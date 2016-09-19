package de.qabel.box.storage.cache

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.sameInstance
import com.nhaarman.mockito_kotlin.mock
import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxNavigation
import org.junit.Test

class BoxNavigationCacheTest {
    private val cache = BoxNavigationCache<BoxNavigation>()
    private val instance1: BoxNavigation = mock()
    private val instance2: BoxNavigation = mock()
    private val folder1 = BoxFolder("ref1", "name1", "key1".toByteArray())
    private val folder2 = BoxFolder("ref2", "name2", "key2".toByteArray())

    @Test
    fun defaults() = assertThat(cache.get(folder1) { instance1 }, sameInstance(instance1))

    @Test
    fun caches() {
        cache.cache(folder1, instance1)
        assertThat(cache.get(folder1) { instance2 }, sameInstance(instance1))
    }

    @Test
    fun cachesPerKey() {
        cache.cache(folder1, instance1)
        assertThat(cache.get(folder2) { instance2 }, sameInstance(instance2))
    }

    @Test
    fun removes() {
        cache.cache(folder1, instance1)
        cache.remove(folder1)
        assertThat(cache.get(folder1) { instance2 }, sameInstance(instance2))
    }

    @Test
    fun hasNot() = assertThat(cache.has(folder2), equalTo(false))

    @Test
    fun has() = assertThat(cache.apply { cache(folder1, instance1) }.has(folder1), equalTo(true))
}

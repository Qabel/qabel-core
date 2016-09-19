package de.qabel.box.storage.cache

import de.qabel.box.storage.BoxFolder
import de.qabel.box.storage.BoxNavigation
import java.util.*

class BoxNavigationCache<C : BoxNavigation> {
    private val navs = WeakHashMap<String, C>()

    fun cache(folder: BoxFolder, nav: C) {
        navs.put(folder.ref, nav)
    }

    fun has(folder: BoxFolder): Boolean {
        return navs.containsKey(folder.ref)
    }

    operator fun get(folder: BoxFolder, default: () -> C): C {
        return navs[folder.ref] ?: default().apply {
            cache(folder, this)
        }
    }

    fun remove(folder: BoxFolder) {
        navs.remove(folder.ref)
    }

    val all: Iterable<C>
        get() = navs.values
}

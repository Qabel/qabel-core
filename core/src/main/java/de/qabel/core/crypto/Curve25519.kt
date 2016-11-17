package de.qabel.core.crypto

import java.io.File
import java.io.Serializable
import java.util.*

class Curve25519 : Serializable {

    external fun cryptoScalarmult(n: ByteArray, p: ByteArray): ByteArray

    external fun cryptoScalarmultBase(n: ByteArray): ByteArray

    companion object {
        init {
            loadLibraryIfNeccessary("curve25519")
        }

        private fun loadLibraryIfNeccessary(libname: String) {
            if (!isLoaded(libname)) {
                System.loadLibrary(libname)
            }
        }

        private fun isLoaded(libname: String): Boolean {
            val systemLibName = System.mapLibraryName(libname)
            var loadedLibraryPaths = getLoadedLibraries(ClassLoader.getSystemClassLoader())
            loadedLibraryPaths += getLoadedLibraries(Curve25519::class.java.classLoader)
            return loadedLibraryPaths.map { File(it).name }.contains(systemLibName)
        }

        private fun getLoadedLibraries(loader: ClassLoader): Array<String> {
            val LIBRARIES: java.lang.reflect.Field = ClassLoader::class.java.getDeclaredField("loadedLibraryNames")
            LIBRARIES.isAccessible = true
            val libraries = LIBRARIES.get(loader) as Vector<String>
            return libraries.toArray(arrayOf<String>())
        }
    }
}

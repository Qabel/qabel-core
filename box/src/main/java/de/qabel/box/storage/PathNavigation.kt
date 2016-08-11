package de.qabel.box.storage

import java.nio.file.Path

interface PathNavigation {
    val path: Path

    fun getPath(folder: BoxObject): Path
}

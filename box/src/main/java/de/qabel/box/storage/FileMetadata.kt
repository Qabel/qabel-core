package de.qabel.box.storage

import java.io.File

interface FileMetadata {
    val file: BoxExternalFile
    val path: File
}

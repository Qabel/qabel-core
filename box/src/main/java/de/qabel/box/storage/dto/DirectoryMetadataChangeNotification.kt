package de.qabel.box.storage.dto

import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.command.DirectoryMetadataChange

class DirectoryMetadataChangeNotification(
    val change: DirectoryMetadataChange<*>,
    val navigation: BoxNavigation
)

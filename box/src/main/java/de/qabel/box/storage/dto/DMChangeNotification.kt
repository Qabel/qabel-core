package de.qabel.box.storage.dto

import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.command.DMChange

class DMChangeNotification(
    val change: DMChange<*>,
    val navigation: BoxNavigation
)

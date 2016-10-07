package de.qabel.box.storage.dto

import de.qabel.box.storage.BoxNavigation
import de.qabel.box.storage.command.DMChange

class DMChangeEvent(
    val change: DMChange<*>,
    val navigation: BoxNavigation
)

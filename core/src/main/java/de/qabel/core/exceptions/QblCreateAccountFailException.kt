package de.qabel.core.exceptions

import java.util.HashMap

class QblCreateAccountFailException(map: HashMap<*, *>) : IllegalArgumentException(map.toString()) {

    val map: Map<*, *>

    init {
        this.map = map
    }
}

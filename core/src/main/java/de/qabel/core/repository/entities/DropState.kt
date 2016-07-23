package de.qabel.core.repository.entities

import de.qabel.core.repository.framework.BaseEntity

data class DropState(override var id: Int,
                     val drop: String, var eTag: String) : BaseEntity {

}

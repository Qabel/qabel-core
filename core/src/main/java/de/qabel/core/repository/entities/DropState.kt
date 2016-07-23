package de.qabel.core.repository.entities

import de.qabel.core.repository.framework.BaseEntity

data class DropState(val drop: String, var eTag: String, override var id: Int = 0) : BaseEntity {

}

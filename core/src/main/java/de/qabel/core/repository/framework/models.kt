package de.qabel.core.repository.framework

interface Field {
    fun exp(): String
    fun alias(): String
    fun select(): String
}

interface PersistableEnum {
    val type: Int
}


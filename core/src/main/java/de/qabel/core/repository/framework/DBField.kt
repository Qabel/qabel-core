package de.qabel.core.repository.framework

interface Field {
    fun exp(): String
    fun alias(): String
    fun select(): String
}

data class DBField(val name: String, val table: String, val tableAlias: String) : Field {
    override fun exp() = tableAlias.plus(".").plus(name)
    override fun alias() = tableAlias.plus("_").plus(name)
    override fun select() = exp() + " AS " + alias()
}

class CustomField(val expression: String, val alias: String) : Field {

    override fun exp(): String = alias

    override fun alias(): String = alias

    override fun select(): String = expression + " AS " + alias

}


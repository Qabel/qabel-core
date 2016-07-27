package de.qabel.core.repository.framework

data class DBField(val name: String, val table: String, val tableAlias: String) : Field {
    override fun exp() = tableAlias.plus(".").plus(name)
    override fun alias() = tableAlias.plus("_").plus(name)
    override fun select() = exp() + " AS " + alias()
}


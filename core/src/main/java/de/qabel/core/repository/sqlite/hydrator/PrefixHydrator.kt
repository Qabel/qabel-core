package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.config.Prefix
import de.qabel.core.repository.EntityManager

import java.sql.ResultSet
import java.sql.SQLException

class PrefixHydrator(val em: EntityManager) : AbstractHydrator<Prefix>() {
    public override fun getFields() = arrayOf("prefix", "type", "account_user")

    @Throws(SQLException::class)
    override fun hydrateOne(resultSet: ResultSet): Prefix = with(resultSet) {
        return Prefix(
            prefix=getString(1),
            type=Prefix.TYPE.valueOf(getString(2))
        ).apply { account = getString(3) }
    }

    override fun recognize(instance: Prefix) { }
}

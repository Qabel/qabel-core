package de.qabel.core.repository.sqlite.migration

import de.qabel.core.repository.sqlite.use
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException

abstract class AbstractMigration(private val connection: Connection) {

    abstract val version: Long

    @Throws(SQLException::class)
    protected fun execute(sql: String, vararg parameters: Any) {
        connection.prepareStatement(sql).use { statement ->
            for (i in 1..parameters.size - 1) {
                statement.setObject(i, parameters[i])
            }
            statement.execute()
        }
    }

    @Throws(SQLException::class)
    abstract fun up()

    @Throws(SQLException::class)
    abstract fun down()
}

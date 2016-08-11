package de.qabel.core.repository.sqlite

import de.qabel.core.repository.Transaction
import de.qabel.core.repository.exception.TransactionException

import java.sql.Connection
import java.sql.SQLException

class SqliteTransaction(private val connection: Connection, private val autocommitState: Boolean?) : Transaction {

    @Throws(TransactionException::class)
    override fun commit() {
        try {
            connection.commit()
        } catch (e: SQLException) {
            throw TransactionException("failed to commit", e)
        } finally {
            resetAutocommit()
        }
    }

    @Throws(TransactionException::class)
    override fun rollback() {
        try {
            connection.rollback()
        } catch (e: SQLException) {
            throw TransactionException("failed to rollback transaction", e)
        } finally {
            resetAutocommit()
        }
    }

    private fun resetAutocommit() {
        try {
            connection.autoCommit = autocommitState!!
        } catch (ignored: SQLException) {
        }

    }

    @Throws(Exception::class)
    override fun close() {
        try {
            commit()
        } catch (e: TransactionException) {
            rollback()
            throw e
        }

    }
}

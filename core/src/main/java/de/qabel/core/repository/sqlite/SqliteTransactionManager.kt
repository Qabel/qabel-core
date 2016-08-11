package de.qabel.core.repository.sqlite

import de.qabel.core.repository.RunnableTransaction
import de.qabel.core.repository.Transaction
import de.qabel.core.repository.TransactionManager
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.exception.TransactionException

import java.sql.Connection
import java.sql.SQLException
import java.util.concurrent.Callable
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock


class SqliteTransactionManager(private val connection: Connection) : TransactionManager {
    private val lock = ReentrantLock(true)

    @Throws(TransactionException::class)
    override fun beginTransaction(): Transaction {
        var oldAutocommit = false
        try {
            oldAutocommit = connection.autoCommit
            connection.autoCommit = false
            return SqliteTransaction(connection, oldAutocommit)
        } catch (e: SQLException) {
            try {
                connection.autoCommit = oldAutocommit
            } catch (ignored: SQLException) {
            }

            throw TransactionException("failed to start transaction", e)
        }

    }

    @Throws(PersistenceException::class)
    override fun <T> transactional(transactionBasedCallback: Callable<T>): T {
        val transaction = beginTransaction()
        try {
            val result = transactionBasedCallback.call()
            transaction.commit()
            return result
        } catch (e: Exception) {
            transaction.rollback()
            throw TransactionException(e.message ?: "", e)
        }

    }

    @Throws(PersistenceException::class)
    override fun transactional(runnable: RunnableTransaction) {
        val transaction = beginTransaction()
        try {
            runnable.run()
            transaction.commit()
        } catch (e: Exception) {
            transaction.rollback()
            throw TransactionException(e.message ?: "", e)
        }

    }
}

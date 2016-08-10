package de.qabel.core.repository.inmemory

import de.qabel.core.repository.RunnableTransaction
import de.qabel.core.repository.Transaction
import de.qabel.core.repository.TransactionManager
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.exception.TransactionException

import java.util.concurrent.Callable

class InMemoryTransactionManager : TransactionManager {
    @Throws(TransactionException::class)
    override fun beginTransaction(): Transaction {
        throw TransactionException("no transaction")
    }

    @Throws(PersistenceException::class)
    override fun <T> transactional(transactionBasedCallback: Callable<T>): T {
        try {
            return transactionBasedCallback.call()
        } catch (e: Exception) {
            throw PersistenceException(e.message ?: "", e)
        }

    }

    @Throws(PersistenceException::class)
    override fun transactional(runnable: RunnableTransaction) {
        try {
            runnable.run()
        } catch (e: Exception) {
            throw PersistenceException(e.message ?: "", e)
        }

    }
}

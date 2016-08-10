package de.qabel.core.repository

interface RunnableTransaction {
    @Throws(Exception::class)
    fun run()
}

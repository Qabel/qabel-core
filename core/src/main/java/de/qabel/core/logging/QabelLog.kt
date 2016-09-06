package de.qabel.core.logging

interface QabelLog {

    fun getLogger(): QabelLoggerWrapper =
        QabelLoggerManager.factory.createLogger(javaClass)

}

fun QabelLog.trace(message: Any, vararg args: Any) =
    getLogger().trace(message, args)

fun QabelLog.debug(message: Any, vararg args: Any) =
    getLogger().debug(message, args)

fun QabelLog.info(message: Any, vararg args: Any) =
    getLogger().info(message, args)

fun QabelLog.warn(message: Any, vararg args: Any) =
    getLogger().warn(message, args)

fun QabelLog.error(message: Any, throwable: Throwable? = null) =
    getLogger().error(message, throwable)

package de.qabel.core.logging

interface QabelLog {

    fun getLogger(): QabelLoggerWrapper = QabelLoggerManager.factory.createLogger(javaClass)

    fun trace(message: Any, vararg args: Any) = getLogger().trace(message, args)

    fun debug(message: Any, vararg args: Any) = getLogger().debug(message, args)

    fun info(message: Any, vararg args: Any) = getLogger().info(message, args)

    fun warn(message: Any, vararg args: Any) = getLogger().warn(message, args)

    fun error(message: Any?, throwable: Throwable? = null) = getLogger().error(message, throwable)
}

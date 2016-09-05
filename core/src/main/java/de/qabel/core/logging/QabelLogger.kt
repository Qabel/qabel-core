package de.qabel.core.logging

interface QabelLogger {
    val logger: QabelLoggerWrapper
        get() = QabelLoggerManager.factory.createLogger(javaClass)
}

fun QabelLogger.trace(message: Any, vararg args: Any) =
    logger.trace(message, args)

fun QabelLogger.debug(message: Any, vararg args: Any) =
    logger.debug(message, args)

fun QabelLogger.info(message: Any, vararg args: Any) =
    logger.info(message, args)

fun QabelLogger.warn(message: Any, vararg args: Any) =
    logger.warn(message, args)

fun QabelLogger.error(message: Any, throwable: Throwable? = null) =
    logger.error(message, throwable)

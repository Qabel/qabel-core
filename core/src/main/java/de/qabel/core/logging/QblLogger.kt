package de.qabel.core.logging

import org.slf4j.LoggerFactory

object QblLoggerManager {
    var factory: QblLoggerFactory = object : QblLoggerFactory {}
}

interface QblLoggerFactory {
    fun <T> createLogger(clazz: Class<T>): QblLoggerWrapper =
        DefaultQblLogger(LoggerFactory.getLogger(clazz))
}

interface QblLogger {
    val logger: QblLoggerWrapper
        get() = QblLoggerManager.factory.createLogger(javaClass)
}

fun QblLogger.trace(message: Any, vararg args: Any) =
    logger.trace(message, args)

fun QblLogger.info(message: Any, vararg args: Any) =
    logger.info(message, args)

fun QblLogger.debug(message: Any, vararg args: Any) =
    logger.debug(message, args)

fun QblLogger.warn(message: Any, vararg args: Any) =
    logger.warn(message, args)

fun QblLogger.error(message: Any, throwable: Throwable? = null) =
    logger.error(message, throwable)

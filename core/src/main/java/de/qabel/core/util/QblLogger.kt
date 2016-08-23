package de.qabel.core.util

import org.slf4j.Logger
import org.slf4j.LoggerFactory

interface QblLogger {
    val logger: Logger
        get() = LoggerFactory.getLogger(javaClass)
}

private inline fun log(
    message: Any?,
    thr: Throwable?,
    f: (String) -> Unit,
    fThrowable: (String, Throwable) -> Unit) {
    if (thr != null) {
        fThrowable(message?.toString() ?: "null", thr)
    } else {
        f(message?.toString() ?: "null")
    }
}

fun QblLogger.trace(message: Any?, thr: Throwable? = null) {
    log(message, thr,
        { msg -> logger.trace(msg) },
        { msg, thr -> logger.trace(msg, thr) })
}

fun QblLogger.debug(message: Any?, thr: Throwable? = null) {
    log(message, thr,
        { msg -> logger.debug(msg) },
        { msg, thr -> logger.debug(msg, thr) })
}

fun QblLogger.info(message: Any?, thr: Throwable? = null) {
    log(message, thr,
        { msg -> logger.info(msg) },
        { msg, thr -> logger.info(msg, thr) })
}

fun QblLogger.info(message: Any?, vararg args : Any?) {
    val formattedMsg = String.format(message.toString(), args)
    log(formattedMsg, null,
        { msg -> logger.info(msg) },
        { msg, thr -> logger.info(msg, thr) })
}

fun QblLogger.warn(message: Any?, thr: Throwable? = null) {
    log(message, thr,
        { msg -> logger.warn(msg) },
        { msg, thr -> logger.warn(msg, thr) })
}

fun QblLogger.error(message: Any?, thr: Throwable? = null) {
    log(message, thr,
        { msg -> logger.error(msg) },
        { msg, thr -> logger.error(msg, thr) })
}

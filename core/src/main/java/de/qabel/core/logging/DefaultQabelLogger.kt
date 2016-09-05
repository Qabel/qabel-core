package de.qabel.core.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory


class DefaultQabelLogger(clazz: Class<*>) : QabelLoggerWrapper {

    val logger: Logger = LoggerFactory.getLogger(clazz)

    override fun trace(msg: Any, vararg args: Any) =
        logger.trace(msg.toString(), *args)

    override fun info(msg: Any, vararg args: Any) =
        logger.info(msg.toString(), *args)

    override fun debug(msg: Any, vararg args: Any) =
        logger.debug(msg.toString(), *args)

    override fun warn(msg: Any, vararg args: Any) =
        logger.warn(msg.toString(), *args)

    override fun error(msg: Any, exception: Throwable?) =
        exception?.let {
            logger.error(msg.toString(), exception)
        } ?: logger.error(msg.toString())

}

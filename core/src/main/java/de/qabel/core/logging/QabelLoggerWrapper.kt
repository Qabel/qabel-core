package de.qabel.core.logging

interface QabelLoggerWrapper {

    fun trace(msg: Any, vararg args: Any)
    fun debug(msg: Any, vararg args: Any)
    fun info(msg: Any, vararg args: Any)
    fun warn(msg: Any, vararg args: Any)

    fun error(msg: Any?, exception: Throwable?)

}

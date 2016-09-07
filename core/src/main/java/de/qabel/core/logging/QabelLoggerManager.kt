package de.qabel.core.logging

object QabelLoggerManager {

    interface QabelLoggerFactory {
        fun <T> createLogger(clazz: Class<T>): QabelLoggerWrapper =
            DefaultQabelLoggerWrapper(clazz)
    }

    var factory: QabelLoggerFactory = object : QabelLoggerFactory {}

}

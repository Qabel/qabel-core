package de.qabel.core.util

interface CheckedFunction<T, S> {
    @Throws(Exception::class)
    fun apply(t: T): S
}

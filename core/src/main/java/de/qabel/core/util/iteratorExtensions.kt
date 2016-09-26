package de.qabel.core.util

fun  <T> Iterable<T>.loop(consumer: (T) -> Unit): Iterable<T> {
    forEach(consumer)
    return this
}

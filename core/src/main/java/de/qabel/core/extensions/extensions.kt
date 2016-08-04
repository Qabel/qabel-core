package de.qabel.core.extensions

/** TODO
 * Kotlin currently not support AutoCloseable, just Closeable
 * Some hope : "TODO: Provide use kotlin package for AutoCloseable"
 * **/
inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (closeException: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

inline fun <T> T.letApply(block: (T) -> Unit): T {
    block(this);
    return this;
}

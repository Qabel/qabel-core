package de.qabel.core.repository.sqlite

inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            close()
        } catch (ignored: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            close()
        }
    }
}

inline fun <T:AutoCloseable,R> tryWith(closeable: T, block: T.() -> R): R = closeable.use(block)


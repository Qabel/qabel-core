package de.qabel.core.http

import java.util.Date

class HTTPResult<T> {
    var responseCode: Int = 0
    var isOk: Boolean = false
    internal var lastModified: Date
    var data: T

    fun setLastModified(lastModified: Date) {
        this.lastModified = lastModified
    }

    fun lastModified(): Date {
        return lastModified
    }
}

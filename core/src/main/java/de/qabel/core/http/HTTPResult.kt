package de.qabel.core.http

import java.util.Date

class HTTPResult<T> {
    var responseCode: Int = 0
    var isOk: Boolean = false
    internal var lastModified: Date? = null
    var data: T? = null

    fun setLastModified(lastModified: Date) {
        this.lastModified = lastModified
    }

    fun lastModified(): Date {
        return lastModified ?: throw IllegalStateException("no last modified date set")
    }
}

package de.qabel.core

object StringUtils {
    fun join(separator: String, parts: Array<String>): String {
        return org.apache.commons.lang3.StringUtils.join(parts, separator)
    }

    fun <T> join(separator: String, parts: List<T>): String {
        return org.apache.commons.lang3.StringUtils.join(parts, separator)
    }
}

package de.qabel.core.extensions

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException

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
    block(this)
    return this
}

private fun <T> JsonObject.getOrThrow(key: String, conversion: (el: JsonElement) -> T): T = try {
    get(key).let {
        if (it == null) {
            throw JsonSyntaxException("Missing property " + key)
        } else {
            conversion(it)
        }
    }
} catch(ex: ClassCastException) {
    throw JsonSyntaxException("Value has a wrong type")
} catch(ex: IllegalStateException) {
    throw JsonSyntaxException("Value is a JSONArray")
}

fun JsonObject.getInt(key: String): Int = getOrThrow(key, { it.asInt })
fun JsonObject.getString(key: String): String = getOrThrow(key, { it.asString })
fun JsonObject.getLong(key: String): Long = getOrThrow(key, { it.asLong })

fun JsonElement.safeObject() = try {
    this.asJsonObject
} catch (e: IllegalStateException) {
    throw JsonSyntaxException("Not a json object")
}

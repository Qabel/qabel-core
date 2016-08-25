package de.qabel.core.ui

import de.qabel.core.config.Contact
import de.qabel.core.config.Entity
import de.qabel.core.config.Identity

private val splitRegex: Regex by lazy { " ".toRegex() }
private fun String.toInitials(): String = split(splitRegex).mapIndexed { i, s ->
    if (i < 2) s.first().toUpperCase().toString() else ""
}.joinToString(" ")

fun Contact.displayName(): String {
    if (nickName != null && !nickName.isEmpty()) {
        return nickName
    }
    return alias
}

fun Contact.initials(): String = displayName().toInitials()

fun Identity.initials() = alias.toInitials()

fun Entity.readableKey() = keyIdentifier.foldIndexed(StringBuilder(), { i, text, char ->
    text.append(char)
    if (i > 0) {
        val current = i.inc()
        if (current % 16 == 0) {
            text.append("\n")
        } else if (current % 4 == 0) {
            text.append(" ")
        }
    }
    text
})

fun Entity.readableUrl(): String {
    val dropUrlString = dropUrls.first().toString()
    val last = dropUrlString.lastIndexOf("/").inc()
    val split = (last + (dropUrlString.length - last) / 2)
    return dropUrlString.substring((0 until last)) + "\n" +
        dropUrlString.substring((last until split)) + "\n" +
        dropUrlString.substring(split.inc())
}

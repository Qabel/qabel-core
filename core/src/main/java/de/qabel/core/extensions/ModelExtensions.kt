package de.qabel.core.extensions

import de.qabel.core.config.Contact
import de.qabel.core.config.Entity
import de.qabel.core.config.EntityMap
import de.qabel.core.config.Identity

fun Identity.toContact(): Contact {
    val contact = Contact(alias, dropUrls, ecPublicKey);
    contact.email = email;
    contact.phone = phone;
    return contact;
}

fun <T : Entity> Map<Int, List<String>>.mapEntities(key: Int, entities: EntityMap<T>): List<T> =
    getOrElse(key, { emptyList<String>() }).map { entities.getByKeyIdentifier(it) }

fun <T : Entity> List<T>.contains(keyIdentifier: String) : Boolean =
    any { entity -> entity.keyIdentifier.equals(keyIdentifier) }

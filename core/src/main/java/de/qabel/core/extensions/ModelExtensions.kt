package de.qabel.core.extensions

import de.qabel.core.config.Entity
import de.qabel.core.config.EntityMap

fun <T : Entity> Map<Int, List<String>>.mapEntities(key: Int, entities: EntityMap<T>): List<T> =
    getOrElse(key, { emptyList<String>() }).map { entities.getByKeyIdentifier(it) }

fun <T : Entity> List<T>.contains(keyIdentifier: String) : Boolean =
    any { entity -> entity.keyIdentifier.equals(keyIdentifier) }

fun <T  : Entity> Set<T>.findById(id : Int) = find { it.id == id }

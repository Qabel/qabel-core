package de.qabel.core.repository

import de.qabel.core.config.Contact
import de.qabel.core.drop.DropURL


interface DropUrlRepository {

    fun findAll(contact: Contact): Collection<DropURL>
    fun findDropUrls(contactIds: List<Int>): Map<Int, List<DropURL>>
}

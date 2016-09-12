package de.qabel.core.index.server

import de.qabel.core.index.RawContact


interface ExternalContactsAccessor {

    fun getContacts(): List<RawContact>

}

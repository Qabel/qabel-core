package de.qabel.core.index


interface ExternalContactsAccessor {

    fun getContacts(): List<RawContact>

}

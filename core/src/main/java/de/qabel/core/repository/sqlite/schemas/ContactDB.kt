package de.qabel.core.repository.sqlite.schemas


object ContactDB {

    const val TABLE_NAME = "contact"
    const val TABLE_ALIAS = "c";

    const val ID = "id";
    const val ALIAS = "alias";
    const val PUBLIC_KEY = "publicKey";
    const val PHONE = "phone";
    const val EMAIL = "email";

    val ENTITY_FIELDS = listOf(ID, ALIAS, PUBLIC_KEY, PHONE, EMAIL)

}

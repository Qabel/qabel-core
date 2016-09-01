package de.qabel.core.repository.sqlite.schemas

import de.qabel.core.repository.framework.DBField

object IdentityDB {

    val TABLE = "identity"
    val ALIAS = "ide"
    val ID = DBField("id", TABLE, ALIAS)
    val CONTACT_ID = DBField("contact_id", TABLE, ALIAS)
    val PRIVATE_KEY = DBField("privateKey", TABLE, ALIAS)

}

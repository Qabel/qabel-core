package de.qabel.core.repository.sqlite.schemas

import de.qabel.core.config.Identity
import de.qabel.core.repository.framework.DBField
import de.qabel.core.repository.framework.DBRelation
import org.spongycastle.util.encoders.Hex
import java.sql.PreparedStatement

object IdentityDB : DBRelation<Identity> {

    override val TABLE_NAME: String = "identity"
    override val TABLE_ALIAS: String = "ide"

    override val ID = field("id")
    val CONTACT_ID = field("contact_id")
    val PRIVATE_KEY = field("privateKey")

    override val ENTITY_FIELDS: List<DBField> = listOf(PRIVATE_KEY, CONTACT_ID)
    override val ENTITY_CLASS: Class<Identity> = Identity::class.java

    override fun applyValues(startIndex: Int, statement: PreparedStatement, model: Identity): Int {
        with(statement) {
            var i = startIndex
            setString(i++, Hex.toHexString(model.primaryKeyPair.privateKey))
            //contact_id is set by repository, not that nice, but forced by the schema
            return i
        }
    }

}

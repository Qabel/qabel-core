package de.qabel.chat.repository.entities

import de.qabel.box.storage.Hash
import de.qabel.core.config.SymmetricKey
import de.qabel.core.repository.framework.BaseEntity

data class BoxFileChatShare(
    var status: ShareStatus,
    //Initial data from shareDropMsg
    var name: String,
    var size: Long,
    val metaKey: SymmetricKey,
    val metaUrl: String,
    //Optional data loaded from fm
    var hashed: Hash? = null,
    var prefix: String? = null,
    var modifiedOn: Long = 0,
    var key: SymmetricKey? = null,
    var block: String? = null,

    var ownerContactId: Int = 0,
    var identityId : Int = 0,
    override var id: Int = 0) : BaseEntity {
}

enum class ShareStatus(val type: Int) {
    //Incoming
    NEW(0),
    ACCEPTED(1), ATTACHED(2),
    //Outgoing
    CREATED(3),
    REVOKED(4),
    //Both
    UNREACHABLE(5),
    DELETED(6)
}

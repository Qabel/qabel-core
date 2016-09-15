package de.qabel.core.config

import de.qabel.core.repository.framework.PersistableEnum

/**
 * [VerificationStatus] for email-address and phone number of [Identity]s
 */
enum class VerificationStatus(override val type: Int) : PersistableEnum<Int> {

    //Field not set (default for existing)
    NONE(0),
    //Field locally set but not on index
    NOT_VERIFIED(1),
    //Index is updated
    VERIFIED(2)

}


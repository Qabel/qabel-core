package de.qabel.core.config

/**
 * [VerificationStatus] for email-address and phone number of [Identity]s
 */
enum class VerificationStatus(val type: Int) {
    //Field not set (default for existing)
    NONE(0),
    //Field has been modified
    MODIFIED(1),
    //Update request has been sent
    PENDING(2),
    //Index is updated
    VERIFIED(3)
}

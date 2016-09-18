package de.qabel.core.index.server

import de.qabel.core.index.*
import java.io.IOException


/**
 * Index server API
 *
 * Exceptions of interest are [IndexServerException] (inherits [IOException]) and it's subclasses
 * [APIError] and [MalformedResponseException].
 */
interface IndexServer {
    /**
     * SearchEndpoint for identities on the index server.
     *
     * At least one attribute has to be specified, or [IllegalArgumentException] will be raised.
     *
     * Returns a list of [IndexContact] instances. If nothing is found, returns an empty list.
     */
    @Throws(IOException::class)
    fun search(attributes: Map<FieldType, String>): List<IndexContact>

    /**
     * Shortcut to search for an email alone.
     */
    @Throws(IOException::class)
    fun searchForMail(email: String): List<IndexContact> {
        return search(mapOf(Pair(FieldType.EMAIL, email)))
    }

    /**
     * Shortcut to search for a phone number alone.
     */
    @Throws(IOException::class)
    fun searchForPhone(phone: String): List<IndexContact> {
        return search(mapOf(Pair(FieldType.PHONE, phone)))
    }

    /**
     * UpdateEndpoint published data of [identity] on the index.
     *
     * For calls with one or more [UpdateField]s with [UpdateAction.CREATE] this will require confirmation by the user
     * in a production setting (e.g. by acting on an email or SMS message).
     *
     * To update e.g. the published email address of an identity, two [UpdateField]s would be added:
     *
     * 1. [UpdateField] with the old email address and [UpdateAction.DELETE]
     * 2. [UpdateField] with the new email address and [UpdateAction.CREATE]
     *
     * As soon as the user confirms the new email address the old email address will be replaced seamlessly.
     */
    @Throws(IOException::class)
    fun updateIdentity(identity: UpdateIdentity): UpdateResult

    /**
     * Confirm a verification code.
     *
     * These are sent to the user to verify eg. mail addresses or phone numbers.
     * See [Index spec](http://qabel.github.io/docs/Qabel-Index/#verification-confirmation) for details.
     */
    @Throws(IOException::class, CodeInvalidException::class, CodeExpiredException::class)
    fun confirmVerificationCode(code: String)

    /**
     * Deny a verification code.
     *
     * See [confirmVerificationCode]
     */
    @Throws(IOException::class, CodeInvalidException::class, CodeExpiredException::class)
    fun denyVerificationCode(code: String)
}

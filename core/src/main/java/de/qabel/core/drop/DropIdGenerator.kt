package de.qabel.core.drop

import org.spongycastle.util.encoders.UrlBase64

import java.util.Arrays

abstract class DropIdGenerator {

    internal abstract fun generateDropIdBytes(): ByteArray

    /**
     * Generates identifier for a drop encoded in Base64url.

     * @return the identifier
     */
    fun generateDropId(): String {
        val id = Arrays.copyOf(generateDropIdBytes(), DROP_ID_LENGTH_BYTE)
        return String(UrlBase64.encode(id)).substring(0, DROP_ID_LENGTH) // cut off terminating dot
    }

    companion object {
        /**
         * Length of the drop id in base64 encoding.
         */
        val DROP_ID_LENGTH_BYTE = 32
        /**
         * Length of the base64url encoded id.
         * The terminating padding character is not part of the id.
         */
        val DROP_ID_LENGTH = 4 * ((DROP_ID_LENGTH_BYTE + 2) / 3) - 1

        val defaultDropIdGenerator: DropIdGenerator
            get() = AdjustableDropIdGenerator()
    }
}

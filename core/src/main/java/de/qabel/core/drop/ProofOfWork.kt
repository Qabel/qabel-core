package de.qabel.core.drop

import de.qabel.core.crypto.CryptoUtils
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.util.encoders.Base64

import java.nio.ByteBuffer
import java.util.Calendar
import java.util.TimeZone

class ProofOfWork
/**
 * Initializes PoW
 */
(
        /**
         * For further description of the scheme see http://qabel.github.io/docs/Qabel-Protocol-ProofOfWork/
         */
        private val leadingZeros: Int, private val initVectorServer: ByteArray, private val initVectorClient: ByteArray, val time: Long, private val messageHash: ByteArray, val counter: Long, private val pow: ByteArray) {

    val iVserverB64: String
        get() = Base64.toBase64String(initVectorServer)

    val iVclientB64: String
        get() = Base64.toBase64String(initVectorClient)

    val messageHashB64: String
        get() = Base64.toBase64String(messageHash)

    val proofOfWorkHashB64: String
        get() = Base64.toBase64String(pow)

    companion object {
        internal val longLength = java.lang.Long.SIZE / java.lang.Byte.SIZE
        internal val hashLength = 256 / 8 //SHA-256

        /**
         * Calculates the PoW for given parameters

         * @param leadingZeros     Number of leading zero bits of PoW hash
         * *
         * @param initVectorServer Server IV which is part of the PoW
         * *
         * @param messageHash      hash of message to be sent
         * *
         * @return byte[][]: byte[0]=plain parameters byte[1]=PoW hash
         */
        fun calculate(leadingZeros: Int, initVectorServer: ByteArray, messageHash: ByteArray): ProofOfWork {
            val time: Long
            val counter: Long
            val initVectorClient: ByteArray
            val pow = ByteArray(hashLength)
            val cryptoUtils = CryptoUtils()
            val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))

            //time in seconds since epoch UTC
            time = calendar.timeInMillis / 1000L
            val timeBytes = toByteArray(time)
            initVectorClient = cryptoUtils.getRandomBytes(16)

            val fix = composeFixParts(initVectorServer, initVectorClient, timeBytes, messageHash)

            //Find counter which fulfills pattern
            counter = calculatePow(pow, fix, leadingZeros)

            return ProofOfWork(leadingZeros, initVectorServer, initVectorClient, time, messageHash, counter, pow)
        }

        private fun composeFixParts(initVectorServer: ByteArray, initVectorClient: ByteArray, time: ByteArray, messageHash: ByteArray): ByteArray {
            val fix = ByteArray(initVectorServer.size + initVectorClient.size + time.size + messageHash.size)
            var offset = 0
            System.arraycopy(initVectorServer, 0, fix, offset, initVectorServer.size)
            offset = initVectorServer.size
            System.arraycopy(initVectorClient, 0, fix, offset, initVectorClient.size)
            offset += initVectorClient.size
            System.arraycopy(time, 0, fix, offset, time.size)
            offset += time.size
            System.arraycopy(messageHash, 0, fix, offset, messageHash.size)
            return fix
        }

        /**
         * Checks whether hash starts with required leading zero bits

         * @param hash         hash to be verified
         * *
         * @param leadingZeros required leading zeros
         * *
         * @return true of hash starts with required leading zero bits
         */
        private fun enoughZeros(hash: ByteArray, leadingZeros: Int): Boolean {
            for (i in 0..leadingZeros - 1) {
                //check whether i-th bit is zero
                if (hash[i / 8] shr i % 8 and 1 != 0) {
                    return false
                }
            }
            return true
        }

        /**
         * Finds a valid proof of work hash with leading zeros

         * @param pow          result of the calculation
         * *
         * @param fix          fix part of the proof of work
         * *
         * @param leadingZeros required leading zeros
         * *
         * @return counter for the valid hash
         */
        private fun calculatePow(pow: ByteArray, fix: ByteArray, leadingZeros: Int): Long {
            val digest = SHA256Digest()
            var counter: Long = 0
            if (pow.size == hashLength) {
                digest.update(fix, 0, fix.size)
                digest.update(toByteArray(counter), 0, longLength)
                digest.doFinal(pow, 0)

                while (!enoughZeros(pow, leadingZeros)) {
                    counter++
                    digest.reset()
                    digest.update(fix, 0, fix.size)
                    digest.update(toByteArray(counter), 0, longLength)
                    digest.doFinal(pow, 0)
                }
                digest.reset()
                return counter
            } else {
                return 0
            }
        }

        internal fun toByteArray(number: Long): ByteArray {
            return ByteBuffer.allocate(longLength).putLong(number).array()
        }
    }
}

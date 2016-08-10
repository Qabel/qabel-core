package de.qabel.core.drop

import java.math.BigInteger
import java.util.Random

/**
 * This drop id generator generates drop ids randomly.
 * It is called adjustable because one can adjust how many bits
 * of the theoretically available id bit length is used during
 * the randomization.
 * This allows a simple tradeoff between collision probability
 * and communication overhead or in other words between anonymity
 * and performance.
 * The rule of thumb is: more utilized bits less collision probability
 * and therefore less anonymity but more performance.
 */
class AdjustableDropIdGenerator
/**
 * Creates a new random drop id using `usedBits` number of bits
 * for the namespace.
 * Hence, the collision probability is 1/2^(usedBits).

 * @param usedBits number of bits used during randomization. Must be in the
 * *                 interval (0, DROP_ID_LENGTH_BYTE*8].
 */
@JvmOverloads constructor(private val usedBits: Int = DropIdGenerator.DROP_ID_LENGTH_BYTE * 8) : DropIdGenerator() {

    init {
        if (usedBits <= 0 || usedBits > DropIdGenerator.DROP_ID_LENGTH_BYTE * 8) {
            throw IllegalArgumentException("Used bits must be between 0 and " + DropIdGenerator.DROP_ID_LENGTH_BYTE)
        }
    }

    /**
     * Generate a random BigInteger smaller than given n.

     * @param n Upper bound of random number range.
     * *
     * @return Randomized BigInteger
     */
    private fun nextRandomBigInteger(n: BigInteger): BigInteger {
        val rand = Random()
        var result: BigInteger
        do {
            result = BigInteger(n.bitLength(), rand)
        } while (result.compareTo(n) >= 0)
        return result
    }

    internal override fun generateDropIdBytes(): ByteArray {
        val upperBoundary = BigInteger.valueOf(2).pow(usedBits)
        return nextRandomBigInteger(upperBoundary).toByteArray()
    }

}
/**
 * Creates a new random drop id using the whole id namespace.
 * Hence, the collision probability is 1/2^(DROP_ID_LENGTH_BYTE*8)
 */

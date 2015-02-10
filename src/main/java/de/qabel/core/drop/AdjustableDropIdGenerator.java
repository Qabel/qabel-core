package de.qabel.core.drop;

import java.math.BigInteger;
import java.util.Random;

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
public class AdjustableDropIdGenerator extends DropIdGenerator {
	private final int usedBits;

	/**
	 * Creates a new random drop id using the whole id namespace.
	 * Hence, the collision probability is 1/2^(DROP_ID_LENGTH_BYTE*8)
	 */
	public AdjustableDropIdGenerator() {
		this(DROP_ID_LENGTH_BYTE*8);
	}

	/**
	 * Creates a new random drop id using {@code usedBits} number of bits
	 * for the namespace.
	 * Hence, the collision probability is 1/2^(usedBits).
	 *
	 * @param usedBits number of bits used during randomization. Must be in the
	 *            interval (0, DROP_ID_LENGTH_BYTE*8].
	 */
	public AdjustableDropIdGenerator(int usedBits) {
		if (usedBits <= 0 || usedBits > DROP_ID_LENGTH_BYTE * 8) {
			throw new IllegalArgumentException("Used bits must be between 0 and " + DROP_ID_LENGTH_BYTE);
		}
		this.usedBits = usedBits;
	}

	/**
	 * Generate a random BigInteger smaller than given n.
	 * 
	 * @param n Upper bound of random number range.
	 * @return Randomized BigInteger
	 */
	private static BigInteger nextRandomBigInteger(BigInteger n) {
		Random rand = new Random();
		BigInteger result;
		do {
			result = new BigInteger(n.bitLength(), rand);
		} while (result.compareTo(n) >= 0);
		return result;
	}

	@Override
	byte[] generateDropIdBytes() {
		BigInteger upperBoundary = BigInteger.valueOf(2).pow(usedBits);
		return nextRandomBigInteger(upperBoundary).toByteArray();
	}

}

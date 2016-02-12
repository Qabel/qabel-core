package de.qabel.core.drop;

import de.qabel.core.crypto.CryptoUtils;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.util.encoders.Base64;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

public class ProofOfWork {
	/**
	 * For further description of the scheme see http://qabel.github.io/docs/Qabel-Protocol-ProofOfWork/
	 */
	private int leadingZeros;
	private byte[] initVectorServer;
	private byte[] initVectorClient;
	private long time;
	private byte[] messageHash;
	private long counter;
	private byte[] pow;
	static final int longLength = Long.SIZE / Byte.SIZE;
	static final int hashLength = 256/8; //SHA-256

	/**
	 * Initializes PoW
	 */
	public ProofOfWork(int leadingZeros, byte[] initVectorServer, byte[] initVectorClient, long time, byte[] messageHash, long counter, byte[] pow) {
		this.leadingZeros = leadingZeros;
		this.initVectorServer = initVectorServer;
		this.initVectorClient = initVectorClient;
		this.time = time;
		this.messageHash = messageHash;
		this.counter = counter;
		this.pow = pow;
	}

	public long getTime() {
		return time;
	}

	public long getCounter() {
		return counter;
	}

	public String getIVserverB64() {
		return Base64.toBase64String(initVectorServer);
	}

	public String getIVclientB64() {
		return Base64.toBase64String(initVectorClient);
	}

	public String getMessageHashB64() {
		return Base64.toBase64String(messageHash);
	}

	public String getProofOfWorkHashB64() { return Base64.toBase64String(pow); }

	/**
	 * Calculates the PoW for given parameters
	 * @param leadingZeros Number of leading zero bits of PoW hash
	 * @param initVectorServer Server IV which is part of the PoW
	 * @param messageHash hash of message to be sent
	 * @return byte[][]: byte[0]=plain parameters byte[1]=PoW hash
	 */
	public static ProofOfWork calculate(int leadingZeros, byte[] initVectorServer, byte[] messageHash) {
		long time;
		long counter;
		byte[] initVectorClient;
		byte[] pow = new byte[hashLength];
		CryptoUtils cryptoUtils = new CryptoUtils();
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

		//time in seconds since epoch UTC
		time = calendar.getTimeInMillis() / 1000L;
		byte[] timeBytes = toByteArray(time);
		initVectorClient = cryptoUtils.getRandomBytes(16);

		byte[] fix = composeFixParts(initVectorServer, initVectorClient, timeBytes, messageHash);

		//Find counter which fulfills pattern
		counter = calculatePow(pow, fix, leadingZeros);

		return new ProofOfWork(leadingZeros, initVectorServer, initVectorClient, time, messageHash, counter,pow);
	}

	private static byte[] composeFixParts(byte[] initVectorServer, byte[] initVectorClient, byte[] time, byte[] messageHash) {
		byte[] fix = new byte[initVectorServer.length + initVectorClient.length + time.length + messageHash.length];
		int offset = 0;
		System.arraycopy(initVectorServer, 0, fix, offset, initVectorServer.length);
		offset = initVectorServer.length;
		System.arraycopy(initVectorClient, 0, fix, offset, initVectorClient.length);
		offset += initVectorClient.length;
		System.arraycopy(time, 0, fix, offset, time.length);
		offset += time.length;
		System.arraycopy(messageHash, 0, fix, offset, messageHash.length);
		return fix;
	}

	/**
	 * Checks whether hash starts with required leading zero bits
	 * @param hash hash to be verified
	 * @param leadingZeros required leading zeros
	 * @return true of hash starts with required leading zero bits
	 */
	private static boolean enoughZeros(byte[] hash, int leadingZeros) {
		for(int i=0; i<leadingZeros; i++) {
			//check whether i-th bit is zero
			if(((hash[i/8]>>(i%8))&1) != 0) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Finds a valid proof of work hash with leading zeros
	 * @param pow result of the calculation
	 * @param fix fix part of the proof of work
	 * @param leadingZeros required leading zeros
	 * @return counter for the valid hash
	 */
	private static long calculatePow(byte[] pow, byte[] fix, int leadingZeros) {
		SHA256Digest digest = new SHA256Digest();
		long counter = 0;
		if(pow.length == hashLength) {
			digest.update(fix, 0, fix.length);
			digest.update(toByteArray(counter), 0, longLength);
			digest.doFinal(pow, 0);

			while (!enoughZeros(pow, leadingZeros)) {
				counter++;
				digest.reset();
				digest.update(fix, 0, fix.length);
				digest.update(toByteArray(counter), 0, longLength);
				digest.doFinal(pow, 0);
			}
			digest.reset();
			return counter;
		} else {
			return 0;
		}
	}

	static byte[] toByteArray(long number) { return ByteBuffer.allocate(longLength).putLong(number).array(); }
}
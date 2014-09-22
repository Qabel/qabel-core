package de.qabel.core.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public class CryptoUtils {

	private static CryptoUtils instance;

	private final static String asymKeyAlgo = "RSA";
	private final static int asymKeySize = 2048;

	private KeyPairGenerator keyGen;
	private SecureRandom secRandom;
	private MessageDigest messageDigest;

	private CryptoUtils() {

		try {
			secRandom = new SecureRandom();

			keyGen = KeyPairGenerator.getInstance(asymKeyAlgo);
			keyGen.initialize(asymKeySize);

			messageDigest = MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static CryptoUtils getInstance() {
		if (CryptoUtils.instance == null) {
			CryptoUtils.instance = new CryptoUtils();
		}
		return CryptoUtils.instance;
	}

	/*
	 * Generates a new key pair
	 */
	public KeyPair generateKeyPair() {
		return keyGen.generateKeyPair();
	}

	/*
	 * Returns a random byte array with an arbitrary size 
	 */	
	public byte[] getRandomBytes(int numBytes) {
		byte[] ranBytes = new byte[numBytes];
		secRandom.nextBytes(ranBytes);
		return ranBytes;
	}
	
	/*
	 * Returns the SHA512 digest for a byte array
	 */	
	public String getSHA512sum(byte[] bytes){
		byte[] digest = messageDigest.digest(bytes);
		StringBuilder sb = new StringBuilder(191);

		for (int i = 0; i < digest.length - 1; i++) {
			sb.append(String.format("%02x", digest[i] & 0xff));
			sb.append(":");
		}
		sb.append(String.format("%02x", digest[digest.length - 1] & 0xff));

		return sb.toString();
	}
	
	/*
	 * Returns the SHA512 digest for a String
	 */
	public String getSHA512sum(String plain) {
		return getSHA512sum(plain.getBytes());
	}
}

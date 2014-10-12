package de.qabel.core.crypto;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.crypto.BadPaddingException;

import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.qabel.core.crypto.CryptoUtils;

public class CryptoUtilsTest {

	CryptoUtils cu = CryptoUtils.getInstance();
	QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();
	QblPrimaryKeyPair qpkp2 = new QblPrimaryKeyPair();
	String jsonTestString = "{\"version\":1,\"time\":100,\"sender\":20,\"model\":\"de.example.qabel.MailMessage\",\"data\":\"{\"sender\":\"a@a.com\",\"content\":\"hello world\",\"recipient\":\"b@b.com\"}\"}";

	@Rule
	public ExpectedException exception = ExpectedException.none();

	@Test
	public void sha512Test() {
		String str = "The quick brown fox jumps over the lazy dog";
		String digest = cu.getSHA512sumHumanReadable(str);
		String expectedDigest = "07:e5:47:d9:58:6f:6a:73:f7:3f:ba:c0:43:5e:d7:69:51:21:8f:b7:d0:c8:d7:88:a3:09:d7:85:43:6b:bb:64:2e:93:a2:52:a9:54:f2:39:12:54:7d:1e:8a:3b:5e:d6:e1:bf:d7:09:78:21:23:3f:a0:53:8f:3d:b8:54:fe:e6";

		assertEquals(digest, expectedDigest);
	}

	@Test
	public void encryptHybridTest() throws BadPaddingException {
		byte[] cipherText = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());

		assertEquals(
				cu.decryptHybridAndValidateSignature(cipherText, qpkp, qpkp.getQblSignPublicKey()),
				jsonTestString);

	}

	@Test
	public void encryptHybridTestInvalidSignature() {
		byte[] cipherText = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());

		assertNull(cu.decryptHybridAndValidateSignature(cipherText, qpkp,
				qpkp2.getQblSignPublicKey()));
	}

	@Test
	public void encryptDecryptSymmetricTest()
			throws UnsupportedEncodingException {
		BigInteger key = new BigInteger(
				"1122334455667788991011121314151617181920212223242526272829303132",
				16);
		byte[] keyBytes = key.toByteArray();
		String plainTextStr = "Hello this a plaintext, which should be encrypted.";
		byte[] plainTextBytes = plainTextStr.getBytes();

		byte[] cipherTextBytes = cu.encryptSymmetric(plainTextBytes, keyBytes);
		byte[] secondPlainTextBytes = cu.decryptSymmetric(cipherTextBytes,
				keyBytes);

		assertEquals(plainTextStr, new String(secondPlainTextBytes, "UTF-8"));
	}

	@Test
	public void decryptHybridWithWrongKeyTest() throws BadPaddingException {
		// exception.expect(BadPaddingException.class);

		byte[] ciphertext = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());
		assertNull(cu.decryptHybridAndValidateSignature(ciphertext, qpkp2,
				qpkp.getQblSignPublicKey()));

	}

	@Test
	public void encryptSymmetricTest() throws UnsupportedEncodingException {
		BigInteger key = new BigInteger(
				"1122334455667788991011121314151617181920212223242526272829303132",
				16);
		byte[] keyBytes = key.toByteArray();
		String plainTextStr = "Hello this a plaintext, which should be encrypted.";
		byte[] plainTextBytes = plainTextStr.getBytes();

		byte[] cipherTextBytes = cu.encryptSymmetric(plainTextBytes, keyBytes);

		assertEquals(plainTextBytes.length + 16, cipherTextBytes.length);
	}

	@Ignore
	@Test
	public void decryptSymmetricTest() throws UnsupportedEncodingException {
		BigInteger key = new BigInteger(
				"1122334455667788991011121314151617181920212223242526272829303132",
				16);
		byte[] keyBytes = key.toByteArray();
		BigInteger cipherText = new BigInteger(
				"7c27d0161cd5480c63535a24229c10fd2ed2b2653976988453ea7309e6eb454402295f0eaa7189e6e7c9aebe6b43bc1fdf573ffdae6c8495a0f6f6165cec20f00b9e",
				16);
		byte[] cipherTextBytes = cipherText.toByteArray();
		String plainTextStr = "Hello this a plaintext, which should be encrypted.";

		byte[] plainTextBytes = cu.decryptSymmetric(cipherTextBytes, keyBytes);

		assertEquals(cipherTextBytes.length, plainTextBytes.length + 16);
		assertEquals(plainTextStr, new String(plainTextBytes, "UTF-8"));
	}
	
	@Test
	public void calcHmacTest() {
		byte[] key = new BigInteger("0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b0b", 16).toByteArray();
		byte[] text = new BigInteger("4869205468657265", 16).toByteArray();
		byte[] hmac = new BigInteger("87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cdedaa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854", 16).toByteArray();
		byte[] hmacResult = cu.calcHmac(text, key);
		
		assertArrayEquals(hmac, hmacResult);
	}
	
	@Test
	public void autheticatedEnDecryptionSymmetricTest() {
		
	}
}

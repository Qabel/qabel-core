package de.qabel.core.crypto;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import javax.crypto.BadPaddingException;

import org.bouncycastle.util.encoders.Hex;
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
	public void decryptHybridWithWrongKeyTest() throws BadPaddingException {
		// exception.expect(BadPaddingException.class);

		byte[] ciphertext = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());
		assertNull(cu.decryptHybridAndValidateSignature(ciphertext, qpkp2,
				qpkp.getQblSignPublicKey()));

	}

	@Test
	public void symmetricCryptoTest() throws UnsupportedEncodingException {
		byte[] key = Hex.decode("1122334455667788991011121314151617181920212223242526272829303132");
		String plainText = "Hello this a plaintext, which should be encrypted.";

		byte[] cipherText = cu.encryptSymmetric(plainText.getBytes(), key);
		byte[] plainTextTwo = cu.decryptSymmetric(cipherText, key);
		assertEquals(Hex.toHexString(plainText.getBytes()), Hex.toHexString(plainTextTwo));
	}
	
	@Test
	public void calcHmacTest() throws UnsupportedEncodingException {
		// Test case from http://www.ietf.org/rfc/rfc4231.txt
		byte[] key = Hex.decode("0102030405060708090a0b0c0d0e0f10111213141516171819");
		byte[] text = Hex.decode("cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd");
		byte[] hmac = Hex.decode("b0ba465637458c6990e5a8c5f61d4af7e576d97ff94b872de76f8050361ee3dba91ca5c11aa25eb4d679275cc5788063a5f19741120c4f2de2adebeb10a298dd");
		byte[] hmacResult = cu.calcHmac(text, key);
		assertArrayEquals(hmac, hmacResult);
	}
	
	@Test
	public void autheticatedSymmetricCryptoTest() throws UnsupportedEncodingException {
		byte[] key = Hex.decode("1122334455667788991011121314151617181920212223242526272829303132");
		String plainText = "Hello this a plaintext, which should be encrypted.";

		byte[] cipherText = cu.encryptAuthenticatedSymmetric(plainText.getBytes(), key);
		byte[] plainTextTwo = cu.decryptAuthenticatedSymmetric(cipherText, key);
		assertEquals(Hex.toHexString(plainText.getBytes()), Hex.toHexString(plainTextTwo));
	}
}

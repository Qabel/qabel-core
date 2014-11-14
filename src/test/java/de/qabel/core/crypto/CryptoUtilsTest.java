package de.qabel.core.crypto;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;

import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.util.encoders.Hex;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.qabel.core.crypto.CryptoUtils;

public class CryptoUtilsTest {
	
	private final static String SYMM_KEY_ALGORITHM = "AES";

	final CryptoUtils cu = new CryptoUtils();
	final QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();
	final QblPrimaryKeyPair qpkp2 = new QblPrimaryKeyPair();
	final String jsonTestString = "{\"version\":1,\"time\":100,\"sender\":20,\"model\":\"de.example.qabel.MailMessage\",\"data\":\"{\"sender\":\"a@a.com\",\"content\":\"hello world\",\"recipient\":\"b@b.com\"}\"}";

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
	public void encryptHybridTest() throws BadPaddingException, InvalidKeyException {
		byte[] cipherText = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());

		assertEquals(
				cu.decryptHybridAndValidateSignature(cipherText, qpkp, qpkp.getQblSignPublicKey()),
				jsonTestString);

	}

	@Test
	public void encryptHybridTestInvalidSignature() throws InvalidKeyException {
		byte[] cipherText = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());

		assertNull(cu.decryptHybridAndValidateSignature(cipherText, qpkp,
				qpkp2.getQblSignPublicKey()));
	}

	@Test
	public void decryptHybridWithWrongKeyTest() throws BadPaddingException, InvalidKeyException {
		// exception.expect(BadPaddingException.class);

		byte[] ciphertext = cu.encryptHybridAndSign(jsonTestString,
				qpkp.getQblEncPublicKey(), qpkp.getSignKeyPairs());
		assertNull(cu.decryptHybridAndValidateSignature(ciphertext, qpkp2,
				qpkp.getQblSignPublicKey()));

	}

	@Test
	public void symmetricCryptoTest() throws UnsupportedEncodingException {
		// Test case from http://tools.ietf.org/html/rfc3686
		SecretKeySpec key = new SecretKeySpec(Hex.decode("F6D66D6BD52D59BB0796365879EFF886C66DD51A5B6A99744B50590C87A23884")
				,SYMM_KEY_ALGORITHM);
		byte[] nonce = Hex.decode("00FAAC24C1585EF15A43D875");
		byte[] plainText = Hex.decode("000102030405060708090A0B0C0D0E0F101112131415161718191A1B1C1D1E1F");
		byte[] cipherTextExpected = Hex.decode("F05E231B3894612C49EE000B804EB2A9B8306B508F839D6A5530831D9344AF1C");

		byte[] cipherText = cu.encryptSymmetric(plainText, key, nonce);
		byte[] plainTextTwo = cu.decryptSymmetric(cipherText, key);
		assertEquals(Hex.toHexString(nonce) + Hex.toHexString(cipherTextExpected),Hex.toHexString(cipherText));
		assertEquals(Hex.toHexString(plainText), Hex.toHexString(plainTextTwo));
	}
	
	@Test
	public void calcHmacTest() throws UnsupportedEncodingException {
		// Test case from http://www.ietf.org/rfc/rfc4231.txt
		SecretKeySpec key = new SecretKeySpec(Hex.decode("0102030405060708090a0b0c0d0e0f10111213141516171819"), SYMM_KEY_ALGORITHM);
		byte[] text = Hex.decode("cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd");
		byte[] hmac = Hex.decode("b0ba465637458c6990e5a8c5f61d4af7e576d97ff94b872de76f8050361ee3dba91ca5c11aa25eb4d679275cc5788063a5f19741120c4f2de2adebeb10a298dd");
		byte[] hmacResult = cu.calcHmac(text, key);
		assertArrayEquals(hmac, hmacResult);
	}
	
	@Test
	public void hmacValidationTest() {
		// Test case from http://www.ietf.org/rfc/rfc4231.txt
		SecretKeySpec key = new SecretKeySpec(Hex.decode("0102030405060708090a0b0c0d0e0f10111213141516171819"), SYMM_KEY_ALGORITHM);
		byte[] text = Hex.decode("cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd");
		byte[] hmac = Hex.decode("b0ba465637458c6990e5a8c5f61d4af7e576d97ff94b872de76f8050361ee3dba91ca5c11aa25eb4d679275cc5788063a5f19741120c4f2de2adebeb10a298dd");
		boolean hmacValidation = cu.validateHmac(text, hmac, key);
		assertEquals(hmacValidation, true);
	}
	
	@Test
	public void invalidHmacValidationTest() {
		// Test case from http://www.ietf.org/rfc/rfc4231.txt
		SecretKeySpec key = new SecretKeySpec(Hex.decode("0102030405060708090a0b0c0d0e0f10111213141516171819"), SYMM_KEY_ALGORITHM);
		byte[] text = Hex.decode("cdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcdcd");
		byte[] wrongHmac = Hex.decode("a1aa465637458c6990e5a8c5f61d4af7e576d97ff94b872de76f8050361ee3dba91ca5c11aa25eb4d679275cc5788063a5f19741120c4f2de2adebeb10a298dd");
		boolean wrongHmacValidaition = cu.validateHmac(text, wrongHmac, key);
		assertEquals(wrongHmacValidaition, false);
	}

	@Test
	public void autheticatedSymmetricCryptoTest() throws UnsupportedEncodingException {
		// Test case from http://csrc.nist.gov/groups/ST/toolkit/BCM/documents/proposedmodes/gcm/gcm-spec.pdf
		SecretKeySpec key = new SecretKeySpec(Hex.decode("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308"), SYMM_KEY_ALGORITHM);
		byte[] nonce = Hex.decode("cafebabefacedbaddecaf888");
		byte[] plainText = Hex.decode("d9313225f88406e5a55909c5aff5269a86a7a9531534f7da2e4c303d8a318a721c3c0c95956809532fcf0e2449a6b525b16aedf5aa0de657ba637b391aafd255");
		byte[] cipherTextExpected = Hex.decode("522dc1f099567d07f47f37a32a84427d643a8cdcbfe5c0c97598a2bd2555d1aa8cb08e48590dbb3da7b08b1056828838c5f61e6393ba7a0abcc9f662898015ad");
		byte[] authenticationTagExpected = Hex.decode("b094dac5d93471bdec1a502270e3cc6c");
		
		byte[] cipherText = cu.encryptAuthenticatedSymmetric(plainText, key, nonce);
		byte[] plainTextTwo = cu.decryptAuthenticatedSymmetricAndValidateTag(cipherText, key);
		assertEquals(Hex.toHexString(nonce) + Hex.toHexString(cipherTextExpected) + Hex.toHexString(authenticationTagExpected), Hex.toHexString(cipherText));
		assertEquals(Hex.toHexString(plainText), Hex.toHexString(plainTextTwo));
	}
	
	@Test
	public void invalidAutheticatedSymmetricCryptoTest() throws UnsupportedEncodingException {
		// Test case from http://csrc.nist.gov/groups/ST/toolkit/BCM/documents/proposedmodes/gcm/gcm-spec.pdf
		SecretKeySpec key = new SecretKeySpec(Hex.decode("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308"), SYMM_KEY_ALGORITHM);
		String nonce = "cafebabefacedbaddecaf888";
		String encryptedPlainText = "522dc1f099567d07f47f37a32a84427d643a8cdcbfe5c0c97598a2bd2555d1aa8cb08e48590dbb3da7b08b1056828838c5f61e6393ba7a0abcc9f662898015ad";
		String ivalidAuthenticationTag = "a194dac5d93471bdec1a502270e3cc6c";
		byte[] cipherText = Hex.decode(nonce + encryptedPlainText + ivalidAuthenticationTag);
		
		byte[] plainText = cu.decryptAuthenticatedSymmetricAndValidateTag(cipherText, key);
		assertEquals(plainText, null);
	}
}

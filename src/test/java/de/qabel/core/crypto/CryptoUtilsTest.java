package de.qabel.core.crypto;

import static org.junit.Assert.*;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

import org.junit.Test;

import de.qabel.core.crypto.CryptoUtils;

public class CryptoUtilsTest {

	CryptoUtils cu = CryptoUtils.getInstance();

	@Test
	public void sha512Test() {

		String str = "The quick brown fox jumps over the lazy dog";
		String digest = cu.getSHA512sumHumanReadable(str);
		String expectedDigest = "07:e5:47:d9:58:6f:6a:73:f7:3f:ba:c0:43:5e:d7:69:51:21:8f:b7:d0:c8:d7:88:a3:09:d7:85:43:6b:bb:64:2e:93:a2:52:a9:54:f2:39:12:54:7d:1e:8a:3b:5e:d6:e1:bf:d7:09:78:21:23:3f:a0:53:8f:3d:b8:54:fe:e6";

		assertEquals(digest, expectedDigest);
	}

	@Test
	public void rsaEncryptForRecipientTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();

		String secretMessage = "SecretMessage";

		byte[] ciphertext = cu.rsaEncryptForRecipient(secretMessage.getBytes(),
				qpkp.getQblEncPublicKey());

		String decryptedMessage = new String(cu.rsaDecrypt(ciphertext,
				qpkp.getQblEncPrivateKey()));

		assertTrue(secretMessage.equals(decryptedMessage));
	}
	
    @Test
    public void symmEncTest() throws UnsupportedEncodingException {
    	BigInteger key = new BigInteger("1122334455667788991011121314151617181920212223242526272829303132", 16);
    	byte[] keyBytes = key.toByteArray();
    	String plainTextStr = "Hello this a plaintext, which should be encrypted.";
    	byte[] plainTextBytes = plainTextStr.getBytes();
    	
    	byte[] cipherTextBytes = cu.symmEncrypt(plainTextBytes, keyBytes);
    	
    	assertEquals(plainTextBytes.length+16, cipherTextBytes.length);
    }
    
    @Test
    public void symmDecTest() throws UnsupportedEncodingException {
    	BigInteger key = new BigInteger("1122334455667788991011121314151617181920212223242526272829303132", 16);
    	byte[] keyBytes = key.toByteArray();
    	BigInteger cipherText = new BigInteger("7c27d0161cd5480c63535a24229c10fd2ed2b2653976988453ea7309e6eb454402295f0eaa7189e6e7c9aebe6b43bc1fdf573ffdae6c8495a0f6f6165cec20f00b9e", 16);
    	byte[] cipherTextBytes = cipherText.toByteArray();
    	String plainTextStr = "Hello this a plaintext, which should be encrypted.";
    	
    	byte[] plainTextBytes = cu.symmDecrypt(cipherTextBytes, keyBytes);
    	
    	assertEquals(cipherTextBytes.length, plainTextBytes.length+16);
    	assertEquals(plainTextStr, new String(plainTextBytes, "UTF-8"));
    }
    
    @Test
	public void encryptMessageTest() {
		QblPrimaryKeyPair qpkp = new QblPrimaryKeyPair();
		String plaintext = "{\"version\":1,\"time\":100,\"sender\":20,\"model\":\"de.example.qabel.MailMessage\",\"data\":\"{\"sender\":\"a@a.com\",\"content\":\"hello world\",\"recipient\":\"b@b.com\"}\"}";

		byte[] cipherText = cu.encryptMessage(plaintext,
				qpkp.getQblEncPublicKey());

		assertEquals(cu.decryptMessage(cipherText, qpkp.getQblEncPrivateKey()),
				plaintext);

	}
}

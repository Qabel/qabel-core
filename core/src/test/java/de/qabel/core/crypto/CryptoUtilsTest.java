package de.qabel.core.crypto;

import org.junit.Before;
import org.junit.Test;
import org.spongycastle.crypto.InvalidCipherTextException;
import org.spongycastle.crypto.params.KeyParameter;
import org.spongycastle.util.encoders.Hex;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidKeyException;

import static org.junit.Assert.*;

public class CryptoUtilsTest {
    final CryptoUtils cu = new CryptoUtils();
    private String testFileName = "src/test/java/de/qabel/core/crypto/testFile";

    @Before
    public void setUp() throws Exception {
        if (!new File(testFileName).exists()) {
            testFileName = "core/" + testFileName;
        }
    }

    @Test
    public void fileDecryptionTest() throws IOException, InvalidKeyException {
        KeyParameter key = new KeyParameter(Hex.decode("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308"));
        byte[] nonce = Hex.decode("cafebabefacedbaddecaf888");
        File testFileEnc = new File(testFileName + ".enc");
        File testFileDec = new File(testFileName + ".dec");

        // create encrypted file for decryption test
        cu.encryptFileAuthenticatedSymmetric(new File(testFileName), new FileOutputStream(testFileEnc), key, nonce);

        FileInputStream cipherStream = new FileInputStream(testFileEnc);

        cu.decryptFileAuthenticatedSymmetricAndValidateTag(cipherStream, testFileDec, key);

        try {
            assertEquals(Hex.toHexString(Files.readAllBytes(Paths.get(testFileName))),
                Hex.toHexString(Files.readAllBytes(testFileDec.toPath())));
        } finally {
            testFileEnc.delete();
            testFileDec.delete();
        }
    }

    @Test
    public void allowsForConcurrentDecryption() throws Exception {
        final KeyParameter key = new KeyParameter(Hex.decode("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308"));
        byte[] nonce = Hex.decode("cafebabefacedbaddecaf888");
        final File testFileEnc = new File(testFileName + ".enc");

        // create encrypted file for decryption test
        cu.encryptFileAuthenticatedSymmetric(new File(testFileName), new FileOutputStream(testFileEnc), key, nonce);

        final DelayedStream cipherStream1 = new DelayedStream(new FileInputStream(testFileEnc));

        final File testFileDec = new File(testFileName + ".dec");
        Thread decryptor = new Thread() {
            @Override
            public void run() {
                try {
                    cu.decryptFileAuthenticatedSymmetricAndValidateTag(cipherStream1, testFileDec, key);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        decryptor.start();
        while (!cipherStream1.isBlocked()) {
            Thread.sleep(10);
        }

        File testFileDec2 = new File(testFileName + ".dec2");
        InputStream cipherStream2 = new FileInputStream(testFileEnc);
        cu.decryptFileAuthenticatedSymmetricAndValidateTag(cipherStream2, testFileDec2, key);
        cipherStream1.setBlockOnRead(false);
        cipherStream1.unblock();
        decryptor.join();

        try {
            assertEquals(Hex.toHexString(Files.readAllBytes(Paths.get(testFileName))),
                Hex.toHexString(Files.readAllBytes(testFileDec.toPath())));
            assertEquals(Hex.toHexString(Files.readAllBytes(Paths.get(testFileName))),
                Hex.toHexString(Files.readAllBytes(testFileDec2.toPath())));
        } finally {
            testFileEnc.delete();
            testFileDec.delete();
            testFileDec2.delete();
        }
    }

    @Test
    public void fileFailingDecryptionTest() throws IOException, InvalidKeyException {
        KeyParameter key = new KeyParameter(Hex.decode("feffe9928665731c6d6a8f9467308308feffe9928665731c6d6a8f9467308308"));
        byte[] nonce = Hex.decode("cafebabefacedbaddecaf888");
        File testFileEnc = new File(testFileName + ".enc");
        File testFileDec = new File(testFileName + ".dec");

        // create encrypted file for decryption test
        cu.encryptFileAuthenticatedSymmetric(new File(testFileName), new FileOutputStream(testFileEnc), key, nonce);

        RandomAccessFile modTestFileEnc = new RandomAccessFile(testFileEnc, "rws");
        modTestFileEnc.seek(nonce.length);
        modTestFileEnc.write(Hex.decode("EC40"));
        modTestFileEnc.close();

        FileInputStream cipherStream = new FileInputStream(testFileEnc);
        boolean result = cu.decryptFileAuthenticatedSymmetricAndValidateTag(cipherStream, testFileDec, key);

        testFileEnc.delete();
        testFileDec.delete();

        assertFalse(result);
    }

    /**
     * Test data from "Cryptography in NaCl" paper (http://cr.yp.to/highspeed/naclcrypto-20090310.pdf)
     */
    @Test
    public void ecPointConstructionTest() {
        byte[] randomDataForPrivateKey = Hex.decode("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a");
        byte[] expectedPublicKey = Hex.decode("8520f0098930a754748b7ddcb43ef75a0dbf3a0d26381af4eba4a98eaa9b4e6a");

        QblECKeyPair testKey = new QblECKeyPair(randomDataForPrivateKey);
        assertArrayEquals(expectedPublicKey, testKey.getPub().getKey());
    }

    /**
     * Test data from "Cryptography in NaCl" paper (http://cr.yp.to/highspeed/naclcrypto-20090310.pdf)
     */
    @Test
    public void dhKeyAgreementTestNaClVector() {
        QblECKeyPair aliceKey = new QblECKeyPair(Hex.decode("77076d0a7318a57d3c16c17251b26645df4c2f87ebc0992ab177fba51db92c2a"));
        QblECKeyPair bobKey = new QblECKeyPair(Hex.decode("5dab087e624a8a4b79e17f8b83800ee66f3bb1292618b6fd1c2f8b27ff88e0eb"));
        byte[] expectedSharedSecret = Hex.decode("4a5d9d5ba4ce2de1728e3bf480350f25e07e21c947d19e3376f09b3c1e161742");
        byte[] sharedSecAlice = aliceKey.ECDH(bobKey.getPub());
        byte[] sharedSecBob = bobKey.ECDH(aliceKey.getPub());

        assertArrayEquals(expectedSharedSecret, sharedSecAlice);
        assertArrayEquals(expectedSharedSecret, sharedSecBob);
    }

    @Test
    public void dhKeyAgreementTest() {
        QblECKeyPair aliceKey = new QblECKeyPair(Hex.decode("5AC99F33632E5A768DE7E81BF854C27C46E3FBF2ABBACD29EC4AFF517369C660"));
        QblECKeyPair bobKey = new QblECKeyPair(Hex.decode("47DC3D214174820E1154B49BC6CDB2ABD45EE95817055D255AA35831B70D3260"));
        byte[] sharedSecAlice = aliceKey.ECDH(bobKey.getPub());
        byte[] sharedSecBob = bobKey.ECDH(aliceKey.getPub());

        assertArrayEquals(sharedSecAlice, sharedSecBob);
    }

    @Test
    public void dhKeyAgreementTestRandomKeys() {
        QblECKeyPair aliceKey = new QblECKeyPair();
        QblECKeyPair bobKey = new QblECKeyPair();
        byte[] sharedSecAlice = aliceKey.ECDH(bobKey.getPub());
        byte[] sharedSecBob = bobKey.ECDH(aliceKey.getPub());

        assertArrayEquals(sharedSecAlice, sharedSecBob);
    }

    @Test
    public void noiseTest() throws InvalidKeyException, InvalidCipherTextException {
        CryptoUtils cu = new CryptoUtils();
        QblECKeyPair aliceKey = new QblECKeyPair();
        QblECKeyPair bobKey = new QblECKeyPair();
        byte[] ciphertext = cu.createBox(aliceKey, bobKey.getPub(), "n0i$e".getBytes(), 0);
        DecryptedPlaintext plaintext = cu.readBox(bobKey, ciphertext);
        assertEquals("n0i$e", new String(plaintext.getPlaintext()));
    }

    @Test
    public void noiseTestWithNullData() throws InvalidKeyException, InvalidCipherTextException {
        CryptoUtils cu = new CryptoUtils();
        QblECKeyPair aliceKey = new QblECKeyPair();
        QblECKeyPair bobKey = new QblECKeyPair();
        byte[] ciphertext = cu.createBox(aliceKey, bobKey.getPub(), null, 0);
        DecryptedPlaintext plaintext = cu.readBox(bobKey, ciphertext);
        assertEquals("", new String(plaintext.getPlaintext()));
    }

    @Test
    public void noiseTestNegativePadLength() throws InvalidKeyException, InvalidCipherTextException {
        CryptoUtils cu = new CryptoUtils();
        QblECKeyPair aliceKey = new QblECKeyPair();
        QblECKeyPair bobKey = new QblECKeyPair();
        byte[] ciphertext = cu.createBox(aliceKey, bobKey.getPub(), "n0i$e".getBytes(), -1);
        DecryptedPlaintext plaintext = cu.readBox(bobKey, ciphertext);
        assertEquals("n0i$e", new String(plaintext.getPlaintext()));
    }

    @Test
    public void noiseBoxFromGoImplementation() throws InvalidCipherTextException, InvalidKeyException {
        CryptoUtils cu = new CryptoUtils();
        String expectedPlainText = "yellow submarines";
        byte[] box = Hex.decode("539edb6df8541fb8e56c97c6a8cd061fe1c6c874a374d8501f8a285ed5ec092244178f74e77071918e3f2c3e3d2a256916c33a85f409844bbd1b749719b2f2e71e210f763928d856479e7078cb0413e1e25f3e6685caaee9d10b2a0756d7c1769ccad1ee13bcbaf1186cec727a94b01e2be042da07");
        byte[] bobKey = Hex.decode("782e3b1ea317f7f808e1156d1282b4e7d0e60e4b7c0f205a5ce804f0a1a3a155");
        QblECKeyPair qblBobKey = new QblECKeyPair(bobKey);
        DecryptedPlaintext plaintext = cu.readBox(qblBobKey, box);
        assertEquals(expectedPlainText, new String(plaintext.getPlaintext()));
    }

    @Test
    public void noiseBoxFromGoImplementationWithPadding() throws InvalidCipherTextException, InvalidKeyException {
        CryptoUtils cu = new CryptoUtils();
        String expectedPlainText = "orange submarine";
        byte[] expectedSenderKey = Hex.decode("2be41e402667281cfe50699fed0b5d73f753392a6dc277126bd0bfb5217dcf33");
        byte[] box = Hex.decode("a63794c4f7033b9c769023f28c12390a7b89296452a4695e35a952625839ae2d9d19715ba2130a6ae49aaf0ea5ab3eacededbb7676724618abb1fe648328086ed253a75d9672540c319114c4891cc6a1356ae7a8f3c9866c704b145efaa0313c9e52f609a4f6c41070ad4741c3ef637e7b7e0a7a7b03a0261607a9");
        byte[] bobKey = Hex.decode("a0c2b2bcb68bbe50b01181bfbcbff28ee00f37e44103d3a591dbae6cd5fb9f6a");
        QblECKeyPair qblBobKey = new QblECKeyPair(bobKey);
        DecryptedPlaintext plaintext = cu.readBox(qblBobKey, box);
        assertEquals(expectedPlainText, new String(plaintext.getPlaintext()));
        assertArrayEquals(plaintext.getSenderKey().getKey(), expectedSenderKey);
    }

    @Test
    public void decryptTest() throws InvalidCipherTextException {
        // These values were extracted from the internal state of the DH1 phase created by noiseBoxFromGoImplementation()
        CryptoUtils cu = new CryptoUtils();
        byte[] key = Hex.decode("120c64583cc9831cedf6b0ffa3cb003c1a3cc057c8f40e3f6fb7f9e376beba43");
        byte[] nonce = Hex.decode("f5a57de46ff8daee400942c5");
        byte[] cipherText = Hex.decode("44178f74e77071918e3f2c3e3d2a256916c33a85f409844bbd1b749719b2f2e71e210f763928d856479e7078cb0413e1");
        byte[] aad = Hex.decode("1def84acf2c1e5ae04bff2a67b0668bb2c9a285e5c5e033f00c227466c8d022b539edb6df8541fb8e56c97c6a8cd061fe1c6c874a374d8501f8a285ed5ec0922");
        byte[] expectedPlainText = Hex.decode("1f5349c16e430d7685d56437734d9346c3c842e4a873034d489f480a68e2ed25");
        KeyParameter keyParameter = new KeyParameter(key);

        byte[] plainText = cu.decrypt(keyParameter, nonce, cipherText, aad);

        assertArrayEquals(expectedPlainText, plainText);
    }
}

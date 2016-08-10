package de.qabel.core.crypto

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.spongycastle.crypto.CipherKeyGenerator
import org.spongycastle.crypto.InvalidCipherTextException
import org.spongycastle.crypto.KeyGenerationParameters
import org.spongycastle.crypto.digests.SHA512Digest
import org.spongycastle.crypto.engines.AESEngine
import org.spongycastle.crypto.macs.HMac
import org.spongycastle.crypto.modes.GCMBlockCipher
import org.spongycastle.crypto.params.AEADParameters
import org.spongycastle.crypto.params.KeyParameter

import java.io.*
import java.nio.ByteBuffer
import java.security.InvalidKeyException
import java.security.SecureRandom
import java.util.Arrays

class CryptoUtils {

    private val secRandom: SecureRandom
    private val keyGenerator: CipherKeyGenerator

    init {
        secRandom = SecureRandom()

        //New key generator needs random for initialization
        keyGenerator = CipherKeyGenerator()
        keyGenerator.init(KeyGenerationParameters(secRandom, AES_KEY_SIZE_BIT))
    }

    /**
     * Returns a random byte array with an arbitrary size

     * @param numBytes Number of random bytes
     * *
     * @return byte[ ] with random bytes
     */
    fun getRandomBytes(numBytes: Int): ByteArray {
        val ranBytes = ByteArray(numBytes)
        secRandom.nextBytes(ranBytes)
        return ranBytes
    }

    /**
     * Encrypts a File to an OutputStream. The OutputStream gets the result
     * immediately while encrypting. The step size of every seperate decryption
     * step is defined in SYMM_ALT_READ_SIZE_BYTE. Nonce of size
     * SYMM_NONCE_SIZE_BIT is taken as nonce directly, else a random nonce is
     * generated.

     * @param file         Input file that will be encrypted
     * *
     * @param outputStream OutputStream where ciphertext is streamed to
     * *
     * @param key          Key which is used to en-/decrypt
     * *
     * @param nonce        Random value which is concatenated to a counter
     * *
     * @return true if encryption worked as expected, else false
     * *
     * @throws InvalidKeyException if key is invalid
     */
    @Throws(InvalidKeyException::class, FileNotFoundException::class)
    @JvmOverloads fun encryptFileAuthenticatedSymmetric(file: File, outputStream: OutputStream, key: KeyParameter, nonce: ByteArray? = null): Boolean {
        val fileInputStream = FileInputStream(file)
        return encryptStreamAuthenticatedSymmetric(fileInputStream, outputStream, key, nonce)
    }

    /**
     * Encrypts an InputStream to an OutputStream. The OutputStream gets the result
     * immediately while encrypting. The step size of every separate decryption
     * step is defined in SYMM_GCM_READ_SIZE_BYTE. Nonce of size
     * SYMM_NONCE_SIZE_BIT is taken as nonce directly, else a random nonce is
     * generated.

     * @param inputStream  InputStream that will be encrypted
     * *
     * @param outputStream OutputStream where ciphertext is streamed to
     * *
     * @param key          Key which is used to en-/decrypt
     * *
     * @param nonce        Random value which is concatenated to a counter
     * *
     * @return true if encryption worked as expected, else false
     * *
     * @throws InvalidKeyException if key is invalid
     */
    @Throws(InvalidKeyException::class)
    fun encryptStreamAuthenticatedSymmetric(inputStream: InputStream, outputStream: OutputStream,
                                            key: KeyParameter, nonce: ByteArray?): Boolean {
        var nonce = nonce
        val cipherText = DataOutputStream(outputStream)
        val tempIn = ByteArray(SYMM_GCM_READ_SIZE_BYTE)
        val tempOut = ByteArray(SYMM_GCM_READ_SIZE_BYTE)
        var usedBytes: Int

        if (nonce == null || nonce.size != SYMM_NONCE_SIZE_BYTE) {
            nonce = getRandomBytes(SYMM_NONCE_SIZE_BYTE)
        }

        val gcmCipher = GCMBlockCipher(AESEngine())
        try {
            gcmCipher.init(true, AEADParameters(key, MAC_BIT, nonce, null))
        } catch (e: IllegalArgumentException) {
            logger.debug("Encryption: Wrong parameters for file encryption cipher.", e)
            return false
        }

        try {
            cipherText.write(nonce)
            while ((usedBytes = inputStream.read(tempIn)) > 0) {
                usedBytes = gcmCipher.processBytes(tempIn, 0, usedBytes, tempOut, 0)
                cipherText.write(tempOut, 0, usedBytes)
            }
            usedBytes = gcmCipher.doFinal(tempOut, 0)
            cipherText.write(tempOut, 0, usedBytes)
            inputStream.close()
        } catch (e: InvalidCipherTextException) {
            // Should not happen
            logger.debug("Encryption: Block size of cipher was illegal => code mistake.", e)
        } catch (e: IOException) {
            logger.debug("Encryption: Input/output Stream cannot be written/read to/from.", e)
            return false
        }

        return true
    }

    /**
     * Decrypts ciphertext from an InputStream to a file. The decrypted content
     * is written to the file immediately. If decryption was successful true
     * is returned, if authentication tag validation fails or another error
     * occurs false is returned.

     * @param inputStream InputStream from where the ciphertext is read
     * *
     * @param file        File in which the decrypted stream is stored
     * *
     * @param key         Key which is used to en-/decrypt the file
     * *
     * @return true if successfully decrypted or false if authentication tag validation
     * * failed or another error occurred
     * *
     * @throws InvalidKeyException if key is invalid
     */
    @Throws(InvalidKeyException::class, IOException::class)
    fun decryptFileAuthenticatedSymmetricAndValidateTag(inputStream: InputStream, file: File, key: KeyParameter): Boolean {
        val nonce = ByteArray(SYMM_NONCE_SIZE_BYTE)
        val tempIn = ByteArray(SYMM_GCM_READ_SIZE_BYTE)
        val tempOut = ByteArray(SYMM_GCM_READ_SIZE_BYTE)
        val bufferedInput = BufferedInputStream(inputStream)
        var usedBytes: Int

        try {
            bufferedInput.read(nonce)
        } catch (e: IOException) {
            logger.debug("Decryption: Ciphertext (in this case the nonce) can not be read.", e)
            throw e
        }

        val gcmCipher = GCMBlockCipher(AESEngine())
        try {
            gcmCipher.init(false, AEADParameters(key, MAC_BIT, nonce, null))
        } catch (e: IllegalArgumentException) {
            throw RuntimeException("Decryption: Wrong parameters for file decryption.", e)
        }

        val fileOutput = FileOutputStream(file)
        try {
            while ((usedBytes = bufferedInput.read(tempIn, 0,
                    SYMM_GCM_READ_SIZE_BYTE)) > 0) {
                /*
				 * reading from a buffered input stream ensures that enough bytes
				 * are read to fulfill the block cipher min. length requirements.
				 */
                usedBytes = gcmCipher.processBytes(tempIn, 0, usedBytes, tempOut, 0)
                fileOutput.write(tempOut, 0, usedBytes)
            }
            try {
                usedBytes = gcmCipher.doFinal(tempOut, 0)
                fileOutput.write(tempOut, 0, usedBytes)
            } catch (e: InvalidCipherTextException) {
                logger.error("Decryption: Either cipher text is too short or authentication tag is invalid!", e)
                // truncate file to avoid leakage of incomplete or unauthenticated data
                fileOutput.channel.truncate(0)
                return false
            }

        } finally {
            fileOutput.close()
        }

        return true
    }

    /**
     * Generates a new symmetric key for encryption.

     * @return new symmetric key.
     */
    fun generateSymmetricKey(): KeyParameter {
        return KeyParameter(keyGenerator.generateKey())
    }

    /**
     * Noise Key derivation function. Outputs a byte sequence that the caller typically splits into multiple variables
     * such as a chain variable and cipher context, or two cipher contexts.

     * @param secret      secret for key derivation
     * *
     * @param extraSecret is used to pass a chaining variable to mix into the KDF.
     * *
     * @param info        ensures that applying the KDF to the same secret values will produce independent output,
     * *                    provided 'info' is different.
     * *
     * @param outputLen   length out the output
     * *
     * @return derived key or null on unexpected errors
     * *
     * @throws InvalidKeyException if secret cannot be used as a HMAC secret
     */
    @Throws(InvalidKeyException::class)
    internal fun kdf(secret: ByteArray, extraSecret: ByteArray, info: ByteArray, outputLen: Int): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        val t = ByteArray(H_LEN)

        // Not required to init the ByteArrayOutputStream with the
        // expected length, but it might improve the performance
        val key = KeyParameter(secret)
        val bs = ByteArrayOutputStream(info.size + 1 + 32 + extraSecret.size)
        val hmac = HMac(SHA512Digest())
        var c = 0
        while (c <= Math.ceil(outputLen.toDouble() / H_LEN) - 1) {
            try {
                bs.write(info)
                bs.write(c)
                bs.write(t, 0, 32)
                bs.write(extraSecret)

                hmac.init(key)
                hmac.update(bs.toByteArray(), 0, bs.size())
                hmac.doFinal(t, 0)
                outputStream.write(t)
                bs.reset()
            } catch (e: IOException) {
                // Should never occur
                logger.error("Cannot write to ByteArrayOutputStream!", e)
                return null
            }

            c++
        }
        return Arrays.copyOfRange(outputStream.toByteArray(), 0, outputLen)
    }

    /**
     * Noise box is the structured anonymised encryption with the use of ECDH of
     * receivers and an ephemeral key. Schematic:
     * c = A' || enc(DH(A',B), A) || enc(DH(A,B)||DH(A',B), m)

     * @param senderKey    senders key pair
     * *
     * @param targetPubKey receivers public key
     * *
     * @param appData      appData which will be decrypted
     * *
     * @param padLen       length of padding added to the box. Negative values are ignored.
     * *
     * @return ciphertext of mentioned format
     * *
     * @throws InvalidKeyException if kdf cannot distribute a key from DH of given EC keys
     */
    @Throws(InvalidKeyException::class)
    fun createBox(senderKey: QblECKeyPair, targetPubKey: QblECPublicKey, appData: ByteArray?, padLen: Int): ByteArray {
        var appData = appData
        var padLen = padLen
        if (appData == null) {
            appData = ByteArray(0)
        }

        val ephKey = QblECKeyPair()
        val key1: ByteArrayInputStream
        val key2: ByteArrayInputStream
        val header = ByteArrayOutputStream()
        val body = ByteArrayOutputStream()
        val noiseBox = ByteArrayOutputStream()
        val authtext = ByteArrayOutputStream()
        val paddedPlaintext = ByteArrayOutputStream(appData.size + PADDING_LEN_BYTES)
        val cv1 = ByteArray(CV_LEN_BYTE)
        val symmKey1 = ByteArray(SYMM_KEY_LEN_BYTE)
        val nonce1 = ByteArray(NONCE_LEN_BYTE)
        val symmKey2 = ByteArray(SYMM_KEY_LEN_BYTE)
        val nonce2 = ByteArray(NONCE_LEN_BYTE)
        val dh1 = ephKey.ECDH(targetPubKey)
        val dh2 = senderKey.ECDH(targetPubKey)
        val info = Arrays.copyOf(SUITE_NAME, SUITE_NAME.size + 1)
        try {
            // disable negative padding
            if (padLen < 0) {
                padLen = 0
            }
            val encryptedPaddingLen = ByteBuffer.allocate(PADDING_LEN_BYTES).putInt(padLen).array()
            paddedPlaintext.write(appData)
            if (padLen > 0) {
                paddedPlaintext.write(getRandomBytes(padLen))
            }
            paddedPlaintext.write(encryptedPaddingLen)

            // kdf
            key1 = ByteArrayInputStream(kdf(dh1, ByteArray(CV_LEN_BYTE), info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE)!!)
            key1.read(cv1)
            key1.read(symmKey1)
            key1.read(nonce1)
            info[info.size - 1] += 0x01.toByte()

            // header = eph_key.pub || ENCRYPT(cc1, sender_key.pub, target_pubkey || eph_key.pub)
            authtext.write(targetPubKey.key)
            authtext.write(ephKey.pub.key)

            header.write(ephKey.pub.key)
            header.write(encrypt(KeyParameter(symmKey1), nonce1, senderKey.pub.key, authtext.toByteArray()))

            // body = noise_body(cc2, appData, target_pubkey || header)
            key2 = ByteArrayInputStream(kdf(dh2, cv1, info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE)!!)
            key2.skip(CV_LEN_BYTE.toLong())    // Not used, so discarded
            key2.read(symmKey2)
            key2.read(nonce2)
            authtext.reset()
            authtext.write(targetPubKey.key)
            header.writeTo(authtext)
            body.write(encrypt(KeyParameter(symmKey2), nonce2, paddedPlaintext.toByteArray(), authtext.toByteArray()))

            // noise box = header || body
            header.writeTo(noiseBox)
            body.writeTo(noiseBox)
        } catch (e: IOException) {
            // Should never occur
            logger.error("Cannot write to ByteArrayOutputStream!", e)
            throw RuntimeException(e)
        } catch (e: InvalidCipherTextException) {
            // Should never occur
            logger.error("Unknown encryption error!", e)
            throw RuntimeException(e)
        }

        return noiseBox.toByteArray()
    }

    /**
     * Gets the plain content from a received noise box.

     * @param targetKey receivers EC key pair
     * *
     * @param noiseBox  ciphertext which is received
     * *
     * @return plaintext which is the content of the received noise box
     * *
     * @throws InvalidKeyException                  if kdf cannot distribute a key from DH of given EC keys
     * *
     * @throws InvalidCipherTextException on decryption errors
     */
    @Throws(InvalidKeyException::class, InvalidCipherTextException::class)
    fun readBox(targetKey: QblECKeyPair, noiseBox: ByteArray): DecryptedPlaintext {
        val key1: ByteArrayInputStream
        val key2: ByteArrayInputStream
        val cipherStream = ByteArrayInputStream(noiseBox)
        val authtext = ByteArrayOutputStream()
        val senderKey: QblECPublicKey
        val encryptedPaddingLength: Int
        val ephRawKey = ByteArray(QblECPublicKey.KEY_SIZE_BYTE)
        val cv1 = ByteArray(CV_LEN_BYTE)
        val symmKey1 = ByteArray(SYMM_KEY_LEN_BYTE)
        val nonce1 = ByteArray(NONCE_LEN_BYTE)
        val symmKey2 = ByteArray(SYMM_KEY_LEN_BYTE)
        val nonce2 = ByteArray(NONCE_LEN_BYTE)
        val headerCipherText = ByteArray(HEADER_CIPHER_TEXT_LEN_BYTE)
        val paddedPlaintext: ByteArray
        val bodyCipherText: ByteArray
        val info = Arrays.copyOf(SUITE_NAME, SUITE_NAME.size + 1)
        try {
            // read ephKey
            if (cipherStream.read(ephRawKey) != ASYM_KEY_SIZE_BYTE) {
                throw InvalidCipherTextException("Invalid ephKey length!")
            }
            val ephKey = QblECPublicKey(ephRawKey)

            // first kdf
            val dh1 = targetKey.ECDH(ephKey)
            key1 = ByteArrayInputStream(kdf(dh1, ByteArray(CV_LEN_BYTE), info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE)!!)
            if (key1.read(cv1) != CV_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid cv1 length!")
            }
            if (key1.read(symmKey1) != SYMM_KEY_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid symmKey length!")
            }
            if (key1.read(nonce1) != NONCE_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid nonce1 length!")
            }

            // sender_key.pub = DECRYPT(cc1, header_cipher_text, target_pubkey || eph_key.pub)
            authtext.write(targetKey.pub.key)
            authtext.write(ephRawKey)
            if (cipherStream.read(headerCipherText) != HEADER_CIPHER_TEXT_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid headerCipherText length!")
            }

            val senderRawKey = decrypt(KeyParameter(symmKey1), nonce1, headerCipherText, authtext.toByteArray())
            senderKey = QblECPublicKey(senderRawKey)

            // second kdf
            val dh2 = targetKey.ECDH(senderKey)
            info[info.size - 1] += 0x01.toByte()
            key2 = ByteArrayInputStream(kdf(dh2, cv1, info, CV_LEN_BYTE + SYMM_KEY_LEN_BYTE + NONCE_LEN_BYTE)!!)
            if (key2.skip(CV_LEN_BYTE.toLong()) != CV_LEN_BYTE.toLong()) {
                throw InvalidCipherTextException("Invalid cv2 length!")
            }
            if (key2.read(symmKey2) != SYMM_KEY_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid symmKey2 length!")
            }
            if (key2.read(nonce2) != NONCE_LEN_BYTE) {
                throw InvalidCipherTextException("Invalid nonce2 length!")
            }

            // plaintext = noise_body^-1(cc2, body, target_pubkey || header)
            authtext.reset()
            authtext.write(targetKey.pub.key)
            authtext.write(ephRawKey)
            authtext.write(headerCipherText)
            bodyCipherText = ByteArray(cipherStream.available())
            cipherStream.read(bodyCipherText)

        } catch (e: IOException) {
            throw InvalidCipherTextException("Invalid ciphertext!")
        }

        paddedPlaintext = decrypt(KeyParameter(symmKey2), nonce2, bodyCipherText, authtext.toByteArray())
        encryptedPaddingLength = ByteBuffer.wrap(
                Arrays.copyOfRange(paddedPlaintext, paddedPlaintext.size - PADDING_LEN_BYTES, paddedPlaintext.size)).int

        // Validate padding length
        if (encryptedPaddingLength < 0 || encryptedPaddingLength > paddedPlaintext.size - PADDING_LEN_BYTES) {
            throw InvalidCipherTextException("Invalid padding length!")
        }
        return DecryptedPlaintext(senderKey,
                Arrays.copyOfRange(paddedPlaintext, 0, paddedPlaintext.size - PADDING_LEN_BYTES - encryptedPaddingLength))
    }


    /**
     * Encrypts a plaintext with associated data with AES GCM

     * @param key            encryption key
     * *
     * @param nonce          nonce for encryption
     * *
     * @param plaintext      plaintext to encrypt
     * *
     * @param associatedData additionally associated data
     * *
     * @return encrypted plaintext
     * *
     * @throws InvalidCipherTextException on encryption errors
     */
    @Throws(InvalidCipherTextException::class)
    fun encrypt(key: KeyParameter, nonce: ByteArray, plaintext: ByteArray, associatedData: ByteArray): ByteArray {
        val params = AEADParameters(key, MAC_BIT, nonce, associatedData)
        val gcm = GCMBlockCipher(AESEngine())
        gcm.init(true, params)

        val output = ByteArray(gcm.getOutputSize(plaintext.size))
        val offOut = gcm.processBytes(plaintext, 0, plaintext.size, output, 0)
        gcm.doFinal(output, offOut)
        return output
    }

    /**
     * Decrypts a ciphertext with associated data with AES GCM

     * @param key            encryption key
     * *
     * @param nonce          nonce for encryption
     * *
     * @param ciphertext     ciphertext to encrypt
     * *
     * @param associatedData additionally associated data
     * *
     * @return encrypted ciphertext
     * *
     * @throws InvalidCipherTextException on decryption errors
     */
    @Throws(InvalidCipherTextException::class)
    fun decrypt(key: KeyParameter, nonce: ByteArray, ciphertext: ByteArray, associatedData: ByteArray): ByteArray {
        val params = AEADParameters(key, MAC_BIT, nonce, associatedData)
        val gcm = GCMBlockCipher(AESEngine())
        gcm.init(false, params)

        val output = ByteArray(gcm.getOutputSize(ciphertext.size))
        val offOut = gcm.processBytes(ciphertext, 0, ciphertext.size, output, 0)
        gcm.doFinal(output, offOut)
        return output
    }

    companion object {
        // https://github.com/Qabel/qabel-doc/wiki/Components-Crypto
        private val SYMM_GCM_READ_SIZE_BYTE = 4096 // Should be multiple of 4096 byte due to flash block size.
        private val SYMM_NONCE_SIZE_BYTE = 12
        private val AES_KEY_SIZE_BYTE = 32
        private val AES_KEY_SIZE_BIT = AES_KEY_SIZE_BYTE * 8

        private val SUITE_NAME = "Noise255/AES256-GCM\0\0\0\0\0".toByteArray()
        private val H_LEN = 64
        private val CV_LEN_BYTE = 48
        private val SYMM_KEY_LEN_BYTE = 32
        private val NONCE_LEN_BYTE = 12
        private val MAC_BIT = 128
        private val HEADER_CIPHER_TEXT_LEN_BYTE = 48
        private val PADDING_LEN_BYTES = 4
        val ASYM_KEY_SIZE_BYTE = 32

        private val logger = LoggerFactory.getLogger(CryptoUtils::class.java.name)
    }
}
/**
 * Encrypts a File to an OutputStream. The OutputStream gets the result
 * immediately while encrypting. The step size of every seperate decryption
 * step is defined in SYMM_ALT_READ_SIZE_BYTE.

 * @param file         Input file that will be encrypted
 * *
 * @param outputStream OutputStream where ciphertext is streamed to
 * *
 * @param key          Key which is used to en-/decrypt
 * *
 * @return true if encryption worked as expected, else false
 * *
 * @throws InvalidKeyException if key is invalid
 */

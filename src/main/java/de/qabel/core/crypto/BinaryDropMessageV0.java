package de.qabel.core.crypto;

import java.security.InvalidKeyException;
import java.util.Arrays;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblVersionMismatchException;

/**
 * Drop message in binary transport format version 0
 */
public class BinaryDropMessageV0 extends AbstractBinaryDropMessage {
	private static final byte VERSION = 0;
	private static final int HEADER_SIZE = 1;
	private static final int PAYLOAD_SIZE = 2048;
	private byte[] encMessage, encKey, signature, signedData;

	private static final Logger logger = LogManager
			.getLogger(BinaryDropMessageV0.class.getName());

	public BinaryDropMessageV0(DropMessage<?> dropMessage)
			throws QblDropPayloadSizeException {
		super(dropMessage);
	}

	public BinaryDropMessageV0(byte[] binaryMessage)
			throws QblVersionMismatchException {
		super(binaryMessage);
		encKey = Arrays.copyOfRange(binaryMessage, HEADER_SIZE, HEADER_SIZE
				+ CryptoUtils.ENCRYPTED_AES_KEY_SIZE_BYTE);
		encMessage = Arrays.copyOfRange(binaryMessage, HEADER_SIZE
				+ CryptoUtils.ENCRYPTED_AES_KEY_SIZE_BYTE, HEADER_SIZE
				+ CryptoUtils.ENCRYPTED_AES_KEY_SIZE_BYTE
				+ CryptoUtils.SYMM_NONCE_SIZE_BYTE + getPayloadSize());
		signature = Arrays.copyOfRange(binaryMessage, getTotalSize()
				- CryptoUtils.RSA_SIGNATURE_SIZE_BYTE, getTotalSize());
		signedData = Arrays.copyOfRange(binaryMessage, HEADER_SIZE, HEADER_SIZE
				+ CryptoUtils.ENCRYPTED_AES_KEY_SIZE_BYTE
				+ CryptoUtils.SYMM_NONCE_SIZE_BYTE + getPayloadSize());
	}

	@Override
	public byte getVersion() {
		return VERSION;
	}

	byte[] getHeader() {
		return new byte[] { VERSION };
	}

	@Override
	int getPayloadSize() {
		return PAYLOAD_SIZE;
	}

	@Override
	int getTotalSize() {
		return PAYLOAD_SIZE + HEADER_SIZE
				+ CryptoUtils.ENCRYPTED_AES_KEY_SIZE_BYTE
				+ CryptoUtils.SYMM_NONCE_SIZE_BYTE
				+ CryptoUtils.RSA_SIGNATURE_SIZE_BYTE;
	}

	private byte[] buildBody(Contact recipient) {
		CryptoUtils cu = new CryptoUtils();
		SecretKey aesKey = cu.generateSymmetricKey();

		try {
			encKey = cu.rsaEncryptForRecipient(aesKey.getEncoded(), recipient
					.getEncryptionPublicKeys().get(0));
			encMessage = cu.encryptSymmetric(getPaddedMessage(), aesKey);
		} catch (InvalidKeyException e) {
			// should not happen
			logger.error("Invalid key", e);
			throw new RuntimeException(e);
		}
		signedData = ArrayUtils.addAll(encKey, encMessage);
		signature = cu.createSignature(signedData, recipient.getContactOwner()
				.getPrimaryKeyPair().getSignKeyPairs().get(0));

		return ArrayUtils.addAll(signedData, signature);
	}

	public byte[] assembleMessageFor(Contact recipient) {
		return ArrayUtils.addAll(getHeader(), buildBody(recipient));
	}

	public byte[] disassembleRawMessageFrom(Contact sender) {
		CryptoUtils cu = new CryptoUtils();
		try {
			if (!cu.validateSignature(signedData, signature, sender
					.getSignPublicKeys().get(0))) {
				logger.debug("Invalid signature.");
				return null;
			}
		} catch (InvalidKeyException e) {
			logger.debug("Invalid signing key");
			return null;
		}

		// Decrypt RSA encrypted AES key and decrypt encrypted data with AES key
		byte[] rawAesKey;
		try {
			rawAesKey = cu.rsaDecrypt(encKey, sender.getContactOwner()
					.getPrimaryKeyPair().getQblEncPrivateKeys().get(0));
		} catch (InvalidKeyException e) {
			logger.debug("Invalid decryption key");
			return null;
		}
		if (rawAesKey == null) {
			// decryption failed
			logger.debug("Message not meant for this sender");
			return null;
		}

		byte[] rawPlainText;
		try {
			rawPlainText = cu.decryptSymmetric(encMessage, new SecretKeySpec(
					rawAesKey, CryptoUtils.SYMM_KEY_ALGORITHM));
		} catch (InvalidKeyException e) {
			logger.debug("Invalid AES key");
			return null;
		}
		return rawPlainText;
	}
}

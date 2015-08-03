package de.qabel.core.crypto;

import java.security.InvalidKeyException;
import java.util.Arrays;

import de.qabel.core.config.Identity;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblVersionMismatchException;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.util.encoders.Hex;

/**
 * Drop message in binary transport format version 0
 */
public class BinaryDropMessageV0 extends AbstractBinaryDropMessage {
	private static final byte VERSION = 0;
	private static final int HEADER_SIZE = 1;
	private static final int BOX_HEADER_SIZE = 100;
	private static final int PAYLOAD_SIZE = 2048;
	private byte[] binaryMessage;

	private static final Logger logger = LoggerFactory
			.getLogger(BinaryDropMessageV0.class.getName());

	public BinaryDropMessageV0(DropMessage<?> dropMessage)
			throws QblDropPayloadSizeException {
		super(dropMessage);
	}

	public BinaryDropMessageV0(byte[] binaryMessage)
			throws QblVersionMismatchException, QblDropInvalidMessageSizeException {
		super(binaryMessage);
		this.binaryMessage = binaryMessage;
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
		return PAYLOAD_SIZE + HEADER_SIZE + BOX_HEADER_SIZE;
	}

	private byte[] buildBody(Contact recipient) {
		CryptoUtils cu = new CryptoUtils();
		byte[] box;
		try {
			box = cu.createBox(recipient.getContactOwner().getPrimaryKeyPair(),
					recipient.getEcPublicKey(), getPaddedMessage(), 0);
		} catch (InvalidKeyException e) {
			// should not happen
			logger.error("Invalid key", e);
			throw new RuntimeException(e);
		}
		return box;
	}

	@Override
	public byte[] assembleMessageFor(Contact recipient) {
		return ArrayUtils.addAll(getHeader(), buildBody(recipient));
	}

	@Override
	public DecryptedPlaintext disassembleRawMessage(Identity identity) {
		CryptoUtils cu = new CryptoUtils();
		DecryptedPlaintext decryptedPlaintext = null;
		try {
			decryptedPlaintext = cu.readBox(identity.getPrimaryKeyPair(),
					Arrays.copyOfRange(binaryMessage, HEADER_SIZE, binaryMessage.length));
		} catch (InvalidKeyException e) {
			logger.debug("Message invalid or not meant for this recipient");
		} catch (InvalidCipherTextException e) {
			logger.debug("Message invalid or not meant for this recipient: " + e.getMessage());
		}
		return decryptedPlaintext;
	}
}

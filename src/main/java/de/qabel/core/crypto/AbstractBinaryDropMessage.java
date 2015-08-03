package de.qabel.core.crypto;

import java.util.Arrays;

import de.qabel.core.config.Identity;
import de.qabel.core.exceptions.QblSpoofedSenderException;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import de.qabel.core.config.Contact;
import de.qabel.core.drop.DropDeserializer;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.DropSerializer;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblVersionMismatchException;

/**
 * Abstract drop message in a binary transport format.
 */
public abstract class AbstractBinaryDropMessage {
	private static final Logger logger = LoggerFactory
			.getLogger(AbstractBinaryDropMessage.class.getName());

	private byte[] plainPayload;

	public AbstractBinaryDropMessage(DropMessage<?> dropMessage)
			throws QblDropPayloadSizeException {
		this.plainPayload = serializeMessage(dropMessage);
		if (plainPayload.length > getPayloadSize()) {
			throw new QblDropPayloadSizeException();
		}
	}

	/**
	 * Creates binary drop message from the raw binary plaintext.
	 *
	 * @param binaryMessage raw binary plaintext.
	 * @throws QblVersionMismatchException if the version header byte is not as expected.
	 * @throws QblDropInvalidMessageSizeException if size does not match the version requirement.
	 */
	public AbstractBinaryDropMessage(byte[] binaryMessage)
			throws QblVersionMismatchException, QblDropInvalidMessageSizeException {
		if (binaryMessage.length != getTotalSize()) {
			logger.debug("Unexpected message size. Is: " + binaryMessage.length
					+ " Should: " + getTotalSize());
			throw new QblDropInvalidMessageSizeException();
		}
		if (binaryMessage[0] != getVersion()) {
			throw new QblVersionMismatchException();
		}
	}

	abstract public byte getVersion();

	abstract int getTotalSize();

	abstract int getPayloadSize();

	private static byte[] serializeMessage(DropMessage<?> dropMessage) {
		Gson gson = new GsonBuilder().registerTypeAdapter(DropMessage.class,
				new DropSerializer()).create();
		return gson.toJson(dropMessage).getBytes();
	}

	/**
	 * Deserializes the message
	 *
	 * @param plainJson plain Json String
	 * @return deserialized Dropmessage or null if deserialization error
	 *         occurred.
	 */
	private static DropMessage<?> deserialize(String plainJson) {
		Gson gson = new GsonBuilder().registerTypeAdapter(DropMessage.class,
				new DropDeserializer()).create();
		try {
			return gson.fromJson(plainJson, DropMessage.class);
		} catch (JsonSyntaxException e) {
			logger.debug("Deserialization failed due to invalid json syntax", e);
			return null;
		}
	}

	byte[] getPaddedMessage() {
		return Arrays.copyOf(plainPayload, getPayloadSize());
	}

	private static byte[] discardPaddingBytes(byte[] paddedMessage) {
		int paddingLen = 0;
		int pos = paddedMessage.length - 1;
		while (pos >= 0 && paddedMessage[pos--] == 0) {
			paddingLen++;
		}
		return Arrays.copyOf(paddedMessage, paddedMessage.length - paddingLen);
	}

	/**
	 * Assembles a binary transport message for the given recipient.
	 * 
	 * @param recipient Recipient of the message.
	 * @return assembled binary message.
	 */
	abstract public byte[] assembleMessageFor(Contact recipient);

	abstract DecryptedPlaintext disassembleRawMessage(Identity identity);

	/**
	 * Disassemble binary transport message assuming it sent by the given
	 * sender.
	 * 
	 * @param identity Identity to decrypt message with.
	 * @return Disassembled drop message or null if either the sender assumption
	 *         was wrong or the message verification failed.
	 */
	public DropMessage<?> disassembleMessage(Identity identity) throws QblSpoofedSenderException {
		DecryptedPlaintext decryptedPlaintext = disassembleRawMessage(identity);
		if (decryptedPlaintext == null){
			return null;
		}

		DropMessage<?> dropMessage = deserialize(new String(
				discardPaddingBytes(decryptedPlaintext.getPlaintext())));
		if (dropMessage == null) {
			logger.debug("Message could not be deserialized. Msg: "
					+ new String(decryptedPlaintext.getPlaintext()));
			return null;
		}

		if (!dropMessage.getSenderKeyId().equals(decryptedPlaintext.getSenderKey().getReadableKeyIdentifier())){
			logger.info("Spoofing of sender information detected."
					+ " Expected: " + dropMessage.getSenderKeyId()
					+ " Actual: " + decryptedPlaintext.getSenderKey().getReadableKeyIdentifier());
			throw new QblSpoofedSenderException();
		}

		return dropMessage;
	}
}

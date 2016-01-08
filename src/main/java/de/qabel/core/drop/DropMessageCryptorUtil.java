package de.qabel.core.drop;

import de.qabel.core.config.Contact;
import de.qabel.core.config.Identity;
import de.qabel.core.crypto.BinaryDropMessageV0;
import de.qabel.core.exceptions.QblDropInvalidMessageSizeException;
import de.qabel.core.exceptions.QblDropPayloadSizeException;
import de.qabel.core.exceptions.QblSpoofedSenderException;
import de.qabel.core.exceptions.QblVersionMismatchException;

/**
 * Utility class to manually en- and decrypt a {@link DropMessage}.
 */
public class DropMessageCryptorUtil {

	/**
	 * Creates an encrypted {@link DropMessage} for a recipient.
	 * @param payload Payload for the encrypted {@link DropMessage}
	 * @param dropMessageType Type of the {@link DropMessage} payload.
	 * @param sender {@link Identity} to use as sender for the {@link DropMessage}.
	 * @param recipient {@link Contact} to encrypt the {@link DropMessage} for.
	 * @return Encrypted {@link DropMessage} for recipient.
	 * @throws QblDropPayloadSizeException
	 */
	public static byte[] createEncryptedDropMessage(String payload, String dropMessageType,
										   Identity sender, Contact recipient) throws QblDropPayloadSizeException {
		DropMessage dropMessage = new DropMessage(sender, payload, dropMessageType);
		BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(dropMessage);
		return binaryMessage.assembleMessageFor(recipient);
	}

	/**
	 * Decrypts an encrypted {@link DropMessage}. Can be used to manually decrypt a {@link DropMessage}
	 * @param identity {@link Identity} to try to decrypt {@link DropMessage} with.
	 * @param encryptedDropMessage encrypted {@link DropMessage}
	 * @return Decrypted {@link DropMessage} if message can be decrypted with
	 * identity or null if message cannot be decrypted with the identity.
	 * @throws QblDropInvalidMessageSizeException
	 * @throws QblVersionMismatchException
	 * @throws QblSpoofedSenderException
	 */
	public static DropMessage decryptDropMessage(Identity identity, byte[] encryptedDropMessage)
			throws QblDropInvalidMessageSizeException, QblVersionMismatchException, QblSpoofedSenderException {
		BinaryDropMessageV0 binaryMessage = new BinaryDropMessageV0(encryptedDropMessage);
		return binaryMessage.disassembleMessage(identity);
	}
}

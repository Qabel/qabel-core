package de.qabel.core.crypto;

/**
 * Abstract super class for all QblSub...KeyPair types 
 * 
 */
abstract class QblSubKeyPair extends QblKeyPair {

	/**
	 * Set the primary key signature for this sub key. A QblPrimaryKeyPair has to
	 * call this method on all its sub keys after creation. 
	 * @param primaryKeySignature
	 */
	abstract void setQblPrimaryKeySignature(byte[] primaryKeySignature);
}

package de.qabel.core.config;

import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import de.qabel.core.crypto.CryptoUtils;

public class QblKeyPair {

	private KeyPair keyPair;
	private String publicKeyFingerprint;
	
	public QblKeyPair() {
		super();
		keyPair = CryptoUtils.getInstance().generateKeyPair();
		genFingerprint();
	}

	public RSAPrivateKey getPrivateKey() {
		return (RSAPrivateKey) keyPair.getPrivate();
	}

	public RSAPublicKey getPublicKey() {
		return (RSAPublicKey) keyPair.getPublic();
	}

	public String getPublicKeyFingerprint() {
		return publicKeyFingerprint;
	}

	private void genFingerprint() {
		StringBuilder sb = new StringBuilder(622);
		sb.append(((RSAPublicKey) keyPair.getPublic()).getPublicExponent());
		sb.append(((RSAPublicKey) keyPair.getPublic()).getModulus());
		publicKeyFingerprint = CryptoUtils.getInstance().getSHA512sum(
				sb.toString());

	}
}

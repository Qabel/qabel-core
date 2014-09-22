package de.qabel.core.config;

import java.security.interfaces.RSAPublicKey;

import de.qabel.core.crypto.CryptoUtils;

public class QblPublicKey {

	private RSAPublicKey publicKey;
	private String publicKeyFingerprint;

	public QblPublicKey(RSAPublicKey publicKey) {
		super();
		genFingerprint();
	}

	public RSAPublicKey getPublicKey() {
		return publicKey;
	}

	public String getPublicKeyFingerprint() {
		return publicKeyFingerprint;
	}

	private void genFingerprint() {
		StringBuilder sb = new StringBuilder(622);
		sb.append(publicKey.getPublicExponent());
		sb.append(publicKey.getModulus());
		publicKeyFingerprint = CryptoUtils.getInstance().getSHA512sum(
				sb.toString());

	}
}

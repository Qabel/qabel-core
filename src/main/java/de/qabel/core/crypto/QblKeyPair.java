package de.qabel.core.crypto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
		ByteArrayOutputStream bs = new ByteArrayOutputStream();
		try {
			bs.write(((RSAPublicKey) keyPair.getPublic()).getPublicExponent()
					.toByteArray());
			bs.write(((RSAPublicKey) keyPair.getPublic()).getModulus()
					.toByteArray());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		publicKeyFingerprint = CryptoUtils.getInstance().getSHA512sum(
				bs.toByteArray());
	}
}

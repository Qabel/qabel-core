package de.qabel.core.crypto;

import java.io.Serializable;

public class Curve25519 implements Serializable {

	static {
		System.loadLibrary("curve25519");
	}

	public Curve25519() {}

	public native byte[] cryptoScalarmult(byte[] n, byte[] p);
	public native byte[] cryptoScalarmultBase(byte[] n);

}

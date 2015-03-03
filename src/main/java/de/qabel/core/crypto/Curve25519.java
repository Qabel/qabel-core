package de.qabel.core.crypto;

public class Curve25519 {

	static {
		System.loadLibrary("curve25519");
	}

	public Curve25519() {}

	public native byte[] cryptoScalarmult(byte[] n, byte[] p);
	public native byte[] cryptoScalarmultBase(byte[] n);

}

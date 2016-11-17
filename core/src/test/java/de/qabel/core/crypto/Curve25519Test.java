package de.qabel.core.crypto;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;

public class Curve25519Test {

    byte[] alicesk = new byte[]{
        (byte) 0x77, (byte) 0x07, (byte) 0x6d, (byte) 0x0a,
        (byte) 0x73, (byte) 0x18, (byte) 0xa5, (byte) 0x7d,
        (byte) 0x3c, (byte) 0x16, (byte) 0xc1, (byte) 0x72,
        (byte) 0x51, (byte) 0xb2, (byte) 0x66, (byte) 0x45,
        (byte) 0xdf, (byte) 0x4c, (byte) 0x2f, (byte) 0x87,
        (byte) 0xeb, (byte) 0xc0, (byte) 0x99, (byte) 0x2a,
        (byte) 0xb1, (byte) 0x77, (byte) 0xfb, (byte) 0xa5,
        (byte) 0x1d, (byte) 0xb9, (byte) 0x2c, (byte) 0x2a
    };
    byte[] expectedpk = new byte[]{
        (byte) 0x85, (byte) 0x20, (byte) 0xf0, (byte) 0x09,
        (byte) 0x89, (byte) 0x30, (byte) 0xa7, (byte) 0x54,
        (byte) 0x74, (byte) 0x8b, (byte) 0x7d, (byte) 0xdc,
        (byte) 0xb4, (byte) 0x3e, (byte) 0xf7, (byte) 0x5a,
        (byte) 0x0d, (byte) 0xbf, (byte) 0x3a, (byte) 0x0d,
        (byte) 0x26, (byte) 0x38, (byte) 0x1a, (byte) 0xf4,
        (byte) 0xeb, (byte) 0xa4, (byte) 0xa9, (byte) 0x8e,
        (byte) 0xaa, (byte) 0x9b, (byte) 0x4e, (byte) 0x6a
    };

    @Test
    public void Curve25519Test() throws InterruptedException {
        byte[] alicepk;
        Curve25519 curve = new Curve25519();

        alicepk = curve.cryptoScalarmultBase(alicesk);
        assertArrayEquals(alicepk, expectedpk);
    }
}

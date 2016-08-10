package de.qabel.core.crypto

import de.qabel.core.crypto.Curve25519
import org.junit.Test

import org.junit.Assert.assertArrayEquals

class Curve25519Test {

    internal var alicesk = byteArrayOf(0x77.toByte(), 0x07.toByte(), 0x6d.toByte(), 0x0a.toByte(), 0x73.toByte(), 0x18.toByte(), 0xa5.toByte(), 0x7d.toByte(), 0x3c.toByte(), 0x16.toByte(), 0xc1.toByte(), 0x72.toByte(), 0x51.toByte(), 0xb2.toByte(), 0x66.toByte(), 0x45.toByte(), 0xdf.toByte(), 0x4c.toByte(), 0x2f.toByte(), 0x87.toByte(), 0xeb.toByte(), 0xc0.toByte(), 0x99.toByte(), 0x2a.toByte(), 0xb1.toByte(), 0x77.toByte(), 0xfb.toByte(), 0xa5.toByte(), 0x1d.toByte(), 0xb9.toByte(), 0x2c.toByte(), 0x2a.toByte())
    internal var expectedpk = byteArrayOf(0x85.toByte(), 0x20.toByte(), 0xf0.toByte(), 0x09.toByte(), 0x89.toByte(), 0x30.toByte(), 0xa7.toByte(), 0x54.toByte(), 0x74.toByte(), 0x8b.toByte(), 0x7d.toByte(), 0xdc.toByte(), 0xb4.toByte(), 0x3e.toByte(), 0xf7.toByte(), 0x5a.toByte(), 0x0d.toByte(), 0xbf.toByte(), 0x3a.toByte(), 0x0d.toByte(), 0x26.toByte(), 0x38.toByte(), 0x1a.toByte(), 0xf4.toByte(), 0xeb.toByte(), 0xa4.toByte(), 0xa9.toByte(), 0x8e.toByte(), 0xaa.toByte(), 0x9b.toByte(), 0x4e.toByte(), 0x6a.toByte())

    @Test
    @Throws(InterruptedException::class)
    fun Curve25519Test() {
        val alicepk: ByteArray
        val curve = Curve25519()

        alicepk = curve.cryptoScalarmultBase(alicesk)
        assertArrayEquals(alicepk, expectedpk)
    }
}

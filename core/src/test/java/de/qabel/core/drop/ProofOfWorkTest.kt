package de.qabel.core.drop

import de.qabel.core.drop.ProofOfWork
import org.junit.Test
import org.spongycastle.crypto.digests.SHA256Digest
import org.spongycastle.util.encoders.Base64
import org.spongycastle.util.encoders.Hex

import org.junit.Assert.assertEquals

class ProofOfWorkTest {
    @Test
    fun proofOfWorkTest() {
        val digest = SHA256Digest()
        val hash = ByteArray(ProofOfWork.hashLength)

        //Create a PoW with 16 leading zero bits
        val pow: ProofOfWork
        pow = ProofOfWork.calculate(16, Hex.decode("257157de4b0551"), Hex.decode("abcdef"))

        digest.update(Base64.decode(pow.iVserverB64), 0, Base64.decode(pow.iVserverB64).size)
        digest.update(Base64.decode(pow.iVclientB64), 0, Base64.decode(pow.iVclientB64).size)
        digest.update(ProofOfWork.toByteArray(pow.time), 0, ProofOfWork.longLength)
        digest.update(Base64.decode(pow.messageHashB64), 0, Base64.decode(pow.messageHashB64).size)
        digest.update(ProofOfWork.toByteArray(pow.counter), 0, ProofOfWork.longLength)

        digest.doFinal(hash, 0)

        assertEquals(Hex.toHexString(Base64.decode(pow.proofOfWorkHashB64)), Hex.toHexString(hash))
        assertEquals(Base64.decode(pow.proofOfWorkHashB64)[0].toLong(), 0)
        assertEquals(Base64.decode(pow.proofOfWorkHashB64)[1].toLong(), 0)
    }
}

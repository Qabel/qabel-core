package de.qabel.core.drop;

import org.junit.Test;
import org.spongycastle.crypto.digests.SHA256Digest;
import org.spongycastle.util.encoders.Hex;

import static org.junit.Assert.assertEquals;

public class ProofOfWorkTest {

	@Test
	public void proofOfWorkTest() {
		SHA256Digest digest = new SHA256Digest();
		byte[] hash = new byte[256/8];

		//Create a PoW with 16 leading zero bits
		ProofOfWork pow = new ProofOfWork();
		byte[][] result = pow.calculate(16, Hex.decode("257157de4b0551"), Hex.decode("abcdef"));

		digest.update(result[0], 0, result[0].length);
		digest.doFinal(hash, 0);

		assertEquals(Hex.toHexString(result[1]), Hex.toHexString(hash));
		assertEquals(result[1][0],0);
		assertEquals(result[1][1],0);
	}
}

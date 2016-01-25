package de.qabel.core.drop;

import de.qabel.core.crypto.CryptoUtils;
import org.spongycastle.crypto.digests.SHA256Digest;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

public class ProofOfWork {
	private SHA256Digest digest;
	private Calendar calendar;
	private CryptoUtils cUtils;

	private int X;
	private byte[] IVserver;
	private byte[] IVclient;
	private long time;
	private byte[] timeBytes;
	private byte[] messageHash;
	private long counter;

	private byte[] pow;
	/**
	 * Initializes PoW
	 */
	public ProofOfWork() {
		calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		cUtils = new CryptoUtils();
		digest = new SHA256Digest();

		// byte array for hash result
		pow = new byte[256/8];
	}

	/**
	 * Calculates the PoW for given parameters
	 * @param X Number of leading zero bits of PoW hash
	 * @param IVserver Server IV which is part of the PoW
	 * @param messageHash hash of message to be sent
	 * @return byte[][]: byte[0]=plain parameters byte[1]=PoW hash
	 */
	public byte[][] calculate(int X, byte[] IVserver, byte[] messageHash) {
		this.X = X;
		this.IVserver = IVserver;
		this.messageHash = messageHash;

		time = calendar.getTimeInMillis() / 1000L;
		timeBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(time).array();
		IVclient = cUtils.getRandomBytes(16);

		byte[] fix = new byte[IVserver.length + IVclient.length + timeBytes.length + messageHash.length];
		System.arraycopy(IVserver, 0, fix, 0, IVserver.length);
		System.arraycopy(IVclient, 0, fix, IVserver.length, IVclient.length);
		System.arraycopy(timeBytes, 0, fix, IVserver.length+IVclient.length, timeBytes.length);
		System.arraycopy(messageHash, 0, fix, IVserver.length+IVclient.length+timeBytes.length, messageHash.length);

		//Find counter which fulfills pattern
		long i = 0;
		updatePow(fix, i);
		while(!enoughZeros(pow)) {
			i++;
			updatePow(fix, i);
		}
		counter = i;

		//Byte array which contains plain pow text and pow hash
		byte[][] result = new byte[2][Math.max(fix.length+Long.SIZE/Byte.SIZE,256/8)];
		System.arraycopy(fix, 0, result[0], 0, fix.length);
		System.arraycopy(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(counter).array(),
				0, result[0], fix.length, Long.SIZE/Byte.SIZE);
		result[1] = pow;

		return result;
	}

	private boolean enoughZeros(byte[] hash) {
		for(int i=0; i<X; i++) {
			//check whether i-th bit is zero
			if(((hash[i/8]>>(i%8))&1) != 0) {
				return false;
			}
		}
		return true;
	}

	private void updatePow(byte[] fix, long i) {
		digest.reset();
		digest.update(fix, 0, fix.length);
		digest.update(ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(i).array(), 0, Long.SIZE / Byte.SIZE);
		digest.doFinal(pow,0);
	}
}

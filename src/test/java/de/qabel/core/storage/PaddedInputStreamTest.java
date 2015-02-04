package de.qabel.core.storage;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class PaddedInputStreamTest {
	@Rule
	public ExpectedException exception = ExpectedException.none();

	private class DummyInput extends InputStream {
		private long bytesToServe;

		DummyInput(long size) {
			this.bytesToServe = size;
		}

		@Override
		public int read() {
			if (bytesToServe > 0) {
				bytesToServe--;
				return 42;
			} else {
				return -1;
			}
		}
	}

	@Test
	public void exceedLimit() throws IOException {
		exception.expect(IOException.class);
		final int size = 23;
		InputStream is = new DummyInput(size + 5); // input of one more byte than allowed
		PaddedInputStream pis = new PaddedInputStream(is, size);
		long total = countBytes(pis); // should throw IOException
		Assert.assertEquals(0, total); // should not be reached
	}

	@Test
	public void exhaustLimit() throws IOException {
		final int size = 23;
		InputStream is = new DummyInput(size); // input size of exact limit
		PaddedInputStream pis = new PaddedInputStream(is, size);
		Assert.assertEquals(size, countBytes(pis));
	}

	@Test
	public void testPaddingToMinimum() throws IOException {
		byte[] input = { 42 };
		byte[] padded = { 42, 0, 0, 0 };
		PaddedInputStream pis = new PaddedInputStream(new ByteArrayInputStream(input), padded.length, padded.length * 2);
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		IOUtils.copy(pis, bos);
		Assert.assertArrayEquals(padded, bos.toByteArray());
		Assert.assertEquals(input.length, pis.getUnPaddedSize());
	}

	private static long countBytes(InputStream stream) throws IOException {
		byte[] buffer = new byte[4096];
		long total = 0, readBytes = 0;
		while ((readBytes = stream.read(buffer)) >= 0) {
			total += readBytes;
		}
		return total;
	}

	@Test
	public void validPaddingRange1() throws IOException {
		InputStream is = new DummyInput(3);
		// use a proper padding range
		PaddedInputStream pis = new PaddedInputStream(is, 4, 8);
		pis.close();
	}

	@Test
	public void invalidPaddingRange1() throws IOException {
		exception.expect(IllegalArgumentException.class);
		InputStream is = new DummyInput(3);
		// use a lower bound which is greater
		PaddedInputStream pis = new PaddedInputStream(is, 8, 4);
		pis.close();
	}

	@Test
	public void invalidPaddingRange2() throws IOException {
		exception.expect(IllegalArgumentException.class);
		InputStream is = new DummyInput(3);
		// use a use a non-power of two padding bound
		PaddedInputStream pis = new PaddedInputStream(is, 5, 8);
		pis.close();
	}
}

package de.qabel.core.storage;

import java.io.IOException;
import java.io.InputStream;

public class PaddedInputStream extends InputStream {
	private static final byte FILL_BYTE = 0;
	private InputStream stream;
	private long totalReadBytes, paddingToWrite;
	private long paddedSize, lowerBoundary, upperBoundary;
	private boolean streamExhausted;

	/**
	 * Creates an InputStream that pads the given InputStream to the given size. The stream is padded with the zero
	 * byte.
	 * 
	 * @param stream Input stream to pad.
	 * @param paddedSize Size of the padded stream in bytes.
	 */
	public PaddedInputStream(InputStream stream, long paddedSize) {
		this(stream, paddedSize, paddedSize);
		this.paddedSize = paddedSize;
	}

	/**
	 * Creates an InputStream that pads the given InputStream to the smallest power-of-two size within the given range.
	 * 
	 * @param stream InputStream to pad.
	 * @param lowerBound Lower bound power of two of the padding range in bytes.
	 * @param upperBound Upper bound power of two of the padding range in bytes.
	 */
	public PaddedInputStream(InputStream stream, long lowerBound, long upperBound) {
		if (lowerBound > upperBound) {
			throw new IllegalArgumentException("Lower bound must be less than or equal to upper.");
		}
		if (lowerBound != upperBound) {
			if (!isNumberOfTwo(lowerBound)) {
				throw new IllegalArgumentException("Given lower bound " + lowerBound + " is not a power of two");
			}
			if (!isNumberOfTwo(upperBound)) {
				throw new IllegalArgumentException("Given upper bound " + upperBound + " is not a power of two");
			}
		}
		this.stream = stream;
		this.lowerBoundary = lowerBound;
		this.upperBoundary = upperBound;
		this.totalReadBytes = 0;
		this.paddedSize = 0;
		this.streamExhausted = false;
	}

	private static boolean isNumberOfTwo(long number) {
		return ((number & -number) == number);
	}

	/**
	 * Return the number of bytes the unpadded InputStream provided. Until the unpadded input stream has been exhausted,
	 * this method returns zero.
	 * 
	 * @return size of unpadded InputStream in bytes.
	 */
	public long getUnPaddedSize() {
		long res = 0;
		if (this.streamExhausted) {
			res = this.totalReadBytes;
		}
		return res;
	}

	@Override
	public int read() throws IOException {
		if (!this.streamExhausted) {
			int result = this.stream.read();

			if (result != -1) {
				this.totalReadBytes++;
				if (this.totalReadBytes > this.upperBoundary) {
					throw new IOException("Maximum size exceeded.");
				}
				return result;
			} else {
				this.streamExhausted = true;
				this.setPaddedSize();
				this.paddingToWrite = this.paddedSize - this.totalReadBytes;
			}
		}

		if (this.paddingToWrite > 0) {
			this.paddingToWrite--;
			return FILL_BYTE;
		}

		// signal end of stream
		return -1;
	}

	private void setPaddedSize() throws IOException {
		if (this.paddedSize == 0) {
			long paddedSize = this.lowerBoundary;

			while (this.totalReadBytes > paddedSize && paddedSize <= this.upperBoundary) {
				paddedSize *= 2;
			}

			if (paddedSize > this.upperBoundary) {
				throw new IOException("Maximum size exceeded.");
			}

			this.paddedSize = paddedSize;
		}
	}
}

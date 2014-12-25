package de.qabel.core.storage;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import org.bouncycastle.util.Arrays;
import org.bouncycastle.util.encoders.DecoderException;
import org.bouncycastle.util.encoders.UrlBase64;

import de.qabel.core.exceptions.QblStorageBlobSizeExeeded;
import de.qabel.core.exceptions.QblStorageInvalidBlobName;

public class StorageBlob {
	public static final int MINIMUM_SIZE_BYTES = 1 * 1024 * 1024; // 1 MiByte
	public static final int MAXIMUM_SIZE_BYTES = 2 * 1024 * 1024; // 2 MiByte
	private static final byte FILL_BYTE = 0;
	private String name;
	private InputStream stream;
	
	/**
	 * Generates a StorageBlob from an InputStream.
	 * @param bolbStream InputStream containing the blob.
	 * @param name Name of the blob, if null then a random name will be set.
	 * @throws IOException
	 * @throws QblStorageBlobSizeExeeded if the blob exceeds the maximum size.
	 * @throws QblStorageInvalidBlobName if name contains non-Base64url characters.
	 */
	public StorageBlob(InputStream bolbStream, String name)
			throws IOException, QblStorageBlobSizeExeeded, QblStorageInvalidBlobName {
		File temp = File.createTempFile("temp", ".bin");
		temp.deleteOnExit(); // TODO is this safe?
		FileOutputStream fos = new FileOutputStream(temp);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		byte[] copyBuffer = new byte[512];
		
		long totalReadBytes = 0;
		int readBytes;
		while ((readBytes = bolbStream.read(copyBuffer)) > 0) {
			bos.write(copyBuffer, 0, readBytes);
			totalReadBytes += readBytes;
		}
		
		long paddedSize = this.calcPaddedSize(totalReadBytes);
		this.writePadding(bos, paddedSize - totalReadBytes);
		bos.close();
		
		this.stream = new FileInputStream(temp);
		this.setName(name);
	}

	/**
	 * Generates a StorageBlob from an InputStream.
	 * @param bolb The blob.
	 * @param name Name of the blob, if null then a random name will be set.
	 * @throws IOException
	 * @throws QblStorageBlobSizeExeeded if the blob exceeds the maximum size.
	 * @throws QblStorageInvalidBlobName if name contains non-Base64url characters.
	 */
	public StorageBlob(byte[] blob, String name)
			throws QblStorageBlobSizeExeeded, IOException, QblStorageInvalidBlobName {
		int size = blob.length;
		int paddedSize = this.calcPaddedSize(size);
		ByteArrayOutputStream os = new ByteArrayOutputStream(paddedSize);
		os.write(blob);
		
		int paddingToWrite = paddedSize - size;
		this.writePadding(os, paddingToWrite);
		os.close();
		this.stream = new ByteArrayInputStream(os.toByteArray());
		this.setName(name);
	}
	
	public String getName() {
		return name;
	}

	/**
	 * Set blob name.
	 * @param name Blob name, if null then a random name is generated.
	 * @throws QblStorageInvalidBlobName if name contains characters not allowed by Base64url.
	 */
	public void setName(String name) throws QblStorageInvalidBlobName {
		if (name == null) {
			// generate random name
			name = UUID.randomUUID().toString();
		}
		checkBlobName(name);
		this.name = name;
	}
	
	static void checkBlobName(String name) throws QblStorageInvalidBlobName {
		try {
			UrlBase64.decode(name + "."); // add terminating dot
		} catch (DecoderException e) {
			throw new QblStorageInvalidBlobName();
		}
	}

	public InputStream getInputStream() throws IOException {
		return stream;
	}

	private int calcPaddedSize(int curSize) throws QblStorageBlobSizeExeeded {
		return (int) this.calcPaddedSize((long)curSize);
	}
	
	private long calcPaddedSize(long curSize) throws QblStorageBlobSizeExeeded {
		long paddedSize = MINIMUM_SIZE_BYTES;
		
		while (curSize > paddedSize && paddedSize <= MAXIMUM_SIZE_BYTES) {
			paddedSize *= 2;
		}
		
		if (paddedSize > MAXIMUM_SIZE_BYTES) {
			throw new QblStorageBlobSizeExeeded();
		}
		
		return paddedSize;
	}
	
	private void writePadding(OutputStream stream, long paddingSize) throws IOException {
		long paddingToWrite = paddingSize;
		byte[] paddingBuffer = new byte[512];
		Arrays.fill(paddingBuffer, FILL_BYTE); 
		while (paddingToWrite > 0) {
			int curChunkLen = paddingToWrite > paddingBuffer.length ? paddingBuffer.length : (int)paddingToWrite;
			stream.write(paddingBuffer, 0, curChunkLen);
			paddingToWrite -= curChunkLen;
		}
	}
}

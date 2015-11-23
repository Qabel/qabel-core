package de.qabel.core.storage;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import org.spongycastle.util.encoders.DecoderException;
import org.spongycastle.util.encoders.UrlBase64;

import de.qabel.core.exceptions.QblStorageInvalidBlobName;

public class StorageBlob {
	public static final int MINIMUM_SIZE_BYTES = 1 * 1024 * 1024; // 1 MiByte
	public static final int MAXIMUM_SIZE_BYTES = 2 * 1024 * 1024; // 2 MiByte
	private String name;
	private PaddedInputStream paddedStream;

	/**
	 * Generates a StorageBlob from an InputStream.
	 * 
	 * @param bolbStream InputStream containing the blob.
	 * @param name Name of the blob, if null then a random name will be set.
	 * @throws IOException if maximum blob size was exceeded while reading.
	 * @throws QblStorageInvalidBlobName if name contains non-Base64url characters.
	 */
	public StorageBlob(InputStream bolbStream, String name) throws IOException, QblStorageInvalidBlobName {
		this.paddedStream = new PaddedInputStream(bolbStream, MINIMUM_SIZE_BYTES, MAXIMUM_SIZE_BYTES);
		this.setName(name);
	}

	/**
	 * Generates a StorageBlob from an InputStream.
	 * 
	 * @param blob The blob.
	 * @param name Name of the blob, if null then a random name will be set.
	 * @throws IOException if maximum blob size was exceeded while reading.
	 * @throws QblStorageInvalidBlobName if name contains non-Base64url characters.
	 */
	public StorageBlob(byte[] blob, String name) throws IOException, QblStorageInvalidBlobName {
		this(new ByteArrayInputStream(blob), name);
	}

	public String getName() {
		return name;
	}

	/**
	 * Set blob name.
	 * 
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
		return paddedStream;
	}
}

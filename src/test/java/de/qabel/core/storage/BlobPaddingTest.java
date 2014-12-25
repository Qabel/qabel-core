package de.qabel.core.storage;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import de.qabel.core.exceptions.QblStorageBlobSizeExeeded;
import de.qabel.core.exceptions.QblStorageInvalidBlobName;

public class BlobPaddingTest {

    @Rule public ExpectedException exception = ExpectedException.none();

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
	public void exceedBlobSizeLimit()
			throws QblStorageBlobSizeExeeded, IOException, QblStorageInvalidBlobName {
    	exception.expect(QblStorageBlobSizeExeeded.class);
    	InputStream is = new DummyInput(StorageBlob.MAXIMUM_SIZE_BYTES + 1); // input of one more byte than allowed
		new StorageBlob(is, null);
	}
	
	@Test
	public void exhaustBlobSizeLimit()
			throws QblStorageBlobSizeExeeded, IOException, QblStorageInvalidBlobName {
    	InputStream is = new DummyInput(StorageBlob.MAXIMUM_SIZE_BYTES); // input size of exact limit
		new StorageBlob(is, null);
	}
	
	@Test
	public void testPaddingToMinimum()
			throws QblStorageBlobSizeExeeded, IOException, QblStorageInvalidBlobName {
		byte[] input = new byte[1];
		Arrays.fill(input, (byte)42);
		StorageBlob blob = new StorageBlob(input, null);
		InputStream stream = blob.getInputStream();
		long paddedSize = 0;
		long readBytes;
		byte[] buffer = new byte[1024];
		while ((readBytes = stream.read(buffer)) >= 0) {
			paddedSize += readBytes;
		}
		Assert.assertEquals(paddedSize, StorageBlob.MINIMUM_SIZE_BYTES);
	}
}

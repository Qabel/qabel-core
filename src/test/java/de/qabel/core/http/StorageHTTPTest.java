package de.qabel.core.http;

import de.qabel.core.config.StorageVolume;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class StorageHTTPTest {
	private URL url;
	private StorageVolume storageVolume;
	private String publicIdentifier, token;

	@Before
	public void setUp() throws IOException {
		try {
			url = new URL("http://localhost:8000/data");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		StorageHTTP storageHTTP = new StorageHTTP();
		HTTPResult result = storageHTTP.createNewStorageVolume(url);
		Assume.assumeTrue(result.isOk());
		storageVolume = (StorageVolume) result.getData();

		publicIdentifier = storageVolume.getPublicIdentifier();
		token = storageVolume.getToken();
	}

	@Test
	public void createNewStorageVolumeTest() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.createNewStorageVolume(url);
		//Then
		StorageVolume storageVolume = (StorageVolume) result.getData();
		assertNotNull(storageVolume);
		assertTrue(result.isOk());
		assertEquals(201, result.getResponseCode());
		assertFalse(storageVolume.getPublicIdentifier().equals(""));
		assertFalse(storageVolume.getRevokeToken().equals(""));
		assertFalse(storageVolume.getToken().equals(""));
	}

	@Test
	public void probeExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.probeStorageVolume(url, publicIdentifier);
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	@Ignore //Doesn't work, get 500 and Error: ENOENT, no such file or directory
	public void probeNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.probeStorageVolume(url, "foo" + publicIdentifier);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	@Ignore //Does work, because "GET /data/ 200" is ok with the current qabel-storage
	public void probeMissingPubIdQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.probeStorageVolume(url, "");
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}
}

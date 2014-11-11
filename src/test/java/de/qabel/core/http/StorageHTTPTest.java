package de.qabel.core.http;

import de.qabel.core.config.StorageVolume;
import org.junit.Before;
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
	@Before
	public void setUp() throws IOException {
		try {
			url = new URL("http://localhost:8000/data");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
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
}

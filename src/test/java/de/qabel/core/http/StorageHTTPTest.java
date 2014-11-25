package de.qabel.core.http;

import de.qabel.core.config.StorageVolume;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

public class StorageHTTPTest {
	private URL url;
	private StorageVolume storageVolume, delStorageVolume;
	private String publicIdentifier, token, delPublicIdentifier, delToken, delRevokeToken;
	private byte[] blob;

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

		char[] text = new char[42];
		Arrays.fill(text, 'a');
		blob = new String(text).getBytes();

		storageHTTP.upload(url, publicIdentifier, "retrieveTest", token, blob);

		HTTPResult resultDelete = storageHTTP.createNewStorageVolume(url);
		Assume.assumeTrue(resultDelete.isOk());
		delStorageVolume = (StorageVolume) result.getData();
		delPublicIdentifier = delStorageVolume.getPublicIdentifier();
		delToken = delStorageVolume.getToken();
		delRevokeToken = delStorageVolume.getRevokeToken();
		storageHTTP.upload(url, delPublicIdentifier, "deleteBlobTest", delToken, blob);
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

	@Test
	public void uploadToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.upload(url, publicIdentifier, "foo", token, blob);
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	@Ignore //400 http status does not exist in qabel-storage (also not in the protocol_update branch)
	public void uploadWithMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.upload(url, null, "foo", token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void uploadWithMissingTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.upload(url, publicIdentifier, "foo", null, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void uploadWithInvalidTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.upload(url, publicIdentifier, "foo", "foo" + token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	public void uploadToNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.upload(url, "foo" + publicIdentifier, "foo", token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.retrieveBlob(url, publicIdentifier, "retrieveTest");
		//Then
		assertEquals(200, result.getResponseCode());
	}

	@Test
	@Ignore //Get HTTP response Code: 500
	public void retrieveBlobFromMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.retrieveBlob(url, null, "retrieveTest");
		//Then
		assertEquals(400, result.getResponseCode());
	}

	@Test
	@Ignore //Get HTTP response Code: 500
	public void retrieveBlobFromInvalidQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.retrieveBlob(url, "foo" + publicIdentifier, "retrieveTest");
		//Then
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteBlobFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	@Test
	@Ignore //400 Doesn't exists in the qabel-storage delete
	public void deleteWithMissingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, null, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void deleteWithMissingRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", null);
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void deleteWithInvalidRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", delRevokeToken + "Foo");
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	@Ignore //404 Doesn't exists in the qabel-storage delete
	public void deleteWithNotExisitingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, "foo" + delPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult result = storageHTTP.delete(url, delPublicIdentifier, "", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}
}

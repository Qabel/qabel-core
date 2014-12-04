package de.qabel.core.http;

import de.qabel.core.config.StorageVolume;
import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class StorageHTTPTest {
	private URL url;
	private StorageVolume storageVolume, delStorageVolume;
	private String publicIdentifier, token, revokeToken, delPublicIdentifier, delToken, delRevokeToken;
	private byte[] blob;

	@Before
	public void setUp() throws IOException {
		try {
			url = new URL("http://localhost:8000/data");
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		StorageHTTP storageHTTP = new StorageHTTP();
		HTTPResult<StorageVolume> result = storageHTTP.createNewStorageVolume(url);
		Assume.assumeTrue(result.isOk());
		storageVolume = result.getData();

		publicIdentifier = storageVolume.getPublicIdentifier();
		token = storageVolume.getToken();
		revokeToken = storageVolume.getRevokeToken();

		char[] text = new char[42];
		Arrays.fill(text, 'a');
		blob = new String(text).getBytes();

		storageHTTP.upload(url, publicIdentifier, "retrieveTest", token, blob);

		HTTPResult<StorageVolume> resultDelete = storageHTTP.createNewStorageVolume(url);
		Assume.assumeTrue(resultDelete.isOk());
		delStorageVolume = resultDelete.getData();
		delPublicIdentifier = delStorageVolume.getPublicIdentifier();
		delToken = delStorageVolume.getToken();
		delRevokeToken = delStorageVolume.getRevokeToken();
		storageHTTP.upload(url, delPublicIdentifier, "deleteBlobTest", delToken, blob);
	}

	@After
	public void tearDown() throws IOException{
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		storageHTTP.delete(url, publicIdentifier, "", revokeToken);
		storageHTTP.delete(url, delPublicIdentifier, "", delRevokeToken);
	}

	@Test
	public void createNewStorageVolumeTest() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<StorageVolume> result = storageHTTP.createNewStorageVolume(url);
		//Then
		StorageVolume storageVolume = result.getData();
		assertNotNull(storageVolume);
		assertTrue(result.isOk());
		assertEquals(201, result.getResponseCode());
		assertFalse(storageVolume.getPublicIdentifier().equals(""));
		assertFalse(storageVolume.getRevokeToken().equals(""));
		assertFalse(storageVolume.getToken().equals(""));
		storageHTTP.delete(url, storageVolume.getPublicIdentifier(), "", storageVolume.getRevokeToken());
	}

	@Test
	public void probeExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume(url, publicIdentifier);
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	public void probeNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume(url, "foo" + publicIdentifier);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void probeMissingPubIdQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume(url, "");
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void uploadToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.upload(url, publicIdentifier, "foo", token, blob);
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	public void uploadWithMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.upload(url, null, "foo", token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void uploadWithMissingTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.upload(url, publicIdentifier, "foo", null, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void uploadWithInvalidTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.upload(url, publicIdentifier, "foo", "foo" + token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	public void uploadToNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.upload(url, "foo" + publicIdentifier, "foo", token, blob);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(url, publicIdentifier, "retrieveTest");
		//Then
		assertEquals(new String(blob), IOUtils.toString(result.getData(), "UTF-8"));
		assertEquals(200, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(url, null, "retrieveTest");
		//Then
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromInvalidQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(url, "foo" + publicIdentifier, "retrieveTest");
		//Then
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteBlobFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	@Test
	public void deleteWithMissingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, null, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void deleteWithMissingRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", null);
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void deleteWithInvalidRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, delPublicIdentifier, "deleteBlobTest", delRevokeToken + "Foo");
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	public void deleteWithNotExisitingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, "foo" + delPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP();
		//When
		HTTPResult<?> result = storageHTTP.delete(url, delPublicIdentifier, "", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}
}

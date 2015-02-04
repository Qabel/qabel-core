package de.qabel.core.http;

import de.qabel.core.config.StorageServer;
import de.qabel.core.config.StorageVolume;

import org.apache.commons.io.IOUtils;
import org.junit.Assume;
import org.junit.Before;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

public class StorageHTTPTest {
	private StorageServer server;
	private StorageVolume storageVolume, delStorageVolume;
	private String publicIdentifier, token, revokeToken, delPublicIdentifier, delToken, delRevokeToken;
	private final String unknownPublicIdentifier = UUID.randomUUID().toString();
	private byte[] blob;

	@Before
	public void setUp() throws IOException {
		this.server = new StorageServer(new URL("http://localhost:8000/data"), "");
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		HTTPResult<StorageVolume> result = storageHTTP.createNewStorageVolume();
		Assume.assumeTrue(result.isOk());
		storageVolume = result.getData();

		publicIdentifier = storageVolume.getPublicIdentifier();
		token = storageVolume.getToken();
		revokeToken = storageVolume.getRevokeToken();

		char[] text = new char[42];
		Arrays.fill(text, 'a');
		blob = new String(text).getBytes();

		OutputStream out = storageHTTP.prepareUpload(publicIdentifier, "retrieveTest", token);
		out.write(blob);
		storageHTTP.finishUpload();

		HTTPResult<StorageVolume> resultDelete = storageHTTP.createNewStorageVolume();
		Assume.assumeTrue(resultDelete.isOk());
		delStorageVolume = resultDelete.getData();
		delPublicIdentifier = delStorageVolume.getPublicIdentifier();
		delToken = delStorageVolume.getToken();
		delRevokeToken = delStorageVolume.getRevokeToken();
		out = storageHTTP.prepareUpload(delPublicIdentifier, "deleteBlobTest", delToken);
		out.write(blob);
		storageHTTP.finishUpload();

		// under very unlikely conditions the unknown QSV id can be the same, by chance
		// then the tests for the invalid QSV id would not work
		assertNotEquals(publicIdentifier, unknownPublicIdentifier);
		assertNotEquals(delPublicIdentifier, unknownPublicIdentifier);
	}

	@After
	public void tearDown() throws IOException{
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		storageHTTP.delete(publicIdentifier, "", revokeToken);
		storageHTTP.delete(delPublicIdentifier, "", delRevokeToken);
	}

	@Test
	public void createNewStorageVolumeTest() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<StorageVolume> result = storageHTTP.createNewStorageVolume();
		//Then
		StorageVolume storageVolume = result.getData();
		assertNotNull(storageVolume);
		assertTrue(result.isOk());
		assertEquals(201, result.getResponseCode());
		assertFalse(storageVolume.getPublicIdentifier().equals(""));
		assertFalse(storageVolume.getRevokeToken().equals(""));
		assertFalse(storageVolume.getToken().equals(""));
		storageHTTP.delete(storageVolume.getPublicIdentifier(), "", storageVolume.getRevokeToken());
	}

	@Test
	public void probeExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume(publicIdentifier);
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	public void probeNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume(unknownPublicIdentifier);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void probeMissingPubIdQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume("");
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void uploadToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		
		OutputStream out = storageHTTP.prepareUpload(publicIdentifier, "foo", token);
		out.write(blob);
		HTTPResult<?> result = storageHTTP.finishUpload(); 
		//Then
		assertTrue(result.isOk());
		assertEquals(200, result.getResponseCode());
	}

	@Test
	@Ignore // see deleteWithMissingVolumeIdFromStorage
	public void uploadWithMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		OutputStream out = storageHTTP.prepareUpload(null, "foo", token);
		out.write(blob);
		HTTPResult<?> result = storageHTTP.finishUpload();
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	@Ignore // see deleteWithMissingRevokeTokenFromStorage
	public void uploadWithMissingTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		OutputStream out = storageHTTP.prepareUpload(publicIdentifier, "foo", "");
		out.write(blob);
		HTTPResult<?> result = storageHTTP.finishUpload();
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void uploadWithInvalidTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		OutputStream out = storageHTTP.prepareUpload(publicIdentifier, "foo", "foo" + token);
		out.write(blob);
		HTTPResult<?> result = storageHTTP.finishUpload();
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	public void uploadToNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		OutputStream out = storageHTTP.prepareUpload(unknownPublicIdentifier, "foo", token);
		out.write(blob);
		HTTPResult<?> result = storageHTTP.finishUpload();
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(publicIdentifier, "retrieveTest");
		//Then
		assertEquals(new String(blob), IOUtils.toString(result.getData(), "UTF-8"));
		assertEquals(200, result.getResponseCode());
	}

	@Test
	@Ignore // deleteWithMissingVolumeIdFromStorage
	public void retrieveBlobFromMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(null, "retrieveTest");
		//Then
		assertEquals(400, result.getResponseCode());
	}

	@Test
	public void retrieveBlobFromInvalidQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(unknownPublicIdentifier, "retrieveTest");
		//Then
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteBlobFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(delPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}

	@Test
	@Ignore
	// Passing null as a missing volume id is problematic because even if null results
	// in an empty string part in the request path (e.g. /data//blobName)
	// many http servers whould automatically detect the non-normalized path
	// and return a redirect (e.g. /data/blobName).
	// As a result, blobName would be treated as a volume identifier instead
	// of rejecting the request as invalid (400).
	public void deleteWithMissingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(null, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(400, result.getResponseCode());
	}

	@Test
	@Ignore // The client API does not allow to omit a token. This test should be on the server.
	public void deleteWithMissingRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(delPublicIdentifier, "deleteBlobTest", "");
		//Then
		assertFalse(result.isOk());
		assertEquals(401, result.getResponseCode());
	}

	@Test
	public void deleteWithInvalidRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(delPublicIdentifier, "deleteBlobTest", delRevokeToken + "Foo");
		//Then
		assertFalse(result.isOk());
		assertEquals(403, result.getResponseCode());
	}

	@Test
	public void deleteWithNotExisitingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(unknownPublicIdentifier, "deleteBlobTest", delRevokeToken);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	public void deleteStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(delPublicIdentifier, "", delRevokeToken);
		//Then
		assertTrue(result.isOk());
		assertEquals(204, result.getResponseCode());
	}
}

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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;

public class StorageHTTPTest {
	private StorageServer server;
	private StorageVolume storageVolume, delStorageVolume;
	private String publicIdentifier, token, revokeToken, delPublicIdentifier, delToken, delRevokeToken;
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
	@Ignore //Doesn't work, get 500 and Error: ENOENT, no such file or directory
	public void probeNotExistingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.probeStorageVolume("foo" + publicIdentifier);
		//Then
		assertFalse(result.isOk());
		assertEquals(404, result.getResponseCode());
	}

	@Test
	@Ignore //Does work, because "GET /data/ 200" is ok with the current qabel-storage
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
	@Ignore //400 http status does not exist in qabel-storage (also not in the protocol_update branch)
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
	public void uploadWithMissingTokenToQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		OutputStream out = storageHTTP.prepareUpload(publicIdentifier, "foo", null);
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
		OutputStream out = storageHTTP.prepareUpload("foo" + publicIdentifier, "foo", token);
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
	@Ignore //Get HTTP response Code: 500
	public void retrieveBlobFromMissingQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob(null, "retrieveTest");
		//Then
		assertEquals(400, result.getResponseCode());
	}

	@Test
	@Ignore //Get HTTP response Code: 500
	public void retrieveBlobFromInvalidQabelStorageVolume() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<InputStream> result = storageHTTP.retrieveBlob("foo" + publicIdentifier, "retrieveTest");
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
	@Ignore //400 Doesn't exists in the qabel-storage delete
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
	public void deleteWithMissingRevokeTokenFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete(delPublicIdentifier, "deleteBlobTest", null);
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
	@Ignore //404 Doesn't exists in the qabel-storage delete
	public void deleteWithNotExisitingVolumeIdFromStorage() throws IOException {
		//Given
		StorageHTTP storageHTTP = new StorageHTTP(this.server);
		//When
		HTTPResult<?> result = storageHTTP.delete("foo" + delPublicIdentifier, "deleteBlobTest", delRevokeToken);
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

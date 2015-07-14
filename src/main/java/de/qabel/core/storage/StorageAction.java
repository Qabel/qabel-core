package de.qabel.core.storage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;

import javax.crypto.SecretKey;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import de.qabel.core.config.StorageServer;
import de.qabel.core.config.StorageVolume;
import de.qabel.core.crypto.CryptoUtils;
import de.qabel.core.exceptions.QblStorageInvalidBlobName;
import de.qabel.core.exceptions.QblStorageInvalidToken;
import de.qabel.core.http.HTTPResult;
import de.qabel.core.http.StorageHTTP;

public class StorageAction {
	private final static Logger logger = LoggerFactory.getLogger(StorageAction.class.getName());

	/**
	 * Creates a new storage volume on the given storage server.
	 * 
	 * @param server Server to host the volume.
	 * @return New storage volume.
	 * @throws IOException if server is overloaded.
	 */
	public static StorageVolume createStorageVolume(StorageServer server) throws IOException {
		StorageHTTP http = new StorageHTTP(server);
		HTTPResult<StorageVolume> result = http.createNewStorageVolume();
		StorageVolume volume = result.getData();

		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 503:
				logger.info("Storage server reported capacity shortcoming.");
				throw new IOException("Storage server overloaded.");
			default:
				logger.error("Volume creation failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}

		return volume;
	}

	/**
	 * Checks existence of storage volume.
	 * 
	 * @param volume Volume to check.
	 * @return true if volume exists, otherfile false.
	 * @throws IOException
	 */
	public static boolean existsStorageVolume(StorageVolume volume) throws IOException {
		StorageHTTP http = new StorageHTTP(volume.getStorageServer());
		HTTPResult<?> result = http.probeStorageVolume(volume.getPublicIdentifier());

		// Responses other than 200 or 404 need to be handled as exceptions
		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 400:
				logger.error("Volume probing failed because of syntactically invalid request.");
				throw new RuntimeException("Unexpected response from storage server");
			case 404:
				logger.debug("Volume probing negative.");
				return false;
			default:
				logger.error("Volume probing failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}

		return true;
	}

	/**
	 * Deletes storage volume from storage server.
	 * 
	 * @param volume Volume to delete.
	 * @throws IOException
	 * @throws QblStorageInvalidToken if revoke token is invalid.
	 */
	public static void deleteStorageVolume(StorageVolume volume) throws IOException, QblStorageInvalidToken {
		StorageHTTP http = new StorageHTTP(volume.getStorageServer());
		HTTPResult<?> result = http.delete(volume.getPublicIdentifier(), "",
				volume.getRevokeToken());

		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 400:
				logger.error("Volume deletion failed because of syntactically invalid request.");
				throw new RuntimeException("Unexpected response from storage server");
			case 401:
				logger.error("Volume deletion failed because of missing token.");
				throw new RuntimeException("Unexpected response from storage server");
			case 403:
				logger.error("Volume deletion failed because of invalid token.");
				throw new QblStorageInvalidToken();
			case 404:
				logger.debug("Volume deletion failed because of unknown volume.");
				throw new RuntimeException("Unexpected response from storage server");
			default:
				logger.error("Volume deletion failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}
	}

	/**
	 * Uploads storage blob to server.
	 * 
	 * @param volume volume to put blob in.
	 * @param blob blob to upload.
	 * @param key secret key used for encryption.
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws QblStorageInvalidToken if write token is invalid.
	 */
	public static void uploadBlob(StorageVolume volume, StorageBlob blob, SecretKey key) throws IOException,
			InvalidKeyException, QblStorageInvalidToken {
		StorageHTTP http = new StorageHTTP(volume.getStorageServer());
		CryptoUtils cryptoUtils = new CryptoUtils();

		

		OutputStream out = http.prepareUpload(volume.getPublicIdentifier(),
				blob.getName(), volume.getToken());
		cryptoUtils.encryptStreamAuthenticatedSymmetric(blob.getInputStream(),
				out, key, null);
		HTTPResult<?> result = http.finishUpload();

		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 400:
				logger.error("Blob upload failed because of syntactically invalid request url.");
				throw new RuntimeException("Unexpected response from storage server");
			case 401:
				logger.error("Blob upload failed because of missing token.");
				throw new RuntimeException("Unexpected response from storage server");
			case 403:
				logger.error("Blob upload failed because of invalid token.");
				throw new QblStorageInvalidToken();
			case 404:
				logger.error("Blob upload failed because of unlocatable volume.");
				throw new RuntimeException("Unexpected response from storage server");
			default:
				logger.error("Blob upload failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}
	}

	/**
	 * Retrieve blob from storage server.
	 * 
	 * @param volume storage volume containing the blob.
	 * @param blobName name of the blob.
	 * @param key secret key to decrypt the downloaded blob.
	 * @return file containing the decrypted blob.
	 * @throws IOException
	 * @throws InvalidKeyException
	 * @throws QblStorageInvalidBlobName if given name contains non-Base64url characters.
	 */
	public static File retrieveBlob(StorageVolume volume, String blobName, SecretKey key) throws IOException,
			InvalidKeyException, QblStorageInvalidBlobName {
		StorageHTTP http = new StorageHTTP(volume.getStorageServer());
		CryptoUtils cryptoUtils = new CryptoUtils();

		StorageBlob.checkBlobName(blobName);
		HTTPResult<InputStream> result = http.retrieveBlob(volume.getPublicIdentifier(),
				blobName);

		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 400:
				logger.error("Blob retrieval failed because of syntactically invalid request url.");
				throw new RuntimeException("Unexpected response from storage server");
			case 404:
				logger.error("Blob retrieval failed because of unlocatable blob.");
				throw new RuntimeException("Unexpected response from storage server");
			default:
				logger.error("Blob retrieval failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}

		InputStream input = result.getData();
		File decryptedDataFile = File.createTempFile("blob", ".dec");
		boolean decryptionSuccessful = false;
		try {
			decryptionSuccessful = cryptoUtils.decryptFileAuthenticatedSymmetricAndValidateTag(input,
					decryptedDataFile, key);
		} finally {
			if (input != null) {
				input.close();
			}
			if (!decryptionSuccessful) {
				// something went wrong during decryption
				// delete file to avoid leakage of unauthenticated data
				decryptedDataFile.delete();
				decryptedDataFile = null;
			}
		}

		return decryptedDataFile;
	}

	/**
	 * Deletes blob from storage server.
	 * 
	 * @param volume storage volume containing the blob.
	 * @param blobName name of blob to be deleted.
	 * @throws IOException
	 * @throws QblStorageInvalidToken if the token is invalid.
	 * @throws QblStorageInvalidBlobName if the name contains non-Base64url characters.
	 */
	public static void deleteBlob(StorageVolume volume, String blobName) throws IOException, QblStorageInvalidToken,
			QblStorageInvalidBlobName {
		StorageHTTP http = new StorageHTTP(volume.getStorageServer());
		StorageBlob.checkBlobName(blobName);
		HTTPResult<?> result = http.delete(volume.getPublicIdentifier(), blobName,
				volume.getRevokeToken());

		if (!result.isOk()) {
			switch (result.getResponseCode()) {
			case 400:
				logger.error("Blob deletion failed because of syntactically invalid request url.");
				throw new RuntimeException("Unexpected response from storage server");
			case 401:
				logger.error("Blob deletion failed because of missing token.");
				throw new RuntimeException("Unexpected response from storage server");
			case 403:
				logger.error("Blob deletion failed because of invalid token.");
				throw new QblStorageInvalidToken();
			case 404:
				logger.error("Blob deletion failed because of unlocatable resource.");
				throw new RuntimeException("Unexpected response from storage server");
			default:
				logger.error("Blob deletion failed with unexpected response " + result.getResponseCode());
				throw new RuntimeException("Unexpected response from storage server");
			}
		}
	}
}

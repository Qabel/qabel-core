package de.qabel.core.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import de.qabel.core.config.StorageServer;
import de.qabel.core.config.StorageVolume;

import java.io.*;
import java.net.*;

public class StorageHTTP {
	private HttpURLConnection connection;
	private StorageServer server;
	
	public StorageHTTP(StorageServer server) {
		this.server = server;
	}

	/**
	 * Sends a request to the storage server, which creates a new Qabel Storage Volume and returns the request result.
	 * @return HTTPResult
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<StorageVolume> createNewStorageVolume() throws IOException {
		this.setupConnection("_new");
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		int responseCode = connection.getResponseCode();
		HTTPResult<StorageVolume> result = new HTTPResult<>();
		result.setResponseCode(responseCode);
		if(responseCode == 201) {
			result.setOk(true);
			String response = parsePostResponse(connection.getInputStream());
			result.setData(jsonStringToStorageVolume(response, server));
		}
		this.closeConnection();
		return result;
	}

	/**
	 * Probes the given StorageServer with the publicIdentifier.
	 * @param publicIdentifier The public identifier we want to probe.
	 * @return HTTPResult with empty data
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<?> probeStorageVolume(String publicIdentifier) throws IOException {
		this.setupConnection(publicIdentifier);
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		HTTPResult<?> result = new HTTPResult<>();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		this.closeConnection();
		return result;
	}

	/**
	 * Prepares the upload of a blob to a storage volume.
	 *
	 * @param publicIdentifier identifier of the containing storage volume.
	 * @param blobName name of the uploaded blob.
	 * @param token Token granting the right to upload blob to this storage volume.
	 * @return OutputStream usable for uploading data.
	 * @throws IOException
	 */
	public OutputStream prepareUpload(String publicIdentifier, String blobName, String token) throws IOException {
		if (connection != null) {
			throw new IOException("Connection already established.");
		}
		this.setupConnection(publicIdentifier, blobName);
		connection.setDoOutput(true);
		connection.setRequestProperty("X-Qabel-Token", token);
		return connection.getOutputStream();
	}

	/**
	 * Finishes to upload of a blob. Requires prepareUpload to be called before.
	 * @return HTTPResult with empty data.
	 * @throws IOException if no upload has been prepared first.
	 */
	public HTTPResult<?> finishUpload() throws IOException {
		if (connection == null) {
			throw new IOException("No connection prepared for upload.");
		}
		OutputStream out = connection.getOutputStream();
		out.flush();
		out.close();
		int responseCode = connection.getResponseCode();
		HTTPResult<?> result = new HTTPResult<>();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		this.closeConnection();
		return result;
	}

	/**
	 * Retrieves a blob/file from the url (baseUrl + publicIdentifier + blobName).
	 * @param publicIdentifier Where the file should be received from.
	 * @param blobName The blob name, which should be downloaded.
	 * @return HTTPResult
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<InputStream> retrieveBlob(String publicIdentifier, String blobName) throws IOException {
		this.setupConnection(publicIdentifier, blobName);
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		HTTPResult<InputStream> result = new HTTPResult<>();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		if (result.isOk()) {
			result.setData(connection.getInputStream());
		}
		return result;
	}

	/**
	 * Deletes a blob or the whole Qabel Storage Volume.
	 * @param publicIdentifier Where the file should be received from.
	 * @param blobName The blob name, which should be downloaded.
	 * @param revokeToken The token, which is required to delete a blob or a qabel storage volume.
	 * @throws IOException If something went wrong with the connection
	 * @return HTTPResult  with empty data
	 */
	public HTTPResult<?> delete(String publicIdentifier, String blobName, String revokeToken) throws IOException {
		this.setupConnection(publicIdentifier, blobName);
		connection.setRequestProperty("X-Qabel-Token", revokeToken);
		connection.setRequestMethod("DELETE");
		int responseCode = connection.getResponseCode();
		HTTPResult<?> result = new HTTPResult<>();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 204);
		this.closeConnection();
		return result;
	}

	private void setupConnection(String publicIdentifier, String blobName) throws IOException {
		StringBuilder resourcePath = new StringBuilder(publicIdentifier + "/");
		if (blobName != null) {
			resourcePath.append(blobName);
		}
		URL url = addPathToURL(server.getUrl(), resourcePath.toString());
		connection = (HttpURLConnection)url.openConnection();
	}
	
	private void setupConnection(String publicIdentifier) throws IOException {
		this.setupConnection(publicIdentifier, null);
	}
	
	private void closeConnection() {
		connection.disconnect();
		connection = null;
	}

	/**
	 * Parses the response and returns it.
	 * @param inputStream From which we parse the response.
	 * @return The parsed response from the inputStream.
	 * @throws IOException
	 */
	private String parsePostResponse(InputStream inputStream) throws IOException{
		StringBuilder sb = new StringBuilder();
		BufferedReader br = null;
		String line;
		br = new BufferedReader(new InputStreamReader(inputStream));
		while ((line = br.readLine()) != null) {
			sb.append(line);
			sb.append("\n");
		}
		br.close();
		return sb.toString();
	}

	/**
	 * Creates a StorageVolume from the json response string.
	 * @param response The parsed response.
	 * @return The new StorageVolume.
	 */
	private StorageVolume jsonStringToStorageVolume(String response, StorageServer server) {
		JsonParser jsonParser = new JsonParser();
		JsonObject jo = (JsonObject) jsonParser.parse(response);

		String public_token = jo.get("public").getAsString();
		String revoke_token = jo.get("revoke_token").getAsString();
		String private_token = jo.get("token").getAsString();
		return new StorageVolume(server, public_token, private_token, revoke_token);
	}

	/**
	 * Adds the newPath to the url.
	 * @param url The url, which should get the new path appended.
	 * @param newPath The path, which should be added, a pre-slash is not required.
	 * @return The new url.
	 * @throws MalformedURLException If the creation of the new URL failed.
	 */
	private URL addPathToURL(URL url, String newPath) throws MalformedURLException{
		String newURL = url.toString() + "/" + newPath;
		return new URL(newURL);
	}
}

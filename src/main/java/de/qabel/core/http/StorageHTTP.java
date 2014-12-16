package de.qabel.core.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.qabel.core.config.StorageVolume;

import java.io.*;
import java.net.*;

public class StorageHTTP {

	/**
	 * Sends a request to the storage server, which creates a new Qabel Storage Volume and returns the request result.
	 * @param url Base url of the storage server.
	 * @return HTTPResult
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<StorageVolume> createNewStorageVolume(URL url) throws IOException {
		HTTPResult<StorageVolume> result = new HTTPResult<>();
		url = addPathToURL(url, "_new");
		HttpURLConnection connection = (HttpURLConnection) this.setupConnection(url);
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		int responseCode = connection.getResponseCode();
		result.setResponseCode(responseCode);
		if(responseCode == 201) {
			result.setOk(true);
			String response = parsePostResponse(connection.getInputStream());
			result.setData(jsonStringToStorageVolume(response));
		}
		connection.disconnect();
		return result;
	}

	/**
	 * Probes the given StorageServer with the publicIdentifier.
	 * @param baseUrl The baseUrl of a StorageServer.
	 * @param publicIdentifier The public identifier we want to probe.
	 * @return HTTPResult with empty data
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<?> probeStorageVolume(URL baseUrl, String publicIdentifier) throws IOException {
		URL url = addPathToURL(baseUrl, publicIdentifier + "/");
		HttpURLConnection connection = (HttpURLConnection) this.setupConnection(url);
		HTTPResult<?> result = new HTTPResult<>();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		connection.disconnect();
		return result;
	}

	/**
	 * Uploads the given blob to the url (baseUrl + publicIdentifier + blobName).
	 * @param baseUrl The baseUrl of a StorageServer.
	 * @param publicIdentifier Where the blob should be uploaded to.
	 * @param blobName The blob/blob name for the upload.
	 * @param token The token, which is required to upload files to the publicIdentifier.
	 * @param blob The blob in bytes, which the user wants to upload.
	 * @return HTTPResult with empty data
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<?> upload(URL baseUrl, String publicIdentifier, String blobName, String token, byte[] blob) throws IOException {
		URL url = addPathToURL(baseUrl, publicIdentifier + "/" + blobName);
		HttpURLConnection connection = (HttpURLConnection) this.setupConnection(url);
		connection.setRequestProperty("X-Qabel-Token", token);
		connection.setDoOutput(true);
		DataOutputStream out;
		HTTPResult<?> result = new HTTPResult<>();
		out = new DataOutputStream(connection.getOutputStream());
		out.write(blob);
		out.flush();
		out.close();
		int responseCode = connection.getResponseCode();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		connection.disconnect();
		return result;
	}

	/**
	 * Retrieves a blob/file from the url (baseUrl + publicIdentifier + blobName).
	 * @param baseUrl  The baseUrl of a StorageServer.
	 * @param publicIdentifier Where the file should be received from.
	 * @param blobName The blob name, which should be downloaded.
	 * @return HTTPResult
	 * @throws IOException If something went wrong with the connection
	 */
	public HTTPResult<InputStream> retrieveBlob(URL baseUrl, String publicIdentifier, String blobName) throws IOException {
		URL url = addPathToURL(baseUrl, publicIdentifier + "/" + blobName);
		HttpURLConnection connection = (HttpURLConnection) this.setupConnection(url);
		HTTPResult<InputStream> result = new HTTPResult<>();
		connection.setRequestMethod("GET");
		int responseCode = connection.getResponseCode();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 200);
		if (result.isOk()) {
			result.setData(connection.getInputStream());
		}
		return result;
	}

	/**
	 * Deletes a blob or the whole Qabel Storage Volume.
	 * @param baseUrl The baseUrl of a StorageServer.
	 * @param publicIdentifier Where the file should be received from.
	 * @param blobName The blob name, which should be downloaded.
	 * @param revokeToken The token, which is required to delete a blob or a qabel storage volume.
	 * @throws IOException If something went wrong with the connection
	 * @return HTTPResult  with empty data
	 */
	public HTTPResult<?> delete(URL baseUrl, String publicIdentifier, String blobName, String revokeToken) throws IOException {
		URL url = addPathToURL(baseUrl, publicIdentifier + "/" + blobName);
		HttpURLConnection connection = (HttpURLConnection) this.setupConnection(url);
		HTTPResult<?> result = new HTTPResult<>();
		connection.setRequestProperty("X-Qabel-Token", revokeToken);
		connection.setRequestMethod("DELETE");
		int responseCode = connection.getResponseCode();
		result.setResponseCode(responseCode);
		result.setOk(responseCode == 204);
		connection.disconnect();
		return result;
	}

	/**
	 * Setups the connection to the given url.
	 * @param url The whole url to the Qabel Storage Volume.
	 * @return The open connection.
	 * @throws IOException If something went wrong with the connection
	 */
	private URLConnection setupConnection(URL url) throws IOException {
		return url.openConnection();
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
	private StorageVolume jsonStringToStorageVolume(String response) {
		JsonParser jsonParser = new JsonParser();
		JsonObject jo = (JsonObject) jsonParser.parse(response);

		String public_token = jo.get("public").getAsString();
		String revoke_token = jo.get("revoke_token").getAsString();
		String private_token = jo.get("token").getAsString();
		return new StorageVolume(public_token, private_token, revoke_token);
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

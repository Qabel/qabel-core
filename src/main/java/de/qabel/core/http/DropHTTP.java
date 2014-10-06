package de.qabel.core.http;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

public class DropHTTP {

	String dateFormat;

	public int send(URL url, byte[] message) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setDoOutput(true); // indicates POST method
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "multipart/mixed");

		// conn.setFixedLengthStreamingMode();
		DataOutputStream out;
		try {
			out = new DataOutputStream(conn.getOutputStream());
			out.write(message);
			out.flush();
			out.close();
			responseCode = conn.getResponseCode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();

		}
		return responseCode;
	}

	public String receiveMessages(URL url) {
		return this.receiveMessages(url, 0);
	}

	public String receiveMessages(URL url, long sinceDate) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setIfModifiedSince(sinceDate);
		String result = "";
		try {
			conn.setRequestMethod("GET");
			responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				InputStream response = conn.getInputStream();
				result = this.convertStreamToString(response);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return result;
	}

	public int head(URL url) {
		return this.head(url, 0);
	}

	public int head(URL url, long sinceDate) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setIfModifiedSince(sinceDate);
		try {
			conn.setRequestMethod("GET");
			responseCode = conn.getResponseCode();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return responseCode;
	}

	private URLConnection setupConnection(URL url) {
		URLConnection conn = null;
		try {
			conn = url.openConnection();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

	private String convertStreamToString(InputStream is) {
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return sb.toString();

	}
}

package de.qabel.core.http;

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;

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

	public Collection<String> receiveMessages(URL url) {
		return this.receiveMessages(url, 0);
	}

	public Collection<String> receiveMessages(URL url, long sinceDate) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setIfModifiedSince(sinceDate);
		Collection<String> messages = new ArrayList<String>();
		try {
			conn.setRequestMethod("GET");
			responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				InputStream inputstream = conn.getInputStream();
				MimeTokenStream stream = new MimeTokenStream();
				stream.parse(inputstream);
				for (EntityState state = stream.getState();
					 state != EntityState.T_END_OF_STREAM;
					 state = stream.next()) {
					if (state == EntityState.T_BODY) {
						messages.add(this.convertStreamToString(stream.getInputStream()));
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (MimeException e) {
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return messages;
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

package de.qabel.core.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Date;

import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.ModelObject;

public class DropHTTP {

	String dateFormat;

	public int send(URL url, DropMessage<ModelObject> message) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "multipart/mixed");

		// conn.setFixedLengthStreamingMode();
		OutputStreamWriter out;
		try {
			out = new OutputStreamWriter(conn.getOutputStream());
			out.write("foo=bar");
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

	public int receiveMessages(URL url) {
		return this.receiveMessages(url, 0);
	}

	public int receiveMessages(URL url, long sinceDate) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setIfModifiedSince(0);
		try {
			conn.setRequestMethod("GET");
			responseCode = conn.getResponseCode();
			if (responseCode == 200) {
				InputStream response = conn.getInputStream();
				System.out.println(response.read());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			conn.disconnect();
		}
		return responseCode;
	}

	public int head(URL url) {
		return this.head(url, 0);
	}

	public int head(URL url, long sinceDate) {
		int responseCode = 0;
		HttpURLConnection conn = (HttpURLConnection) this.setupConnection(url);
		conn.setIfModifiedSince(0);
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

	public void setSinceDate(Date date) {
		// TODO Auto-generated method stub

	}

	public ArrayList<String> getHTTPBody() {
		// TODO Auto-generated method stub
		return new ArrayList<String>();
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
}

package de.qabel.core.drop.http;

import de.qabel.core.http.HTTPResult;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.utils.DateUtils;
import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.stream.EntityState;
import org.apache.james.mime4j.stream.MimeTokenStream;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URLConnection;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class DropHTTP {

    public HTTPResult<?> send(URI uri, byte[] message) {
        HTTPResult<?> result = new HTTPResult<>();
        HttpURLConnection conn = (HttpURLConnection) setupConnection(uri);
        conn.setDoOutput(true); // indicates POST method
        conn.setDoInput(true);
        conn.setRequestProperty("Content-Type", "application/octet-stream");
        conn.setRequestProperty("Authorization", "Client Qabel");

        // conn.setFixedLengthStreamingMode();
        DataOutputStream out;
        try {
            out = new DataOutputStream(conn.getOutputStream());
            out.write(message);
            out.flush();
            out.close();
            result.setResponseCode(conn.getResponseCode());
            result.setOk(conn.getResponseCode() == 200);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally {
            conn.disconnect();

        }
        return result;
    }

    public HTTPResult<Collection<byte[]>> receiveMessages(URI uri) throws IOException {
        return receiveMessages(uri, 0);
    }

    public HTTPResult<Collection<byte[]>> receiveMessages(URI uri, long sinceDate) throws IOException {
        HTTPResult<Collection<byte[]>> result = new HTTPResult<>();
        HttpURLConnection conn = (HttpURLConnection) setupConnection(uri);
        conn.setIfModifiedSince(sinceDate);
        Collection<byte[]> messages = new ArrayList<>();
        try {
            conn.setRequestMethod("GET");
            result.setResponseCode(conn.getResponseCode());
            result.setOk(conn.getResponseCode() == 200);
            if (result.isOk()) {
                if (conn.getHeaderField("Last-Modified") != null) {
                    try {
                        result.setLastModified(parseDate(conn.getHeaderField("Last-Modified")));
                    } catch (ParseException ignored) {
                    }
                }
                InputStream inputstream = conn.getInputStream();
                MimeTokenStream stream = new MimeTokenStream();
                stream.parseHeadless(inputstream, conn.getContentType());
                for (EntityState state = stream.getState();
                     state != EntityState.T_END_OF_STREAM;
                     state = stream.next()) {
                    if (state == EntityState.T_BODY) {
                        byte[] message = IOUtils.toByteArray(stream.getInputStream());
                        messages.add(message);
                    }
                }
            }
        } catch (MimeException e) {
            throw new IllegalStateException("error while parsing mime response: " + e.getMessage(), e);
        } finally {
            conn.disconnect();
        }
        result.setData(messages);
        return result;
    }

    private Date parseDate(String dateHeader) throws ParseException {
        return DateUtils.parseDate(dateHeader);
    }

    public HTTPResult<?> head(URI uri) throws IOException {
        return head(uri, 0);
    }

    public HTTPResult<?> head(URI uri, long sinceDate) throws IOException {
        HTTPResult<?> result = new HTTPResult<>();
        HttpURLConnection conn = (HttpURLConnection) setupConnection(uri);
        conn.setIfModifiedSince(sinceDate);
        try {
            conn.setRequestMethod("GET");
            result.setResponseCode(conn.getResponseCode());
            result.setOk(conn.getResponseCode() == 200);
        } finally {
            conn.disconnect();
        }
        return result;
    }

    private URLConnection setupConnection(URI uri) {
        URLConnection conn = null;
        try {
            conn = uri.toURL().openConnection();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return conn;
    }
}

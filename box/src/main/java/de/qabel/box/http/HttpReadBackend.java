package de.qabel.box.http;

import de.qabel.box.storage.exceptions.QblStorageException;
import de.qabel.box.storage.exceptions.QblStorageNotFound;
import de.qabel.box.storage.StorageDownload;
import de.qabel.box.storage.StorageReadBackend;
import de.qabel.box.storage.UnmodifiedException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public class HttpReadBackend extends AbstractHttpStorageBackend implements StorageReadBackend {
    public HttpReadBackend(String root) throws URISyntaxException {
        super(root);
    }

    @Override
    public StorageDownload download(String name) throws QblStorageException {
        try {
            return download(name, null);
        } catch (UnmodifiedException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public StorageDownload download(String name, String ifModifiedVersion) throws QblStorageException, UnmodifiedException {
        info("Downloading " + name);
        URI uri = getRoot().resolve(name);
        HttpGet httpGet = new HttpGet(uri);
        if (ifModifiedVersion != null) {
            httpGet.addHeader(HttpHeaders.IF_NONE_MATCH, ifModifiedVersion);
        }
        prepareRequest(httpGet);

        try {
            CloseableHttpResponse response = getHttpclient().execute(httpGet);
            try {
                int status = response.getStatusLine().getStatusCode();
                if (status == HttpStatus.SC_NOT_FOUND || status == HttpStatus.SC_FORBIDDEN) {
                    throw new QblStorageNotFound("File not found");
                }
                if (status == HttpStatus.SC_NOT_MODIFIED) {
                    throw new UnmodifiedException();
                }
                if (status != HttpStatus.SC_OK) {
                    throw new QblStorageException("Download error");
                }
                String modifiedVersion = response.getFirstHeader(HttpHeaders.ETAG).getValue();

                if (ifModifiedVersion != null && modifiedVersion.equals(ifModifiedVersion)) {
                    throw new UnmodifiedException();
                }
                HttpEntity entity = response.getEntity();
                if (entity == null) {
                    throw new QblStorageException("No content");
                }
                InputStream content = entity.getContent();
                return new StorageDownload(content, modifiedVersion, entity.getContentLength(), response);
            } catch (Exception e) {
                response.close();
                throw e;
            }
        } catch (IOException e) {
            throw new QblStorageException(e);
        }
    }

    @Override
    public String getUrl(String meta) {
        return getRoot().resolve(meta).toString();
    }
}

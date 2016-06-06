package de.qabel.box.storage;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class StorageDownload implements Closeable {
    private InputStream inputStream;
    private String mHash;
    private long size;
    private Closeable closeable;

    public StorageDownload(InputStream inputStream, String mHash, long size) {
        this(inputStream, mHash, size, null);
    }

    public StorageDownload(InputStream inputStream, String mHash, long size, Closeable closeable) {
        this.inputStream = inputStream;
        this.mHash = mHash;
        this.size = size;
        this.closeable = closeable;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public String getMHash() {
        return mHash;
    }

    public long getSize() {
        return size;
    }

    @Override
    public void close() throws IOException {
        if (closeable != null) {
            closeable.close();
        }
    }
}

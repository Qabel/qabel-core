package de.qabel.box.storage;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ProgressInputStream extends FilterInputStream {
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Consumer<Long> consumer;
    private long read;

    public ProgressInputStream(InputStream in, Consumer<Long> consumer) {
        super(in);
        this.consumer = consumer;
    }

    @Override
    public int read() throws IOException {
        int read = super.read();
        add(1);
        return read;
    }

    private void add(long bytes) {
        if (bytes <= 0) {
            return;
        }
        read += bytes;
        update();
    }

    private void update() {
        executor.submit(() -> consumer.accept(read));
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = super.read(b, off, len);
        add(read);
        return read;
    }

    @Override
    public long skip(long n) throws IOException {
        long skip = super.skip(n);
        add(skip);
        return skip;
    }

    @Override
    public void close() throws IOException {
        update();
        super.close();
    }

    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }
}

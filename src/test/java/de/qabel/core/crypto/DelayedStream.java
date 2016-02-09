package de.qabel.core.crypto;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DelayedStream extends FilterInputStream {
	private boolean blockOnRead = true;
	private boolean blocked = false;

	protected DelayedStream(InputStream in) {
		super(in);
	}

	@Override
	public int read() throws IOException {
		while(blocked)
			Thread.yield();
		if (blockOnRead)
			block();
		return super.read();
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		while(blocked)
			Thread.yield();
		if (blockOnRead)
			block();
		return super.read(b, off, len);
	}

	public void block() {
		blocked = true;
	}

	public void unblock() {
		blocked = false;
	}

	public void setBlockOnRead(boolean blockOnRead) {
		this.blockOnRead = blockOnRead;
	}

	public boolean isBlocked() {
		return blocked;
	}
}

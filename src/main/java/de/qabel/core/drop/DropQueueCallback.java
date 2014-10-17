package de.qabel.core.drop;

import java.util.concurrent.LinkedBlockingQueue;

public class DropQueueCallback<T extends ModelObject> extends
		LinkedBlockingQueue<DropMessage<T>> implements DropCallback<T> {

	private static final long serialVersionUID = -3961632733846834316L;

	@Override
	public void onDropMessage(DropMessage<T> message) {
		try {
			this.put(message);
		} catch (InterruptedException e) {
			// TODO: HANDLE THIS
			e.printStackTrace();
		}
	}

}

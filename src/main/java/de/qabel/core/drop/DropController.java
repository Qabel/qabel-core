package de.qabel.core.drop;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class DropController {

	/**
	 * <pre>
	 *           0..*     0..*
	 * DropController ------------------------> DropListener
	 *           dropController        &gt;       dropListener
	 * </pre>
	 */
	private Map<Class<? extends ModelObject>, ArrayList<BlockingQueue<DropMessage<ModelObject>>>> mapBlockingQueues;

	public DropController() {
		this.mapBlockingQueues = new HashMap<Class<? extends ModelObject>, ArrayList<BlockingQueue<DropMessage<ModelObject>>>>();
	}

	/**
	 * Register for DropMessages with a modelObject
	 * 
	 * @param modelObject
	 *            ModelObject to receive DropMessages for
	 * @return a queue in which the DropController will put received
	 *         DropMessages with the selected ModelObject
	 */
	public BlockingQueue<DropMessage<ModelObject>> register(
			ModelObject modelObject) {

		ArrayList<BlockingQueue<DropMessage<ModelObject>>> alBlockingQueue = mapBlockingQueues
				.get(modelObject.getClass());
		if (alBlockingQueue == null) {
			alBlockingQueue = new ArrayList<BlockingQueue<DropMessage<ModelObject>>>();
			mapBlockingQueues.put(modelObject.getClass(), alBlockingQueue);
		}

		BlockingQueue<DropMessage<ModelObject>> bq = new LinkedBlockingQueue<DropMessage<ModelObject>>();
		alBlockingQueue.add(bq);
		return bq;
	}

	/**
	 * Handles a received DropMessage. Puts this DropMessage into the registered
	 * Queues.
	 * 
	 * @param dm
	 *            DropMessage which should be handled
	 */
	public void handleDrop(DropMessage<? extends ModelObject> dm) {
		for (BlockingQueue<DropMessage<ModelObject>> bq : mapBlockingQueues
				.get(dm.getModelObject())) {
			try {
				bq.put((DropMessage<ModelObject>) dm);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}

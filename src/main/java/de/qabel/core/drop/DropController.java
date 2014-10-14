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
	private Map<Class<? extends ModelObject>, ArrayList<DropListener>> dropListeners;
	private Map<DropListener, ArrayList<BlockingQueue<DropMessage<ModelObject>>>> blockingQueues;

	public DropController() {
		this.dropListeners = new HashMap<Class<? extends ModelObject>, ArrayList<DropListener>>();
		this.blockingQueues = new HashMap<DropListener, ArrayList<BlockingQueue<DropMessage<ModelObject>>>>();
	}

	public BlockingQueue<DropMessage<ModelObject>> register(ModelObject modelObject, DropListener dropListener) {
		ArrayList<DropListener> ar = dropListeners.get(modelObject.getClass());
		if (ar == null) {
			ar = new ArrayList<DropListener>();
			dropListeners.put(modelObject.getClass(), ar);
		}
		ar.add(dropListener);
		
		ArrayList<BlockingQueue<DropMessage<ModelObject>>> listBlockingQueue = blockingQueues
				.get(modelObject.getClass());
		if (listBlockingQueue == null){
			listBlockingQueue = new ArrayList<BlockingQueue<DropMessage<ModelObject>>>();
			blockingQueues.put(dropListener, listBlockingQueue);
		}
		
		BlockingQueue<DropMessage<ModelObject>> bq = new LinkedBlockingQueue<DropMessage<ModelObject>>();
		listBlockingQueue.add(bq);
		return bq;
	}

	public void handleDrop(DropMessage<? extends ModelObject> dm) {
		for (DropListener dl : dropListeners.get(dm.getModelObject())) {
			dl.onDropEvent((DropMessage<ModelObject>) dm);
			
			for (BlockingQueue<DropMessage<ModelObject>> bq : blockingQueues.get(dl)){
				try {
					bq.put((DropMessage<ModelObject>) dm);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}			
		}
	}
}

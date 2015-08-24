package de.qabel.core.drop;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.module.Module;
import de.qabel.core.module.ModuleManager;

import java.util.concurrent.LinkedBlockingQueue;

public class DropCommunicatorUtil extends Module {
	LinkedBlockingQueue<DropMessage> inputqueue = new LinkedBlockingQueue<>();
	private ResourceActor resourceActor;
	private DropActor dropActor;
	private Thread dropActorThread;
	private Identities identities;

	public DropCommunicatorUtil(ModuleManager moduleManager) {
		super(moduleManager);
	}

	static public DropCommunicatorUtil newInstance(ResourceActor resourceActor, EventEmitter emitter, Contacts contacts, Identities identities) throws IllegalAccessException, InstantiationException {
		DropActor dropActor = new DropActor(resourceActor, emitter);

		Thread dropActorThread = new Thread(dropActor, "dropActor");
		dropActor.setInterval(500);
		dropActorThread.start();
		ModuleManager manager = new ModuleManager(emitter, resourceActor);
		try {
			DropCommunicatorUtil util = manager.startModule(DropCommunicatorUtil.class);
			util.resourceActor = resourceActor;
			util.resourceActor.writeContacts(contacts.getContacts().toArray(new Contact[0]));
			util.resourceActor.writeIdentities(identities.getIdentities().toArray(new Identity[0]));

			util.identities = identities;
			util.dropActor = dropActor;
			util.dropActorThread = dropActorThread;
			return util;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public DropMessage retrieve() throws InterruptedException {
		return inputqueue.take();
	}

	@Override
	public void run() {
		super.run();
	}

	@Override
	public void stop() {
		this.dropActor.stop();
		this.dropActor.unregister();
		this.resourceActor.removeIdentities(identities.getIdentities().toArray(new Identity[0]));
		try {
			this.dropActorThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void init() {
	}

	@Override
	public void moduleMain() {

	}

	@Override
	public void onEvent(String event, MessageInfo info, Object... data) {
		try {
			this.inputqueue.put((DropMessage) data[0]);
		} catch (InterruptedException e) {
			// TODO
		}
	}

	public void registerModelObject(Class cls) {
		registerModelObject(cls.getName());
	}

	public void registerModelObject(String type) {
		on(DropActor.EVENT_DROP_MESSAGE_RECEIVED_PREFIX + type, this);
	}
}

package de.qabel.core.drop;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServers;
import de.qabel.core.config.Identities;
import de.qabel.core.module.Module;
import de.qabel.core.module.ModuleManager;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DropCommunicatorUtil extends Module {
	private EventEmitter emitter;
	LinkedBlockingQueue<DropMessage<?>> inputqueue = new LinkedBlockingQueue<>();
	private DropActor dropActor;
	private Thread dropActorThread;
	private Contacts contacts;
	private Identities identities;
	private DropServers dropServers;

	public DropCommunicatorUtil(EventEmitter emitter) {
		super(emitter);
	}

	static public DropCommunicatorUtil newInstance(EventEmitter emitter, Contacts contacts, Identities identities, DropServers dropServers) throws IllegalAccessException, InstantiationException {
		DropActor dropActor = new DropActor(emitter);
		dropActor.setContacts(contacts);
		dropActor.setDropServers(dropServers);
		dropActor.setIdentities(identities);
		Thread dropActorThread = new Thread(dropActor, "dropActor");
		dropActor.setInterval(500);
		dropActorThread.start();
		ModuleManager manager = new ModuleManager(emitter);
		try {
			manager.startModule(DropCommunicatorUtil.class);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		DropCommunicatorUtil util = (DropCommunicatorUtil) manager.getModules().keySet().iterator().next();

		util.dropActor = dropActor;
		util.dropActorThread = dropActorThread;
		util.emitter = emitter;
		util.contacts = contacts;
		util.identities = identities;
		util.dropServers = dropServers;
		return util;
	}

	public DropMessage<?> retrieve() throws InterruptedException {
		return inputqueue.poll(2000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		super.run();
		this.dropActor.stop();
	}

	@Override
	public void init() {
	}

	@Override
	public void onDropMessage(DropMessage<?> dm) {
		try {
			this.inputqueue.put(dm);
		} catch (InterruptedException e) {
			// TODO
		}
	}

	@Override
	public void registerModelObject(Class<? extends ModelObject> cls) {
		super.registerModelObject(cls);
	}
}

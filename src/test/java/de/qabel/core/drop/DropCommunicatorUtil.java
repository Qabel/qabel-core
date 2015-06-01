package de.qabel.core.drop;

import de.qabel.ackack.event.EventEmitter;
import de.qabel.core.config.*;
import de.qabel.core.module.Module;
import de.qabel.core.module.ModuleManager;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class DropCommunicatorUtil extends Module {
	LinkedBlockingQueue<DropMessage<?>> inputqueue = new LinkedBlockingQueue<>();
	private ContactsActor contactsActor;
	private ConfigActor configActor;
	private DropActor dropActor;
	private Thread dropActorThread;
	private Identities identities;
	private DropServers dropServers;

	public DropCommunicatorUtil(EventEmitter emitter) {
		super(emitter);
	}

	static public DropCommunicatorUtil newInstance(EventEmitter emitter, Contacts contacts, Identities identities, DropServers dropServers) throws IllegalAccessException, InstantiationException {
		DropActor dropActor = new DropActor(emitter);

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

		util.contactsActor = ContactsActor.getDefault();
		util.configActor = ConfigActor.getDefault();
		util.contactsActor.writeContacts(contacts.getContacts().toArray(new Contact[0]));
		util.configActor.writeIdentities(identities.getIdentities().toArray(new Identity[0]));
		util.configActor.writeDropServers(dropServers.getDropServers().toArray(new DropServer[0]));

		util.identities = identities;
		util.dropServers = dropServers;
		util.dropActor = dropActor;
		util.dropActorThread = dropActorThread;
		return util;
	}

	public DropMessage<?> retrieve() throws InterruptedException {
		return inputqueue.poll(2000, TimeUnit.MILLISECONDS);
	}

	@Override
	public void run() {
		super.run();
	}

	@Override
	public void stop() {
		this.dropActor.stop();
		this.dropActor.unregister();
		this.configActor.removeIdentities(identities.getIdentities().toArray(new Identity[0]));
		this.configActor.removeDropServers(dropServers.getDropServers().toArray(new DropServer[0]));
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

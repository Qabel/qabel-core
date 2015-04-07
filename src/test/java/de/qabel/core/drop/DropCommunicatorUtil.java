package de.qabel.core.drop;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventActor;
import de.qabel.ackack.event.EventEmitter;
import de.qabel.ackack.event.EventListener;
import de.qabel.core.config.Contacts;
import de.qabel.core.config.DropServers;
import de.qabel.core.config.Identities;
import de.qabel.core.module.Module;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by tox on 3/29/15.
 */
public class DropCommunicatorUtil <T extends ModelObject> {
	private final EventEmitter emitter;
	LinkedBlockingQueue<DropMessage<T>> inputqueue = new LinkedBlockingQueue<>();
	private EventActor actor;
	private DropActor dropActor;
	private Thread actorThread;
	private Thread dropActorThread;
	Class<?> cls;
	public DropCommunicatorUtil(EventEmitter emitter) {
		this.emitter = emitter;
	}

	public void setCls(Class<?> cls) {
		this.cls = cls;
	}

	public void start(Contacts contacts, Identities identities, DropServers dropServers) throws InterruptedException {
		this.actor = new EventActor(emitter);
		this.actor.on(DropActor.EVENT_DROP_MESSAGE_RECEIVED, new EventListener() {
			@Override
			public void onEvent(String event, MessageInfo info, Object... data) {
				try {
					DropMessage<T> dropMessage = (DropMessage<T>) data[0];
					if(dropMessage.getClass() == cls || cls == null)
						inputqueue.put(dropMessage);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		});
		this.dropActor = new DropActor(emitter);
		this.dropActor.setContacts(contacts);
		this.dropActor.setDropServers(dropServers);
		this.dropActor.setIdentities(identities);
		this.actorThread = new Thread(actor, "actor");
		this.dropActorThread = new Thread(dropActor, "dropActor");
		this.dropActor.setInterval(500);
		actorThread.start();
		dropActorThread.start();

		Thread.sleep(1000);
	}

	public void stop() throws InterruptedException {
		this.actor.stop();
		this.dropActor.stop();
		this.actorThread.join();
		this.dropActorThread.join();
	}

	public DropMessage<T> retrieve() throws InterruptedException {
		return inputqueue.poll(2000, TimeUnit.MILLISECONDS);
	}
}

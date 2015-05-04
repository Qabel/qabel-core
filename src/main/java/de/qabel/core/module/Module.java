package de.qabel.core.module;

import de.qabel.ackack.MessageInfo;
import de.qabel.ackack.event.EventActor;
import de.qabel.ackack.event.EventListener;
import de.qabel.core.drop.DropActor;
import de.qabel.core.drop.DropMessage;
import de.qabel.core.drop.ModelObject;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public abstract class Module extends EventActor implements EventListener {
	HashSet<Class<?>> modelObjects = new HashSet<>();
	protected Module() {
		this.on(DropActor.EVENT_DROP_MESSAGE_RECEIVED, this);
	}
	/**
	 * <pre>
	 *           0..*     1..1
	 * Module ------------------------- ModuleManager
	 *           module        &lt;       moduleManager
	 * </pre>
	 */
	private ModuleManager moduleManager;


	/**
	 * 
	 * @param value
	 *            ModuleManager which managers this Module
	 */
	public void setModuleManager(ModuleManager value) {
		this.moduleManager = value;
	}

	/**
	 * gets the ModuleManager which manages this Module.
	 * 
	 * @return ModuleManager
	 */
	public ModuleManager getModuleManager() {
		return this.moduleManager;
	}

	/**
	 * Called by the modulemanager to set up this Module and registers Listener
	 */
	abstract public void init();

	/**
	 * stops the background thread. Overwrite this if you want to do cleanup work.
	 * Don't forget to call super.
	 * This should NOT be called from the background thread itself!
	 */
	public synchronized void stopModule() {
		this.stop();
		getModuleManager().getModules().remove(this);
	}

	protected void registerModelObject(Class<? extends ModelObject> cls) {
		modelObjects.add(cls);
	}

	@Override
	public void onEvent(String event, MessageInfo info, Object... data) {
		if(!event.equals(DropActor.EVENT_DROP_MESSAGE_RECEIVED)) {
			return;
		}
		DropMessage<?> dm = (DropMessage<?>) data[0];
        if(modelObjects.contains(dm.getClass())) {
			onDropMessage(dm);
		}
	}

    protected void onDropMessage(DropMessage<?> dm) {

    }
}

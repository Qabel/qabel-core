package de.qabel.core.module;

public abstract class Module extends Thread {
	protected Module(String name) {
		super(name);
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
	 * Called by the modulemanager to run the module. This is runned in an own
	 * Thread.
	 */
	abstract public void run();

	/**
	 * stops the background thread. Overwrite this if you want to do cleanup work.
	 * Don't forget to call super.
	 * This should NOT be called from the background thread itself!
	 */
	public synchronized void stopModule() {
		interrupt();
		while(this.isAlive()) {
			try {
				wait();
			} catch (InterruptedException e) { }
		}
		getModuleManager().getModules().remove(this);
	}
}

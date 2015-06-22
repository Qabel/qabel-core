package de.qabel.core.module;

/**
 * ModuleThread is used by the ModuleManager to start a Module in
 * a new thread and keep a reference from ModuleThread to the belonging Module.
 * When a ModuleThread is started, it creates one thread that runs the moduleMain()
 * and a second thread that runs the run() method a Module have to override from
 * the Actor class. This secondary thread handles receiving ov events in the Modules
 * onEvent() method.
 */
public class ModuleThread extends Thread {
    private final Module module;

    public ModuleThread(Module module) {
        super(module, module.getClass().getName());
        this.module = module;
    }

    public Module getModule() {
        return module;
    }

    /**
     * Starts the actor thread of a Module which will handle
     * received events in a new thread and moduleMain() in the current thread.
     */
	@Override
	public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                module.run();
            }
        }).start();
        module.moduleMain();
	}
}

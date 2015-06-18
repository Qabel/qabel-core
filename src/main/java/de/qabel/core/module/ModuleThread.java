package de.qabel.core.module;

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

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
}

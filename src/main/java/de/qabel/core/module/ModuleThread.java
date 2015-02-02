package de.qabel.core.module;

/**
 * Created by tox on 2/9/15.
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
}

package de.qabel.core.module;


public abstract class Module {
/**
 * <pre>
 *           0..*     1..1
 * Module ------------------------- ModuleManager
 *           module        &lt;       moduleManager
 * </pre>
 */
private ModuleManager moduleManager;

public void setModuleManager(ModuleManager value) {
   this.moduleManager = value;
}

public ModuleManager getModuleManager() {
   return this.moduleManager;
}

}

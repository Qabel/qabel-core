package de.qabel.core.storage;
import de.qabel.core.module.ModuleManager;

import java.util.Set;
import java.util.HashSet;

public class StorageConnection {
/**
 * <pre>
 *           0..*     0..*
 * StorageConnection ------------------------- ModuleManager
 *           storageConnection        &gt;       moduleManager
 * </pre>
 */
private Set<ModuleManager> moduleManager;

public Set<ModuleManager> getModuleManager() {
   if (this.moduleManager == null) {
this.moduleManager = new HashSet<ModuleManager>();
   }
   return this.moduleManager;
}


}

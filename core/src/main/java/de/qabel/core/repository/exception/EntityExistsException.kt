package de.qabel.core.repository.exception

class EntityExistsException(val msg: String = "Entity already exists") : PersistenceException(msg) {

}

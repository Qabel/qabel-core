package de.qabel.core.config

class EntityDelegate : EntityObservable {

    override fun removeObserver(observer: EntityObserver) {
        throw UnsupportedOperationException("not implemented")
    }

    private val observers: MutableList<EntityObserver> = mutableListOf()

    override fun attach(observer: EntityObserver) {
        observers += observer
    }

    override fun notifyObservers() = observers.forEach { it.update() }
}

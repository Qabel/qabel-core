package de.qabel.core.config

class SimpleEntityObservable : EntityObservable {

    private val observers: MutableList<EntityObserver> = mutableListOf()

    override fun removeObserver(observer: EntityObserver) {
        observers.remove(observer);
    }

    override fun attach(observer: EntityObserver) {
        observers += observer
    }

    override fun notifyObservers() = observers.forEach { it.update() }
}

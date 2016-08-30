package de.qabel.core.config

interface EntityObservable {
    fun attach(observer: EntityObserver)
    fun removeObserver(observer: EntityObserver)
    fun notifyObservers()
}

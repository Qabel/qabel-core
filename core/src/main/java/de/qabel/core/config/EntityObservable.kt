package de.qabel.core.config

@Deprecated(message = "use events instead", replaceWith = ReplaceWith("EventDispatcher"))
interface EntityObservable {
    fun attach(observer: EntityObserver)
    fun removeObserver(observer: EntityObserver)
    fun notifyObservers()
}

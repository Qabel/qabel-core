package de.qabel.core.config

interface EntityObservable {

    fun addObserver(observer: EntityObserver)

    fun removeObserver(observer: EntityObserver)

}

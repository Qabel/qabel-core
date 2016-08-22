package de.qabel.core.ui


interface DataView<in T> {

    fun getCount() : Int

    fun appendData(models : List<T>)
    fun prependData(models : List<T>)
    fun reset()

    fun handleLoadError(throwable: Throwable)

}

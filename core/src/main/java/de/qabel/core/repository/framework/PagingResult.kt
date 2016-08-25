package de.qabel.core.repository.framework

data class PagingResult<out T>(val availableRange : Int, val result : List<T>)

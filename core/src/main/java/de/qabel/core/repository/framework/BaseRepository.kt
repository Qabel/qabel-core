package de.qabel.core.repository.framework


interface BaseRepository<T : BaseEntity> {

    fun findById(id: Int): T

    fun persist(model: T)
    fun update(model: T)
    fun delete(id: Int)

}

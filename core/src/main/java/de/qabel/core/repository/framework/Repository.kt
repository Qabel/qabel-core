package de.qabel.core.repository.framework


interface Repository<T : BaseEntity> {

    fun findById(id: Int): T
    fun findByIds(ids : List<Int>): List<T>

    fun persist(model: T)
    fun update(model: T)
    fun delete(id: Int)

}

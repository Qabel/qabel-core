package de.qabel.core.repository.framework

import de.qabel.core.repository.framework.BaseEntity


interface BaseRepository<T : BaseEntity> {

    fun findById(id: Int): T

    fun persist(model: T, identityId: Int)
    fun update(model: T)
    fun delete(id: Int)

}

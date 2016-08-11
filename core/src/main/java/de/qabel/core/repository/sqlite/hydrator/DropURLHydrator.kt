package de.qabel.core.repository.sqlite.hydrator

import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL

import java.net.URISyntaxException
import java.sql.ResultSet
import java.sql.SQLException

class DropURLHydrator : AbstractHydrator<DropURL>() {
    override val fields: Array<String>
        get() = arrayOf("url")

    @Throws(SQLException::class)
    override fun hydrateOne(resultSet: ResultSet): DropURL {
        val url = resultSet.getString(1)
        try {
            return DropURL(url)
        } catch (e: URISyntaxException) {
            throw SQLException("failed to hydrate DropUrl from " + url, e)
        } catch (e: QblDropInvalidURL) {
            throw SQLException("failed to hydrate DropUrl from " + url, e)
        }

    }

    override fun recognize(instance: DropURL) {

    }
}

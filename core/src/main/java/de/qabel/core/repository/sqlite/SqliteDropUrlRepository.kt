package de.qabel.core.repository.sqlite

import de.qabel.core.StringUtils
import de.qabel.core.config.Contact
import de.qabel.core.config.Entity
import de.qabel.core.drop.DropURL
import de.qabel.core.exceptions.QblDropInvalidURL
import de.qabel.core.extensions.use
import de.qabel.core.repository.DropUrlRepository
import de.qabel.core.repository.exception.PersistenceException
import de.qabel.core.repository.sqlite.hydrator.DropURLHydrator
import de.qabel.core.util.DefaultHashMap
import java.net.URISyntaxException
import java.sql.SQLException
import java.util.*

class SqliteDropUrlRepository(database: ClientDatabase, hydrator: Hydrator<DropURL> = DropURLHydrator()) :
    AbstractSqliteRepository<DropURL>(database, hydrator, SqliteDropUrlRepository.TABLE_NAME), DropUrlRepository {

    constructor(database: ClientDatabase): this(database, DropURLHydrator())

    companion object {
        val TABLE_NAME = "drop_url"
    }

    @Deprecated("@todo hide contactId and use findAll(Contact contact) as soon as Identity extends Contact")
    @Throws(PersistenceException::class)
    fun findAll(contactId: Int): Collection<DropURL> {
        return findAll("contact_id=?", contactId)
    }

    override fun findAll(contact: Contact): Collection<DropURL> {
        return findAll(contact.id)
    }

    @Throws(SQLException::class)
    fun delete(contact: Contact) {
        val contactId = contact.id
        delete(contactId)
    }


    @Deprecated("@todo hide contactId and use delete(Contact contact) as soon as Identity extends Contact")
    @Throws(SQLException::class)
    fun delete(contactId: Int) {
        database.prepare(
            "DELETE FROM $TABLE_NAME WHERE contact_id = ?").use { dropDrops ->
            dropDrops.setInt(1, contactId)
            dropDrops.execute()
        }
    }

    @Throws(SQLException::class)
    fun store(contact: Contact) {
        val contactId = contact.id
        store(contact, contactId)
    }

    @Deprecated("@todo hide contactId and use store(Contact contact) as soon as Identity extends Contact")
    @Throws(SQLException::class)
    fun store(contact: Entity, contactId: Int) {
        database.prepare(
            "INSERT INTO $TABLE_NAME (contact_id, url) VALUES (?, ?)").use { dropStatement ->
            for (url in contact.dropUrls) {
                dropStatement.setInt(1, contactId)
                dropStatement.setString(2, url.toString())
                dropStatement.execute()
            }
        }
    }

    override fun findDropUrls(contactIds: List<Int>): Map<Int, List<DropURL>> {
        try {
            val contactDropUrlMap = DefaultHashMap<Int, MutableList<DropURL>>({ LinkedList<DropURL>() })
            database.prepare(
                "SELECT contact_id, url " +
                    "FROM " + TABLE_NAME + " urls " +
                    "WHERE contact_id IN (" + StringUtils.join(",", contactIds) + ")").use { statement ->
                statement.executeQuery().use({ results ->
                    while (results.next()) {
                        val id = results.getInt(1)
                        val dropUrl = DropURL(results.getString(2))
                        contactDropUrlMap.getOrDefault(id).add(dropUrl)
                    }
                })
            }
            return contactDropUrlMap
        } catch (e: SQLException) {
            throw PersistenceException("Error loading dropUrls for contacts", e)
        } catch (e: QblDropInvalidURL) {
            throw PersistenceException("Error loading dropUrls for contacts", e)
        } catch (e: URISyntaxException) {
            throw PersistenceException("Error loading dropUrls for contacts", e)
        }
    }
}

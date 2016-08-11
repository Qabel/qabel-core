package de.qabel.core.config

import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.junit.Before
import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class EntityMapTest {

    private var dropURLs: MutableCollection<DropURL>? = null
    private val qblECKeyPair = QblECKeyPair()
    private var identity: Identity? = null
    private var contacts: Contacts? = null
    private var contact: Contact? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        val uptoDate = booleanArrayOf(false)
        dropURLs = ArrayList<DropURL>()
        dropURLs!!.add(DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"))
        identity = Identity("Identity", dropURLs!!, qblECKeyPair)
        contacts = Contacts(identity!!)
        contact = Contact("Contact", identity!!.dropUrls, identity!!.ecPublicKey)
        contacts!!.put(contact!!)
    }

    @Test
    fun testContains() {
        val newContact = Contact("Contact", identity!!.dropUrls, identity!!.ecPublicKey)
        assertTrue(contacts!!.contains(newContact))
    }

    @Test
    @Throws(Exception::class)
    fun testPutNotifyObserver() {
        val uptoDate = booleanArrayOf(false)

        val observer = object : EntityObserver {
            override fun update() {
                uptoDate[0] = true
            }
        }

        contacts!!.addObserver(observer)
        contacts!!.put(contact!!)
        assertTrue(uptoDate[0])
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveNotifyObserver() {
        val uptoDate = booleanArrayOf(false)

        val observer = object : EntityObserver {
            override fun update() {
                uptoDate[0] = true
            }
        }

        contacts!!.addObserver(observer)
        contacts!!.removeObserver(observer)
        contacts!!.put(contact!!)
        assertFalse(uptoDate[0])
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveStringNotifyObserver() {

        val uptoDate = booleanArrayOf(false)

        val observer = object : EntityObserver {

            override fun update() {
                uptoDate[0] = true
            }
        }

        contacts!!.addObserver(observer)
        contacts!!.remove(contact!!.keyIdentifier)
        assertTrue(uptoDate[0])
    }

    @Test
    @Throws(Exception::class)
    fun testRemoveEntityNotifyObserver() {
        val uptoDate = booleanArrayOf(false)

        val observer = object : EntityObserver {

            override fun update() {
                uptoDate[0] = true
            }
        }

        contacts!!.addObserver(observer)
        contacts!!.remove(contact!!)
        assertTrue(uptoDate[0])
    }


}

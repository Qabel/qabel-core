package de.qabel.core.config

import de.qabel.core.config.Identity
import de.qabel.core.config.IdentityExportImport
import de.qabel.core.crypto.QblECKeyPair
import de.qabel.core.drop.DropURL
import org.junit.Test

import java.util.ArrayList

import org.junit.Assert.assertEquals

class IdentityExportImportTest {

    @Test
    @Throws(Exception::class)
    fun testExportImportIdentity() {

        val qblECKeyPair = QblECKeyPair()
        val dropURLs = ArrayList<DropURL>()
        dropURLs.add(DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"))
        dropURLs.add(DropURL("http://localhost:6000/0000000000000000000000000000000000000000000"))

        val identity = Identity("Identity", dropURLs, qblECKeyPair)
        identity.phone = "049111111"
        identity.email = "test@example.com"
        identity.prefixes.add("prefix1")
        identity.prefixes.add("prefix2")

        val exportedIdentity = IdentityExportImport.exportIdentity(identity)

        val importedIdentity = IdentityExportImport.parseIdentity(exportedIdentity)

        assertEquals(identity.id.toLong(), importedIdentity.id.toLong())
        assertEquals(identity.email, importedIdentity.email)
        assertEquals(identity.phone, importedIdentity.phone)
        assertEquals(identity.primaryKeyPair, importedIdentity.primaryKeyPair)
        assertEquals(identity.dropUrls, importedIdentity.dropUrls)
        assertEquals(identity.prefixes, importedIdentity.prefixes)
    }

    @Test
    @Throws(Exception::class)
    fun testExportImportIdentityMissingOptionals() {

        val qblECKeyPair = QblECKeyPair()
        val dropURLs = ArrayList<DropURL>()
        dropURLs.add(DropURL("http://localhost:6000/1234567890123456789012345678901234567891234"))
        dropURLs.add(DropURL("http://localhost:6000/0000000000000000000000000000000000000000000"))

        val identity = Identity("Identity", dropURLs, qblECKeyPair)

        val exportedIdentity = IdentityExportImport.exportIdentity(identity)

        val importedIdentity = IdentityExportImport.parseIdentity(exportedIdentity)

        assertEquals(identity.id.toLong(), importedIdentity.id.toLong())
        assertEquals(identity.email, importedIdentity.email)
        assertEquals(identity.phone, importedIdentity.phone)
        assertEquals(identity.primaryKeyPair, importedIdentity.primaryKeyPair)
        assertEquals(identity.dropUrls, importedIdentity.dropUrls)
        assertEquals(identity.prefixes, importedIdentity.prefixes)
    }
}

package de.qabel.core.extensions

import de.qabel.core.config.Contact
import de.qabel.core.ui.initials
import org.hamcrest.Matchers
import org.hamcrest.Matchers.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThat
import org.junit.Test


class ExtensionsTest() {

    data class TestData(var text: String) {}

    @Test
    fun testLetApply() {
        val text = "Banane"
        val textObj = TestData("Kirsche").letApply {
            it.text = text
        }
        assertEquals("Banane", textObj.text)
    }

    @Test
    fun testInitials() {
        val initials = Contact("Eine kleiner Test", emptyList(), null).initials()
        assertThat(initials.length, equalTo(2))
        val zwo = Contact("E", emptyList(), null).initials()
        assertThat(zwo.length, equalTo(1))
    }

}

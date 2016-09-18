package de.qabel.core.extensions

import org.junit.Assert.assertEquals
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

}

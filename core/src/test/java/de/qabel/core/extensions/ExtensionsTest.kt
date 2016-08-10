package de.qabel.core.extensions

import org.junit.Assert.assertEquals


class ExtensionsTest() {

    data class Test(var text: String) {}

    @org.junit.Test
    fun testLetApply() {
        val text = "Banane"
        val textObj = Test("Kirsche").letApply {
            it.text = text
        }
        assertEquals("Banane", textObj.text)
    }
}

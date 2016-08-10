package de.qabel.core.drop

import de.qabel.core.drop.DropIdGenerator
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.rules.ExpectedException

import org.junit.Assert.assertEquals

class DropIdGenerationTest {
    @get:Rule
    var exception = ExpectedException.none()

    @Test
    fun testAdjustable() {
        val generator = AdjustableDropIdGenerator()
        val id = generator.generateDropId()
        assertEquals(DropIdGenerator.DROP_ID_LENGTH.toLong(), id.length.toLong())
    }

    @Test
    fun testAdjustableTooFewBits() {
        exception.expect(IllegalArgumentException::class.java)
        val generator = AdjustableDropIdGenerator(0)
        val id = generator.generateDropId()
        assertEquals(DropIdGenerator.DROP_ID_LENGTH.toLong(), id.length.toLong())
    }

    @Test
    fun testAdjustableTooManyBits() {
        exception.expect(IllegalArgumentException::class.java)
        val generator = AdjustableDropIdGenerator(
                DropIdGenerator.DROP_ID_LENGTH_BYTE * 8 + 1)
        val id = generator.generateDropId()
        assertEquals(DropIdGenerator.DROP_ID_LENGTH.toLong(), id.length.toLong())
    }

    @Test
    fun testAdjustableJustRightLow() {
        val generator = AdjustableDropIdGenerator(1)
        val id = generator.generateDropId()
        assertEquals(DropIdGenerator.DROP_ID_LENGTH.toLong(), id.length.toLong())
    }

    @Test
    fun testAdjustableJustRightHigh() {
        val generator = AdjustableDropIdGenerator(
                DropIdGenerator.DROP_ID_LENGTH_BYTE * 8)
        val id = generator.generateDropId()
        assertEquals(DropIdGenerator.DROP_ID_LENGTH.toLong(), id.length.toLong())
    }
}

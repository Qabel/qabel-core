package de.qabel.core.index

import org.junit.Test

class PhoneUtilTest {

    val invalidNumbers = listOf("xxx", "123", "x123", "+45$2", "1234567891011", "01999999999")
    val validNumbers = listOf(randomPhone(), "+49 511 96 94 98 400")

    @Test
    fun testValidation() {
        invalidNumbers.forEach {
            assert(!isValidPhoneNumber(it), { "validation failed for invalid $it" })
        }
        validNumbers.forEach {
            assert(isValidPhoneNumber(it), { "validation failed for valid $it" })
        }
    }

    @Test
    fun testFormat() {
        val randomPhone = randomPhone()
        assertValidAndFormatted(formatPhoneNumber(randomPhone))

        val phone = "+49 511 96 94 98 400"
        assertValidAndFormatted(formatPhoneNumber(phone))
    }

    private fun assertValidAndFormatted(phone: String) {
        assert(phone.length in (13..15))
        assert(!phone.contains(" "))
        assert(isValidPhoneNumber(phone))
    }

}

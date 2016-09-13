package de.qabel.core.index

import org.junit.Test
import org.hamcrest.Matchers.*
import org.junit.Assert.*

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
        assertValidAndFormatted(formatPhoneNumber(randomPhone), (10..15), 0)

        val phone = "+49 511 96 94 98 400"
        assertValidAndFormatted(formatPhoneNumber(phone), (10..15), 0)
    }

    @Test
    fun testFormatReadable() {
        val randomPhone = randomPhone()
        assertValidAndFormatted(formatPhoneNumberReadable(randomPhone), (10..17), 2)

        val phone = "+49 511 96 94 98 400"
        assertValidAndFormatted(formatPhoneNumberReadable(phone), (10..17), 2)
    }

    private fun assertValidAndFormatted(phone: String, range: IntRange, spaces: Int) {
        assert(phone.length in range, { "Phone number not match valid size ${phone.length}" })
        assertThat(phone.count { it.isWhitespace() }, equalTo(spaces))
        assert(isValidPhoneNumber(phone), { "Phone number is not valid $phone" })
    }

}

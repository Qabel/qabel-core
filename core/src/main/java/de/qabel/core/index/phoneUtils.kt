package de.qabel.core.index

import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.PhoneNumberUtil.PhoneNumberFormat
import com.google.i18n.phonenumbers.Phonenumber
import java.util.*

private val phoneUtil: PhoneNumberUtil by lazy { PhoneNumberUtil.getInstance() }
private val localCountry: String by lazy { Locale.getDefault().country }

private fun parse(phone: String): Phonenumber.PhoneNumber =
    phoneUtil.parse(phone, localCountry)

private fun format(phone: String, format: PhoneNumberFormat) =
    phoneUtil.format(parse(phone), format)

/**
 * Parse [phone] and format to [PhoneNumberFormat.E164]
 * [1..3 country code] [12..14 national number (destination code + subscriber number] MaxLength : 15
 * example "+49157812345678"
 */
@Throws(NumberParseException::class)
fun formatPhoneNumber(phone: String): String = format(phone, PhoneNumberFormat.E164)

/**
 * Parse [phone] and format to [PhoneNumberFormat.INTERNATIONAL].
 * like [PhoneNumberFormat.E164] with whitespaces
 * example "+49 1578 12345678"
 */
@Throws(NumberParseException::class)
fun formatPhoneNumberReadable(phone: String): String = format(phone, PhoneNumberFormat.INTERNATIONAL)

/**
 * Checks [phone] is a valid phone number with [PhoneNumberUtil]
 */
fun isValidPhoneNumber(phone: String): Boolean =
    try {
        phoneUtil.isValidNumber(parse(phone))
    } catch (ex: NumberParseException) {
        false
    }

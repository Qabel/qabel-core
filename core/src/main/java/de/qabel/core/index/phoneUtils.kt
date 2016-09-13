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

/**
 * Parse [phone] and format to [PhoneNumberFormat.INTERNATIONAL]
 * [1..3 country code] [12 (+spaces) national number (destination code + subscriber number]
 */
@Throws(NumberParseException::class)
fun formatPhoneNumber(phone: String): String =
    phoneUtil.format(parse(phone), PhoneNumberFormat.INTERNATIONAL)

/**
 * Checks [phone] is a valid phone number with [PhoneNumberUtil]
 */
fun isValidPhoneNumber(phone: String): Boolean =
    try {
        phoneUtil.isValidNumber(parse(phone))
    } catch (ex: NumberParseException) {
        false
    }

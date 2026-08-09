package karika.distribucija.ba.salesrep.util

import android.util.Patterns

/**
 * Approximate equivalents of composeApp's ui/components validators (isEmailFormat/isPhoneFormat/
 * isPostalCodeValid) - the exact original regex wasn't available to port verbatim, so these use
 * standard Android/BiH-format checks instead.
 */
fun String.isEmailFormatValid(): Boolean = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isPhoneFormatValid(): Boolean = Regex("^[+]?[0-9 ()-]{6,15}$").matches(this)

fun String.isPostalCodeValidBa(): Boolean = Regex("^\\d{5}$").matches(this)

package karika.distribucija.ba.salesrep.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.math.round

/** Copied verbatim from composeApp's util/Extensions.kt - pure math, no platform coupling. */
fun karikaPriceFormat(value: Double): String {
    val scaled = round(value * 100) / 100
    val parts = scaled.toString().split('.')

    val integerPart = parts[0]
    val decimalPart = when (parts.getOrNull(1)?.length) {
        0 -> "00"
        1 -> parts[1] + "0"
        2 -> parts[1]
        else -> parts[1]?.take(2) ?: "00"
    }
    return "$integerPart,$decimalPart"
}

private fun Long.toDateTime(): String {
    val localDate = Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC)
    val dateFormat = LocalDateTime.Format {
        day(); char('.'); monthNumber(); char('.'); year(); char('.'); char(' '); hour(); char(':'); minute()
    }
    return localDate.format(dateFormat)
}

/**
 * Copied verbatim from composeApp's ui/view/distributer/orders/OrderFilter.kt - the backend
 * sends "yyyy-MM-dd HH:mm:ss" in UTC; this renders it in Sarajevo local time as "dd.MM.yyyy. HH:mm".
 */
fun String.toDateTime(): String {
    val isoString = replace(" ", "T")
    val localDateTime = LocalDateTime.parse(isoString)
    val instant = localDateTime.toInstant(TimeZone.UTC)
        .toLocalDateTime(TimeZone.of("Europe/Sarajevo"))
        .toInstant(TimeZone.UTC)
    return instant.toEpochMilliseconds().toDateTime()
}

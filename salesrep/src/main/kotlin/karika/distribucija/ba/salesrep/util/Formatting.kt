package karika.distribucija.ba.salesrep.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
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

private val apiDateFormat = LocalDate.Format {
    year(); char('-'); monthNumber(); char('-'); day()
}
private val displayDateFormat = LocalDate.Format {
    day(); char('.'); monthNumber(); char('.'); year(); char('.')
}

/** Analytics filters send `dateFrom`/`dateTo` as plain `Y-m-d` (Magento's expected format). */
fun Long.toApiDate(): String =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date.format(apiDateFormat)

/** Filter-screen display format, e.g. "01.09.2026." */
fun Long.toDisplayDate(): String =
    Instant.fromEpochMilliseconds(this).toLocalDateTime(TimeZone.UTC).date.format(displayDateFormat)

/** For analytics payloads like `last_order_at` ("yyyy-MM-dd HH:mm:ss") shown as a plain date. */
fun String.toDisplayDate(): String =
    LocalDateTime.parse(replace(" ", "T")).date.format(displayDateFormat)

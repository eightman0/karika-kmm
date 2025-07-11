package karika.distribucija.ba.ui.components

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

// 17.03.1991
fun String.toDateString(): String {
    val instant = replace(" ", "T").let { Instant.parse("${it}Z") }
    val localDateTime = instant.toLocalDateTime(TimeZone.UTC)
    val day = localDateTime.date.dayOfMonth.toString().padStart(2, '0')
    val month = localDateTime.date.monthNumber.toString().padStart(2, '0')
    val year = localDateTime.date.year
    return "$day.$month.$year"
}
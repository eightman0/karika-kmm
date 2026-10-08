package karika.distribucija.ba.util

import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** The app shows every date and time in Bosnia's time zone, whatever the device is set to. */
val SARAJEVO: TimeZone = TimeZone.of("Europe/Sarajevo")

/**
 * The backend's timestamps are UTC, as `yyyy-MM-dd HH:mm:ss` (or ISO-8601). This is the same
 * moment in Sarajevo, or null when the text is not such a timestamp.
 */
@OptIn(ExperimentalTime::class)
fun String.serverTimeInSarajevo(): LocalDateTime? = runCatching {
    val text = trim()
    val hasZone = text.endsWith("Z") || Regex("""T.*[+-]\d{2}:?\d{2}$""").containsMatchIn(text)
    if (hasZone) {
        Instant.parse(text).toLocalDateTime(SARAJEVO)
    } else {
        LocalDateTime.parse(text.replace(' ', 'T')).toInstant(TimeZone.UTC).toLocalDateTime(SARAJEVO)
    }
}.getOrNull()

/**
 * A backend timestamp moved from UTC to Sarajevo, written the way the backend writes it
 * (`yyyy-MM-dd HH:mm:ss`), so that the code that formats it for the screen stays as it is.
 * Text that is not a timestamp comes back unchanged.
 */
fun String.inSarajevo(): String = serverTimeInSarajevo()?.format(SERVER_FORMAT) ?: this

private val SERVER_FORMAT = LocalDateTime.Format {
    year()
    char('-')
    monthNumber()
    char('-')
    day()
    char(' ')
    hour()
    char(':')
    minute()
    char(':')
    second()
}

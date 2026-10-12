package karika.distribucija.ba.salesrep.ui.messages

/** Initials of a name for the message avatars, at most two letters - mirrors composeApp's
 * KarikaUi.kt `String?.initials()` ("Mesna industrija Vimes" -> "MI"). */
internal fun String?.messageInitials(): String {
    val words = orEmpty()
        .replace(Regex("[„“\"'.,]"), " ")
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

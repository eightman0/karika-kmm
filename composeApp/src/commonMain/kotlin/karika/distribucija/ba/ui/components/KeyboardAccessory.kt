package karika.distribucija.ba.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.PlatformImeOptions
import karika.distribucija.ba.ui.common.textFieldImeOptions

/**
 * Keyboard options with the iOS bar above the keyboard (inputAccessoryView) and its "Zatvori"
 * button, which clears focus and hides the keyboard. Remembered, so the text field keeps the
 * same options across recompositions (a new instance would restart the input session).
 * On Android there is no such bar and this is null.
 */
@Composable
fun rememberKeyboardCloseOptions(): PlatformImeOptions? {
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    return remember(focusManager, keyboard) {
        textFieldImeOptions(
            onDone = {
                focusManager.clearFocus()
                keyboard?.hide()
            },
            onPrevious = { focusManager.moveFocus(FocusDirection.Previous) },
            onNext = { focusManager.moveFocus(FocusDirection.Next) },
            useAccessoryView = true
        )
    }
}

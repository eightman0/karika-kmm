package karika.distribucija.ba.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import karika.distribucija.ba.ui.common.CommonViewModel


@Composable
fun <T> MutableState<T>.asState() = remember { this }

fun Modifier.onClick(callback: () -> Unit): Modifier {
    return this.clickable(
        interactionSource = null,
        indication = null
    ) {
        callback.invoke()
    }
}

@Composable
fun <T : CommonViewModel> T.asState(): T = remember { this }
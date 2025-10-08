package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.common.CommonComponent


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

fun Modifier.bgWhite() = this.background(color = KarikaColors.White)

@Composable
fun <T : CommonComponent> T.asState(): T = remember { this }

fun MutableState<Boolean>.negate() {
    this.value = !this.value
}

fun String.isEmailFormat(): Boolean {
    val emailRegex = Regex("^\\S+@\\S+\\.\\S+\$")
    return emailRegex.matches(this)
}

@Composable
fun Modifier.hideKeyboard(onlyClick: Boolean = false): Modifier {
    val keyboard = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }
    if (!onlyClick) {
        this.nestedScroll(nestedScrollConnection)
    }
    return this
        .clickable(
            interactionSource = null,
            indication = null
        ) {
            keyboard?.hide()
        }
}

@Composable
fun Modifier.roundedWithBorder(
    color: Color = KarikaColors.White,
    borderColor: Color = KarikaColors.Placeholder,
    shape: Dp = 100.dp
): Modifier {
    return this
        .background(
            color = color,
            shape = RoundedCornerShape(shape)
        )
        .border(
            width = 1.dp,
            color = borderColor,
            shape = RoundedCornerShape(shape)
        )

}

@Composable
fun Modifier.rounded(
    color: Color = KarikaColors.White,
    shape: Dp = 4.dp
): Modifier {
    return this
        .background(
            color = color,
            shape = RoundedCornerShape(shape)
        )
}

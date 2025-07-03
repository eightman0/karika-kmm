package karika.distribucija.ba.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun YSpacer16() {
    Spacer(
        modifier = Modifier
            .height(16.dp)
    )
}

@Composable
fun YSpacer8() {
    Spacer(
        modifier = Modifier
            .height(8.dp)
    )
}

@Composable
fun YSpacer32() {
    Spacer(
        modifier = Modifier
            .height(32.dp)
    )
}

@Composable
fun YSpacer(space: Int) {
    Spacer(
        modifier = Modifier
            .height(space.dp)
    )
}

@Composable
fun XSpacer16() {
    Spacer(
        modifier = Modifier
            .width(16.dp)
    )
}

@Composable
fun XSpacer8() {
    Spacer(
        modifier = Modifier
            .width(8.dp)
    )
}

@Composable
fun XSpacer32() {
    Spacer(
        modifier = Modifier
            .width(16.dp)
    )
}

@Composable
fun XSpacer(size: Int) {
    Spacer(
        modifier = Modifier
            .width(size.dp)
    )
}
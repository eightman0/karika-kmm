package karika.distribucija.ba.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import karika.distribucija.ba.ui.components.KarikaColors

@Composable
expect fun HtmlTextWithStyles(
    modifier: Modifier = Modifier,
    html: String,
    background: Color = KarikaColors.White,
    textColor: Color = KarikaColors.White
)
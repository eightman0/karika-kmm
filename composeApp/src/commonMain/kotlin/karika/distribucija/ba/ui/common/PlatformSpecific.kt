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

expect fun openPdf(url: String)

expect fun getEnvPrefix(): String

expect fun getEnvJwt(): String

expect fun isKiosk(): Boolean

expect fun appVersion(): String

expect fun appVersionName(): String

expect fun openPhoneCall(phoneNumber: String, error: (String) -> Unit = {})

package karika.distribucija.ba.ui.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PlatformImeOptions
import androidx.compose.ui.unit.sp
import com.mohamedrejeb.richeditor.annotation.ExperimentalRichTextApi
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material.RichText
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.karikaFonts

@OptIn(ExperimentalRichTextApi::class)
@Composable
fun HtmlTextWithStyles(
    modifier: Modifier = Modifier,
    html: String,
    textColor: Color = KarikaColors.White
) {
    val state = rememberRichTextState()
    LaunchedEffect(html) {
        state.setHtml(html)
    }

    RichText(
        state = state,
        modifier = modifier,
        color = textColor,
        fontFamily = karikaFonts(),
        fontSize = 14.sp,
        fontWeight = FontWeight.W400
    )
}

expect fun openPdf(url: String)

expect fun getEnvPrefix(): String

expect fun getEnvJwt(): String

expect fun isKiosk(): Boolean

expect fun appVersion(): Int

expect fun userAgent(): String

expect fun appVersionName(): String

expect fun isAndroid(): Boolean

expect fun appUrl(): String

expect fun openPhoneCall(phoneNumber: String, error: (String) -> Unit = {})
expect fun openEmail(emailAddress: String, error: (String) -> Unit = {})
expect fun textFieldImeOptions(
    onDone: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    useAccessoryView: Boolean = false
): PlatformImeOptions?

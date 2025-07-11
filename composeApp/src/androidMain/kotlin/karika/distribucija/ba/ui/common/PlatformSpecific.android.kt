package karika.distribucija.ba.ui.common

import android.content.res.ColorStateList
import android.text.Html
import android.util.TypedValue
import androidx.appcompat.widget.AppCompatTextView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.parseAsHtml

@Composable
actual fun HtmlTextWithStyles(
    modifier: Modifier,
    html: String,
    background: Color,
    textColor: Color,
) {
    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            val unescaped = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString()

            AppCompatTextView(ctx).apply {
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                text = unescaped.parseAsHtml()
                setTextColor(textColor.toArgb())
            }
        }
    )
}
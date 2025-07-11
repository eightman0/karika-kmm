package karika.distribucija.ba.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import karika.distribucija.ba.ui.components.KarikaColors
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import platform.CoreGraphics.CGRectZero
import platform.CoreGraphics.CGSizeMake
import platform.Foundation.NSAttributedString
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Foundation.NSUTF8StringEncoding
import platform.Foundation.create
import platform.Foundation.dataUsingEncoding
import platform.UIKit.NSCharacterEncodingDocumentAttribute
import platform.UIKit.NSDocumentTypeDocumentAttribute
import platform.UIKit.NSHTMLTextDocumentType
import platform.UIKit.NSStringDrawingUsesLineFragmentOrigin
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIScreen
import platform.UIKit.UITextView
import platform.UIKit.boundingRectWithSize
import platform.UIKit.create

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun HtmlTextWithStyles(
    modifier: Modifier,
    html: String,
    background: Color,
    textColor: Color
) {
    val coroutineScope = rememberCoroutineScope()
    var height by remember { mutableStateOf(100.dp) }

    key(html) {
        UIKitView(
            factory = {
                val textView = UITextView(CGRectZero.readValue()).apply {
                    backgroundColor = UIColor(
                        red = background.red.toDouble(),
                        green = background.green.toDouble(),
                        blue = background.blue.toDouble(),
                        alpha = background.alpha.toDouble()
                    )
                    setTextColor(
                        UIColor(
                            red = textColor.red.toDouble(),
                            green = textColor.green.toDouble(),
                            blue = textColor.blue.toDouble(),
                            alpha = textColor.alpha.toDouble()
                        )
                    )
                    setEditable(false)
                    setSelectable(false)
                    setScrollEnabled(false)
                    opaque = false
                }

                val data: NSData? = NSString.create(string = HtmlEntityDecoder.decode(html))
                    .dataUsingEncoding(encoding = NSUTF8StringEncoding)
                if (data != null) {
                    coroutineScope.launch {
                        val attributedString = withContext(Dispatchers.IO) {
                            val options: Map<Any?, *> = mapOf(
                                String to NSHTMLTextDocumentType,
                                NSDocumentTypeDocumentAttribute to NSHTMLTextDocumentType,
                                NSCharacterEncodingDocumentAttribute to NSUTF8StringEncoding
                            )
                            NSAttributedString.create(
                                data = data,
                                options = options,
                                documentAttributes = null,
                                error = null
                            )
                        } ?: NSAttributedString()

                        textView.attributedText = attributedString
                        textView.font = UIFont.systemFontOfSize(fontSize = 16.0)

                        val maxWidth = UIScreen.mainScreen.bounds.useContents { size.width } - 32

                        val boundingSize = attributedString.boundingRectWithSize(
                            CGSizeMake(maxWidth, Double.MAX_VALUE),
                            options = NSStringDrawingUsesLineFragmentOrigin,
                            context = null
                        )

                        val h = boundingSize.useContents { size.height }
                        height = h.dp + 50.dp
                    }
                } else {
                    textView.text = html
                }

                textView
            },
            modifier = modifier
                .fillMaxWidth()
                .height(height)
                .background(color = KarikaColors.White),
            update = { textView ->
                textView.backgroundColor = UIColor(
                    red = background.red.toDouble(),
                    green = background.green.toDouble(),
                    blue = background.blue.toDouble(),
                    alpha = background.alpha.toDouble()
                )
            },
            onReset = { textView ->
                textView.backgroundColor = UIColor(
                    red = background.red.toDouble(),
                    green = background.green.toDouble(),
                    blue = background.blue.toDouble(),
                    alpha = background.alpha.toDouble()
                )
            },
            onRelease = { textView ->
                textView.backgroundColor = UIColor(
                    red = background.red.toDouble(),
                    green = background.green.toDouble(),
                    blue = background.blue.toDouble(),
                    alpha = background.alpha.toDouble()
                )
            }
        )
    }
}

object HtmlEntityDecoder {

    private val entityMap = mapOf(
        "&lt;" to "<",
        "&gt;" to ">",
        "&amp;" to "&",
        "&quot;" to "\"",
        "&#39;" to "'",
        "&nbsp;" to " ",
    )

    fun decode(input: String): String {
        var result = input
        entityMap.forEach { (entity, char) ->
            result = result.replace(entity, char)
        }
        return result
    }
}
package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_close
import org.jetbrains.compose.resources.vectorResource

/**
 * Label and white bordered field of the order screens. The placeholder is drawn inside the text
 * field, so tests can find a field by it. The filters ([allowedChars], [maxLength],
 * [leadingZero]) work as in the old KarikaTextField1/4.
 */
@Composable
internal fun OrderField(
    value: MutableState<String>,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    required: Boolean = false,
    enabled: Boolean = true,
    error: String = "",
    trailingText: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    allowedChars: List<String> = emptyList(),
    maxLength: Int = Int.MAX_VALUE,
    leadingZero: Boolean = true,
    singleLine: Boolean = true,
    minHeight: Dp = 48.dp,
    background: Color = KarikaColors.White,
    shape: RoundedCornerShape = RoundedCornerShape(12.dp),
    onValueChange: (String) -> Unit = {},
) {
    val keyboard = LocalSoftwareKeyboardController.current
    Column(modifier = modifier) {
        if (label.isNotEmpty()) {
            KFieldLabel(text = label, required = required, requiredColor = VendorAccent)
        }
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value.value,
            onValueChange = { new ->
                if (!leadingZero && new.startsWith("0") && new.length > 1) return@BasicTextField
                if (new.startsWith(" ")) return@BasicTextField
                if (allowedChars.isNotEmpty() && new.any { c -> !allowedChars.contains(c.toString()) }) {
                    return@BasicTextField
                }
                if (new.length > maxLength) return@BasicTextField
                value.value = new
                onValueChange(new)
            },
            enabled = enabled,
            singleLine = singleLine,
            textStyle = TextStyle(
                color = if (enabled) KarikaUiColors.Ink else KarikaUiColors.Subtle,
                fontSize = 15.sp,
                fontFamily = karikaFonts()
            ),
            cursorBrush = SolidColor(VendorAccent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .then(if (singleLine) Modifier.height(minHeight) else Modifier.heightIn(min = minHeight))
                        .clip(shape)
                        .background(if (enabled) background else KarikaUiColors.Field)
                        .border(
                            1.dp,
                            when {
                                error.isNotEmpty() -> KarikaUiColors.Red
                                background == KarikaColors.White -> KarikaUiColors.Border
                                else -> Color.Transparent
                            },
                            shape
                        )
                        .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
                    verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
                ) {
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.value.isEmpty() && placeholder.isNotEmpty()) {
                            KarikaText(
                                text = placeholder,
                                color = KarikaUiColors.Subtle,
                                textSize = 15.sp,
                                maxLines = if (singleLine) 1 else Int.MAX_VALUE
                            )
                        }
                        innerTextField()
                    }
                    if (trailingText != null) {
                        Spacer(Modifier.width(8.dp))
                        KarikaText(text = trailingText, color = KarikaUiColors.Muted, textSize = 14.sp)
                    }
                }
            }
        )
        if (error.isNotEmpty()) {
            KarikaText(
                modifier = Modifier.padding(top = 6.dp),
                text = error,
                color = KarikaUiColors.Red,
                textSize = 12.sp
            )
        }
    }
}

/** Centered white dialog of the order screen: title, close button and [content]. */
@Composable
internal fun OrderModal(
    title: String,
    onDismiss: () -> Unit,
    titleColor: Color = KarikaUiColors.Ink,
    content: @Composable ColumnScope.() -> Unit,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 480.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(KarikaColors.White)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = title,
                    color = titleColor,
                    textSize = 19.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.W700
                )
                Spacer(Modifier.width(12.dp))
                KCircleButton(
                    icon = vectorResource(Res.drawable.ic_k_close),
                    size = 36.dp,
                    iconSize = 18.dp,
                    onClick = onDismiss
                )
            }
            Spacer(Modifier.height(16.dp))
            content()
        }
    }
}

/** "Odustani" and the primary action side by side at the bottom of a modal or sheet. */
@Composable
internal fun OrderModalButtons(
    primaryText: String,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    secondaryText: String = "Odustani",
    primaryColor: Color = VendorAccent,
    primaryEnabled: Boolean = true,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KSecondaryButton(
            modifier = Modifier.weight(1f),
            text = secondaryText,
            height = 48.dp,
            onClick = onSecondary
        )
        KPrimaryButton(
            modifier = Modifier.weight(1f),
            text = primaryText,
            background = primaryColor,
            enabled = primaryEnabled,
            height = 48.dp,
            onClick = onPrimary
        )
    }
}

/** Round radio mark in the supplier's blue. */
@Composable
internal fun OrderRadioMark(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) VendorAccent else KarikaUiColors.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Box(Modifier.size(10.dp).clip(CircleShape).background(VendorAccent))
        }
    }
}

/** Option row with a radio mark, as in the approve modal. */
@Composable
internal fun OrderOptionRow(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) VendorAccent.copy(alpha = 0.08f) else KarikaColors.White)
            .border(1.dp, if (selected) VendorAccent else KarikaUiColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrderRadioMark(selected = selected)
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Ink,
            textSize = 14.5.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500
        )
    }
}

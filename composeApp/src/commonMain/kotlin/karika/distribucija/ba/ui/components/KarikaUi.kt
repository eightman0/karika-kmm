package karika.distribucija.ba.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.Dialog
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_left
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_minus
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_search
import karikav2.composeapp.generated.resources.img_karika_logo
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

/**
 * Colors of the customer redesign ("stil 3a"): white headers with rounded bottom corners
 * on a light gray page, pink accents and navy for secondary emphasis.
 */
object KarikaUiColors {
    val Pink = Color(0xFFE8368F)
    /** Suppliers' blue (KarikaColors.Blue), used instead of pink on supplier screens. */
    val Blue = Color(0xFF3575E2)
    val BlueSoft = Color(0xFFE7EFFC)
    val PinkSoft = Color(0xFFFDE7F1)
    val Ink = Color(0xFF1A1F36)
    val Navy = Color(0xFF1C2038)
    val Muted = Color(0xFF6B7280)
    val Subtle = Color(0xFF9CA3AF)
    val Page = Color(0xFFF6F7F9)
    val Field = Color(0xFFF1F3F6)
    val Line = Color(0xFFECEFF2)
    val Border = Color(0xFFDFE3E8)
    val Green = Color(0xFF15803D)
    val GreenSoft = Color(0xFFDCFCE7)
    val GreenDot = Color(0xFF22C55E)
    val Red = Color(0xFFDC2626)
    val RedSoft = Color(0xFFFEF2F2)
    val Amber = Color(0xFFB45309)
    val AmberSoft = Color(0xFFFEF3C7)
}

val KarikaHeaderShape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp)
val KarikaCardShape = RoundedCornerShape(16.dp)

@Composable
fun KIcon(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tint: Color = KarikaUiColors.Ink,
    size: Dp = 22.dp,
) {
    Icon(
        modifier = modifier.size(size),
        imageVector = icon,
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun KLogo(modifier: Modifier = Modifier, width: Dp = 52.dp) {
    Image(
        modifier = modifier.width(width),
        painter = painterResource(Res.drawable.img_karika_logo),
        contentDescription = "Karika",
        contentScale = ContentScale.FillWidth
    )
}

/** Round light-gray icon button, used for back and the notifications bell. */
@Composable
fun KCircleButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = KarikaUiColors.Field,
    tint: Color = KarikaUiColors.Ink,
    size: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    showDot: Boolean = false,
    onClick: () -> Unit,
) {
    Box(modifier = modifier.size(size)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(background)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = icon, tint = tint, size = iconSize)
        }
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-9).dp, y = 9.dp)
                    .size(9.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(KarikaUiColors.Pink)
            )
        }
    }
}

@Composable
fun KBackButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    KCircleButton(
        modifier = modifier,
        icon = vectorResource(Res.drawable.ic_k_chevron_left),
        onClick = onClick
    )
}

/** White page header with rounded bottom corners. */
@Composable
fun KHeader(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .padding(contentPadding),
        content = content
    )
}

/** Header with a back button, a title (and optional overline) and trailing actions. */
@Composable
fun KBackHeader(
    title: String,
    modifier: Modifier = Modifier,
    overline: String? = null,
    onBack: () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
    below: @Composable ColumnScope.() -> Unit = {},
) {
    KHeader(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KBackButton(onClick = onBack)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!overline.isNullOrEmpty()) {
                    KarikaText(
                        text = overline,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
                KarikaText(
                    text = title,
                    color = KarikaUiColors.Ink,
                    textSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
            actions()
        }
        below()
    }
}

/** Header with a large title (e.g. "Dobavljači", "Meni") and a trailing slot. */
@Composable
fun KTitleHeader(
    title: String,
    modifier: Modifier = Modifier,
    trailing: @Composable RowScope.() -> Unit = {},
    below: @Composable ColumnScope.() -> Unit = {},
) {
    KHeader(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = title,
                color = KarikaUiColors.Ink,
                textSize = 26.sp,
                lineHeight = 32.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
            trailing()
        }
        below()
    }
}

/** Gray, borderless search field. With [onClick] it is a read-only entry point to search. */
@Composable
fun KSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onSearch: () -> Unit = {},
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaUiColors.Field)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_search), tint = KarikaUiColors.Muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (onClick != null) {
                KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
            } else {
                if (value.isEmpty()) {
                    KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
                }
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = TextStyle(
                        color = KarikaUiColors.Ink,
                        fontSize = 15.sp,
                        fontFamily = karikaFonts()
                    ),
                    cursorBrush = SolidColor(KarikaUiColors.Pink),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }
        }
    }
}

/** Dark square button next to the search field. */
@Composable
fun KSquareIconButton(
    icon: ImageVector,
    modifier: Modifier = Modifier,
    background: Color = KarikaUiColors.Ink,
    tint: Color = KarikaColors.White,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KIcon(icon = icon, tint = tint, size = 20.dp)
    }
}

@Composable
fun KCard(
    modifier: Modifier = Modifier,
    shape: Shape = KarikaCardShape,
    background: Color = KarikaColors.White,
    border: BorderStroke? = BorderStroke(1.dp, KarikaUiColors.Line),
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .clip(shape)
            .background(background)
            .then(if (border != null) Modifier.border(border, shape) else Modifier)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        content = content
    )
}

@Composable
fun KDivider(modifier: Modifier = Modifier) {
    HorizontalDivider(modifier = modifier, thickness = 1.dp, color = KarikaUiColors.Line)
}

/** Section title with an optional pink link on the right ("Svi", "Vidi sve", "Uredi"). */
@Composable
fun KSectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    actionText: String? = null,
    onAction: () -> Unit = {},
    titleExtra: @Composable RowScope.() -> Unit = {},
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        titleExtra()
        Spacer(Modifier.weight(1f))
        if (actionText != null) {
            KarikaText(
                modifier = Modifier.clickable(onClick = onAction).padding(4.dp),
                text = actionText,
                color = KarikaUiColors.Pink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

/** Rounded filter chip: navy when selected, white with a border otherwise. */
@Composable
fun KChip(
    text: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    trailingIcon: ImageVector? = null,
    selectedColor: Color = KarikaUiColors.Ink,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = modifier
            .height(34.dp)
            .clip(shape)
            .background(if (selected) selectedColor else KarikaColors.White)
            .then(if (selected) Modifier else Modifier.border(1.dp, KarikaUiColors.Border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            text = text,
            color = if (selected) KarikaColors.White else KarikaUiColors.Ink,
            textSize = 14.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500,
            maxLines = 1
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(6.dp))
            KIcon(
                icon = trailingIcon,
                tint = if (selected) KarikaColors.White else KarikaUiColors.Ink,
                size = 16.dp
            )
        }
    }
}

@Composable
fun KChipRow(
    modifier: Modifier = Modifier,
    contentPadding: Dp = 16.dp,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = contentPadding),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        content = content
    )
}

/** Initials of a name, at most two letters, e.g. "Mesna industrija Vimes" -> "MI". */
fun String?.initials(): String {
    val words = orEmpty()
        .replace(Regex("[„“\"'.,]"), " ")
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetterOrDigit() }
    return when {
        words.isEmpty() -> ""
        words.size == 1 -> words.first().take(2).uppercase()
        else -> (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

/** Rounded square or circle with initials on a soft pink background. */
@Composable
fun KInitials(
    name: String?,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    shape: Shape = RoundedCornerShape(8.dp),
    background: Color = KarikaUiColors.PinkSoft,
    color: Color = KarikaUiColors.Pink,
    textSize: TextUnit = 12.sp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(shape)
            .background(background),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = name.initials(),
            color = color,
            textSize = textSize,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}

@Composable
fun KPrimaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    background: Color = KarikaUiColors.Pink,
    height: Dp = 52.dp,
    icon: ImageVector? = null,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(14.dp))
            .background(if (enabled) background else background.copy(alpha = 0.4f))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            KIcon(icon = icon, tint = KarikaColors.White, size = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        KarikaText(
            text = text,
            color = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}

@Composable
fun KSecondaryButton(
    text: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    height: Dp = 52.dp,
    textColor: Color = KarikaUiColors.Ink,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(14.dp)
    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(KarikaColors.White)
            .border(1.dp, KarikaUiColors.Border, shape)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = text,
            color = if (enabled) textColor else KarikaUiColors.Subtle,
            textSize = 16.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}

/** Soft tinted button, e.g. "Pošalji poruku dobavljaču" or "Isprazni". */
@Composable
fun KTonalButton(
    text: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    background: Color = KarikaUiColors.PinkSoft,
    color: Color = KarikaUiColors.Pink,
    height: Dp = 44.dp,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(background)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            KIcon(icon = icon, tint = color, size = 18.dp)
            Spacer(Modifier.width(8.dp))
        }
        KarikaText(text = text, color = color, textSize = 15.sp, fontWeight = FontWeight.W700, maxLines = 1)
    }
}

/** White panel pinned to the bottom of a screen, holding the main action(s). */
@Composable
fun KBottomPanel(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            .background(KarikaColors.White)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        content = content
    )
}

/** Small rounded label, e.g. order status "Odobrena" or "Na zalihama". */
@Composable
fun KPill(
    text: String,
    modifier: Modifier = Modifier,
    background: Color = KarikaUiColors.GreenSoft,
    color: Color = KarikaUiColors.Green,
    dot: Color? = null,
    icon: ImageVector? = null,
    textSize: TextUnit = 12.sp,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(background)
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (dot != null) {
            Box(Modifier.size(6.dp).clip(CircleShape).background(dot))
            Spacer(Modifier.width(6.dp))
        }
        if (icon != null) {
            KIcon(icon = icon, tint = color, size = 13.dp)
            Spacer(Modifier.width(5.dp))
        }
        KarikaText(text = text, color = color, textSize = textSize, fontWeight = FontWeight.W600, maxLines = 1)
    }
}

/** "Label .... value" row used in cards (Moj nalog, Narudžba, Dostava). */
@Composable
fun KKeyValueRow(
    label: String,
    value: String?,
    modifier: Modifier = Modifier,
    valueColor: Color = KarikaUiColors.Ink,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(text = label, color = KarikaUiColors.Muted, textSize = 14.sp, maxLines = 1)
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            color = valueColor,
            textSize = 14.sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.End,
            maxLines = 2
        )
    }
}

/** Card with key/value rows separated by dividers. */
@Composable
fun KKeyValueCard(rows: List<Pair<String, String?>>, modifier: Modifier = Modifier) {
    KCard(modifier = modifier.fillMaxWidth()) {
        rows.forEachIndexed { index, (label, value) ->
            if (index > 0) KDivider()
            KKeyValueRow(label = label, value = value)
        }
    }
}

/** Menu row: icon tile, label and chevron. */
@Composable
fun KMenuRow(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    tileBackground: Color = KarikaUiColors.Field,
    iconTint: Color = KarikaUiColors.Ink,
    textColor: Color = KarikaUiColors.Ink,
    onClick: () -> Unit,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(tileBackground),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = icon, tint = iconTint, size = 19.dp)
        }
        Spacer(Modifier.width(14.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = textColor,
            textSize = 15.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
        KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaUiColors.Subtle, size = 18.dp)
    }
}

/** Pink quantity stepper "– 1 +". */
@Composable
fun KQuantityStepper(
    quantity: String,
    modifier: Modifier = Modifier,
    large: Boolean = false,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onQuantityClick: (() -> Unit)? = null,
) {
    val button = if (large) 42.dp else 30.dp
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(KarikaUiColors.PinkSoft)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(button)
                .clip(CircleShape)
                .background(KarikaColors.White)
                .clickable(onClick = onMinus),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_minus), tint = KarikaUiColors.Pink, size = if (large) 20.dp else 16.dp)
        }
        Box(
            modifier = Modifier
                .widthIn(min = if (large) 44.dp else 32.dp)
                .then(if (onQuantityClick != null) Modifier.clickable(onClick = onQuantityClick) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                text = quantity,
                color = KarikaUiColors.Ink,
                textSize = if (large) 17.sp else 15.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
        }
        Box(
            modifier = Modifier
                .size(button)
                .clip(CircleShape)
                .background(KarikaUiColors.Pink)
                .clickable(onClick = onPlus),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = KarikaColors.White, size = if (large) 20.dp else 16.dp)
        }
    }
}

/** Pink round "+" over a product photo. */
@Composable
fun KAddButton(modifier: Modifier = Modifier, size: Dp = 38.dp, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(KarikaUiColors.Pink)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = KarikaColors.White, size = 20.dp)
    }
}

/** Pink switch from the design ("Zapamti me", notification toggles). */
@Composable
fun KToggle(
    checked: Boolean,
    modifier: Modifier = Modifier,
    color: Color = KarikaUiColors.Pink,
    onCheckedChange: (Boolean) -> Unit,
) {
    Box(
        modifier = modifier
            .width(46.dp)
            .height(28.dp)
            .clip(RoundedCornerShape(50))
            .background(if (checked) color else KarikaUiColors.Border)
            .clickable { onCheckedChange(!checked) }
            .padding(3.dp),
        contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(KarikaColors.White)
        )
    }
}

/** Label above a form field, with a pink asterisk when the field is required. */
@Composable
fun KFieldLabel(
    text: String,
    modifier: Modifier = Modifier,
    required: Boolean = false,
    requiredColor: Color = KarikaUiColors.Pink,
) {
    Row(modifier = modifier.padding(bottom = 8.dp)) {
        KarikaText(text = text, color = KarikaUiColors.Muted, textSize = 13.sp, fontWeight = FontWeight.W600)
        if (required) {
            KarikaText(text = " *", color = requiredColor, textSize = 13.sp, fontWeight = FontWeight.W600)
        }
    }
}

/**
 * Form field from the design. [filled] gives the gray borderless look of the login form,
 * otherwise it is white with a border as in the edit forms.
 */
@Composable
fun KTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    filled: Boolean = false,
    enabled: Boolean = true,
    isError: Boolean = false,
    leadingIcon: ImageVector? = null,
    trailing: @Composable (RowScope.() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    password: Boolean = false,
    singleLine: Boolean = true,
    minHeight: Dp = 50.dp,
) {
    val shape = RoundedCornerShape(12.dp)
    val borderColor = when {
        isError -> KarikaUiColors.Red
        filled || !enabled -> Color.Transparent
        else -> KarikaUiColors.Border
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(minHeight) else Modifier.heightIn(min = minHeight))
            .clip(shape)
            .background(if (filled || !enabled) KarikaUiColors.Field else KarikaColors.White)
            .border(1.dp, borderColor, shape)
            .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        if (leadingIcon != null) {
            KIcon(icon = leadingIcon, tint = KarikaUiColors.Muted, size = 18.dp)
            Spacer(Modifier.width(10.dp))
        }
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty() && placeholder.isNotEmpty()) {
                KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp)
            }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = singleLine,
                textStyle = TextStyle(
                    color = if (enabled) KarikaUiColors.Ink else KarikaUiColors.Subtle,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(KarikaUiColors.Pink),
                visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction)
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        } else if (!enabled) {
            // A read-only field shows a lock, so it does not look like a broken input
            Spacer(Modifier.width(8.dp))
            KIcon(icon = vectorResource(Res.drawable.ic_k_lock), tint = KarikaUiColors.Subtle, size = 16.dp)
        }
    }
}

/** Gray placeholder with diagonal stripes, shown where an image is missing. */
@Composable
fun KImagePlaceholder(modifier: Modifier = Modifier, label: String? = null) {
    Box(
        modifier = modifier.background(KarikaUiColors.Field),
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            KarikaText(text = label, color = KarikaUiColors.Subtle, textSize = 11.sp)
        }
    }
}

/** Image from the network with the design's gray placeholder while it is missing. */
@Composable
fun KImage(
    url: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    placeholderLabel: String? = null,
) {
    if (url.isNullOrBlank()) {
        KImagePlaceholder(modifier = modifier, label = placeholderLabel)
    } else {
        Box(modifier = modifier.background(KarikaUiColors.Field)) {
            KarikaImage(
                modifier = Modifier.fillMaxSize(),
                model = url,
                contentScale = contentScale
            )
        }
    }
}

/** Empty state text centered in the remaining space. */
@Composable
fun KEmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
        KarikaText(text = text, color = KarikaUiColors.Muted, textSize = 15.sp, textAlign = TextAlign.Center)
    }
}

/** Bottom sheet style confirmation panel used on top of a dimmed screen. */
@Composable
fun KConfirmSheet(
    title: String,
    message: String,
    icon: ImageVector,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    iconBackground: Color = KarikaUiColors.RedSoft,
    iconTint: Color = KarikaUiColors.Red,
    confirmColor: Color = KarikaUiColors.Red,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Ink.copy(alpha = 0.45f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                .background(KarikaColors.White)
                .clickable(enabled = false) {}
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp)
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KarikaUiColors.Border)
            )
            Spacer(Modifier.height(20.dp))
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = icon, tint = iconTint, size = 24.dp)
            }
            Spacer(Modifier.height(16.dp))
            KarikaText(text = title, color = KarikaUiColors.Ink, textSize = 20.sp, lineHeight = 24.sp, fontWeight = FontWeight.W700)
            Spacer(Modifier.height(8.dp))
            KarikaText(text = message, color = KarikaUiColors.Muted, textSize = 14.sp, lineHeight = 20.sp)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KSecondaryButton(modifier = Modifier.weight(1f), text = dismissText, onClick = onDismiss)
                KPrimaryButton(modifier = Modifier.weight(1f), text = confirmText, background = confirmColor, onClick = onConfirm)
            }
        }
    }
}

/** Centered confirmation dialog (icon, title, message, two buttons), for destructive actions. */
@Composable
fun KConfirmDialog(
    title: String,
    message: String,
    icon: ImageVector,
    confirmText: String,
    dismissText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    iconBackground: Color = KarikaUiColors.RedSoft,
    iconTint: Color = KarikaUiColors.Red,
    confirmColor: Color = KarikaUiColors.Red,
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 24.dp)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(KarikaColors.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = icon, tint = iconTint, size = 24.dp)
            }
            Spacer(Modifier.height(16.dp))
            KarikaText(
                text = title,
                color = KarikaUiColors.Ink,
                textSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            KarikaText(
                text = message,
                color = KarikaUiColors.Muted,
                textSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(22.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KSecondaryButton(modifier = Modifier.weight(1f), text = dismissText, height = 48.dp, onClick = onDismiss)
                KPrimaryButton(
                    modifier = Modifier.weight(1f),
                    text = confirmText,
                    background = confirmColor,
                    height = 48.dp,
                    onClick = onConfirm
                )
            }
        }
    }
}

/** Large colored tile with a decorative circle, e.g. "Kupac"/"Dobavljač" and "Outlet"/"Akcije". */
@Composable
fun KFeatureTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    background: Color,
    modifier: Modifier = Modifier,
    height: Dp = 118.dp,
    showChevron: Boolean = false,
    onClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(18.dp))
            .background(background)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 22.dp, y = 22.dp)
                .size(92.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.14f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                KIcon(icon = icon, tint = KarikaColors.White, size = 24.dp)
                Spacer(Modifier.weight(1f))
                if (showChevron) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaColors.White, size = 18.dp)
                }
            }
            Column {
                KarikaText(text = title, color = KarikaColors.White, textSize = 18.sp, lineHeight = 22.sp, fontWeight = FontWeight.W700, maxLines = 1)
                Spacer(Modifier.height(2.dp))
                KarikaText(text = subtitle, color = KarikaColors.White.copy(alpha = 0.85f), textSize = 12.sp, lineHeight = 16.sp, maxLines = 1)
            }
        }
    }
}

/** Page background for the redesigned screens. */
@Composable
fun KPage(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(KarikaUiColors.Page), content = content)
}

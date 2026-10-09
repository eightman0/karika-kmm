package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KKeyValueRow
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_plus
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ShippingDetailsView(component: ShippingDetailsComponent) {
    Column(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize()
    ) {
        KBackHeader(
            title = "Informacije za dostavu",
            onBack = { component.mainBack() },
            actions = {
                KarikaText(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { component.mainBack() }
                        .padding(horizontal = 4.dp, vertical = 6.dp),
                    text = "Odustani",
                    color = KarikaUiColors.Pink,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600
                )
            }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            SectionTitle("Adresa dostave")
            AddressBox(component)
            SectionTitle("Napomena za dobavljača")
            ShippingField(
                modifier = Modifier.padding(horizontal = 16.dp),
                value = component.vendorNote.asState(),
                placeholder = "Napomena (opciono)",
                imeAction = ImeAction.Done,
                singleLine = false,
                minHeight = 88.dp
            )
            SectionTitle("Pregled")
            Cart(component)
        }
        PinnedFooter(component)
    }
}

@Composable
private fun SectionTitle(title: String) {
    KSectionTitle(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
        title = title
    )
}

@Composable
private fun Cart(component: ShippingDetailsComponent) {
    val cart = component.stateHolder.cartHandler.cart.collectAsState()
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        cart.value.items.entries.forEach {
            VendorItem(it)
        }
    }
}

@Composable
private fun VendorItem(entry: Map.Entry<Vendor, List<Pair<Product, Int>>>) {
    KCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(name = entry.key.name(), textSize = 11.sp)
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = entry.key.name(),
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
        }
        KDivider()
        KKeyValueRow(label = "VPC", value = entry.totalVPC())
        KDivider()
        KKeyValueRow(label = "Ukupno sa PDV", value = entry.total())
    }
}

@Composable
private fun AddressBox(component: ShippingDetailsComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    val newAddress = component.newAddress.asState()
    val addresses = component.addresses.collectAsState()
    val selectedAddress = component.selectedAddress.asState()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        addresses.value.forEach {
            SelectableCard(
                selected = selectedAddress.value == it.id?.toString(),
                onClick = {
                    selectedAddress.value = it.id?.toString() ?: ""
                    newAddress.value = false
                }
            ) {
                KarikaText(
                    text = profile.companyName(),
                    color = KarikaUiColors.Ink,
                    textSize = 14.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = it.address(),
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    lineHeight = 19.sp
                )
            }
        }

        if (newAddress.value) {
            SelectableCard(
                selected = true,
                onClick = {
                    newAddress.value = true
                    selectedAddress.value = ""
                }
            ) {
                KarikaText(
                    text = "Dodaj novu adresu",
                    color = KarikaUiColors.Ink,
                    textSize = 14.5.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W700
                )
            }
            NewAddressForm(component)
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .dashedBorder(color = KarikaUiColors.Border, radius = 14.dp)
                    .clickable {
                        newAddress.value = true
                        selectedAddress.value = ""
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(KarikaUiColors.PinkSoft),
                    contentAlignment = Alignment.Center
                ) {
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_plus),
                        tint = KarikaUiColors.Pink,
                        size = 14.dp
                    )
                }
                Spacer(Modifier.width(12.dp))
                KarikaText(
                    text = "Dodaj novu adresu",
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Composable
private fun NewAddressForm(component: ShippingDetailsComponent) {
    KCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                KFieldLabel(text = "Ime", required = true)
                ShippingField(
                    value = component.firstname.asState(),
                    placeholder = "Ime",
                    allowedChars = KarikaConstants.lettersSpace,
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Prezime", required = true)
                ShippingField(
                    value = component.lastname.asState(),
                    placeholder = "Prezime",
                    allowedChars = KarikaConstants.lettersSpace,
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Naziv pravnog lica", required = true)
                ShippingField(
                    value = component.companyName.asState(),
                    placeholder = "Naziv pravnog lica",
                    allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                    keyboardType = KeyboardType.Text,
                    enabled = false,
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Grad", required = true)
                ShippingField(
                    value = component.city.asState(),
                    placeholder = "Grad",
                    allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Adresa i broj ulice", required = true)
                ShippingField(
                    value = component.address.asState(),
                    placeholder = "Adresa i broj ulice",
                    allowedChars = KarikaConstants.numbersAndLettersSpace,
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Poštanski broj", required = true)
                ShippingField(
                    value = component.postal.asState(),
                    placeholder = "Poštanski broj",
                    allowedChars = KarikaConstants.numbers,
                    keyboardType = KeyboardType.Number,
                    maxLength = 5,
                    imeAction = ImeAction.Next
                )
            }
            Column {
                KFieldLabel(text = "Broj telefona", required = true)
                ShippingField(
                    value = component.telephone.asState(),
                    placeholder = "Broj telefona",
                    allowedChars = KarikaConstants.numbers,
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Done
                )
            }
        }
    }
}

/** Address option: pink border and a pink check when selected. */
@Composable
private fun SelectableCard(
    selected: Boolean,
    onClick: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(KarikaColors.White)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) KarikaUiColors.Pink else KarikaUiColors.Line,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) KarikaUiColors.Pink else KarikaColors.White)
                .border(
                    width = 1.5.dp,
                    color = if (selected) KarikaUiColors.Pink else KarikaUiColors.Border,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_check),
                    tint = KarikaColors.White,
                    size = 14.dp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), content = content)
    }
}

/**
 * Form field in the design's style. The placeholder sits inside the text field, so the field
 * can be found by it (as with the Material text field it replaces); the input is filtered as
 * KarikaTextField1 does.
 */
@Composable
private fun ShippingField(
    value: MutableState<String>,
    placeholder: String,
    modifier: Modifier = Modifier,
    allowedChars: List<String> = emptyList(),
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    maxLength: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    minHeight: Dp = 50.dp,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        modifier = modifier.fillMaxWidth(),
        value = value.value,
        onValueChange = {
            if (it.startsWith(" ")) {
                return@BasicTextField
            }
            if (allowedChars.isNotEmpty() && it.any { f -> !allowedChars.contains(f.toString()) }) {
                return@BasicTextField
            }
            if (it.length > maxLength) {
                return@BasicTextField
            }
            value.value = it
        },
        enabled = enabled,
        singleLine = singleLine,
        textStyle = TextStyle(
            color = if (enabled) KarikaUiColors.Ink else KarikaUiColors.Muted,
            fontSize = 15.sp,
            fontFamily = karikaFonts()
        ),
        cursorBrush = SolidColor(KarikaUiColors.Pink),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .then(if (singleLine) Modifier.height(minHeight) else Modifier.heightIn(min = minHeight))
                    .clip(shape)
                    .background(if (enabled) KarikaColors.White else KarikaUiColors.Field)
                    .border(1.dp, if (enabled) KarikaUiColors.Border else Color.Transparent, shape)
                    .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
                contentAlignment = if (singleLine) Alignment.CenterStart else Alignment.TopStart
            ) {
                if (value.value.isEmpty()) {
                    KarikaText(
                        text = placeholder,
                        color = KarikaUiColors.Subtle,
                        textSize = 15.sp,
                        lineHeight = 20.sp
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun PinnedFooter(component: ShippingDetailsComponent) {
    val newAddress = component.newAddress.asState()
    val cart by component.stateHolder.cartHandler.cart.collectAsState()
    KBottomPanel {
        KPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = if (newAddress.value) {
                "Spasi i nastavi dalje"
            } else {
                "Završi narudžbu · " + cart.items.grandTotal()
            },
            enabled = if (newAddress.value) component.validateNewAddress() else true
        ) {
            component.handleShippingAddress()
        }
    }
}

/** Dashed rounded outline, as around "Dodaj novu adresu". */
private fun Modifier.dashedBorder(color: Color, radius: Dp): Modifier = this.drawBehind {
    val strokeWidth = 1.5.dp.toPx()
    drawRoundRect(
        color = color,
        topLeft = androidx.compose.ui.geometry.Offset(strokeWidth / 2, strokeWidth / 2),
        size = androidx.compose.ui.geometry.Size(size.width - strokeWidth, size.height - strokeWidth),
        cornerRadius = CornerRadius(radius.toPx()),
        style = Stroke(
            width = strokeWidth,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(6.dp.toPx(), 4.dp.toPx()), 0f)
        )
    )
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.totalVPC(): String {
    return karikaPriceFormat(
        value.sumOf { it.first.vpc(it.second) }
    ) + " KM"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.total(): String {
    return karikaPriceFormat(
        value.sumOf { it.first.vpc(it.second) } * 1.17
    ) + " KM"
}

private fun Map<Vendor, List<Pair<Product, Int>>>.grandTotal(): String {
    return karikaPriceFormat(
        values.flatten().sumOf { it.first.vpc(it.second) } * 1.17
    ) + " KM"
}

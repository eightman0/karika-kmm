package karika.distribucija.ba.ui.view.shop.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.textFieldImeOptions
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KConfirmDialog
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KTitleHeader
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rememberImeVisible
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_minus
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_trash
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

/** Test tags of a cart line's image and delete icon, for the end-to-end tests. */
fun cartProductTag(product: Product) = "cart_product_${product.sku}"
fun removeFromCartTag(product: Product) = "remove_from_cart_${product.sku}"

/** Test tags of a cart line's "–" and "+" buttons, for the end-to-end tests. */
fun cartQtyMinusTag(product: Product) = "cart_qty_minus_${product.sku}"
fun cartQtyPlusTag(product: Product) = "cart_qty_plus_${product.sku}"

/** Test tag of the cart's list of vendors, shown only while the cart has products. */
const val CART_LIST_TAG = "cart_list"

@Composable
fun CartView(component: CartComponent) {
    val items = component.stateHolder.cartHandler.cart.collectAsState()
    val clearCartModal = mutableStateOf(false).asState()
    val imeVisible = rememberImeVisible()
    val focusManager = LocalFocusManager.current
    val isEmpty = items.value.items.isEmpty()

    Column(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize()
    ) {
        KTitleHeader(
            title = "Korpa",
            trailing = {
                if (!isEmpty) {
                    KTonalButton(
                        text = "Isprazni",
                        icon = vectorResource(Res.drawable.ic_k_trash),
                        background = KarikaUiColors.RedSoft,
                        color = KarikaUiColors.Red,
                        height = 38.dp,
                        onClick = { clearCartModal.negate() }
                    )
                }
            }
        )
        if (isEmpty) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KEmptyState(text = "Nema artikala u korpi.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .testTag(CART_LIST_TAG)
                    .hideKeyboard()
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items.value.items.entries.toList()) {
                    CartItem(it, component)
                }
            }
            PinnedFooter(component)
            if (imeVisible) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    KarikaText(
                        modifier = Modifier
                            .clickable { focusManager.clearFocus() }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        text = "Zatvori",
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        color = KarikaUiColors.Pink
                    )
                }
            }
        }
    }

    if (clearCartModal.value) {
        KConfirmDialog(
            title = "Isprazniti korpu?",
            message = "Ova akcija će ukloniti sve artikle iz korpe.",
            icon = vectorResource(Res.drawable.ic_k_trash),
            confirmText = "Da, isprazni",
            dismissText = "Ne",
            onConfirm = {
                component.clearCart()
                clearCartModal.negate()
            },
            onDismiss = {
                clearCartModal.negate()
            }
        )
    }
}

@Composable
private fun PinnedFooter(component: CartComponent) {
    val cart by component.stateHolder.cartHandler.cart.collectAsState()

    KBottomPanel {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Ukupno sa PDV",
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = "VPC " + cart.items.calculateTotalVpc(),
                    color = KarikaUiColors.Subtle,
                    textSize = 11.sp
                )
            }
            KarikaText(
                text = cart.items.calculateTotal(),
                color = KarikaUiColors.Ink,
                textSize = 22.sp,
                lineHeight = 26.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
        }
        KPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Nastavi dalje",
            height = 50.dp
        ) {
            if (cart.items.orderValid()) {
                component.shippingDetails()
            } else {
                component.showMessage("Nije zadovoljena minimalna vrijednost narudžbe za dobavljača!")
            }
        }
    }
}

@Composable
private fun CartItem(
    item: Map.Entry<Vendor, List<Pair<Product, Int>>>,
    component: CommonComponent
) {
    val vendorProduct = Product(
        vendorName = item.key.name(),
        vendorId = item.key.entityId.toString()
    )
    val canOpenVendor = !component.isGuest()

    KCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (canOpenVendor) {
                        Modifier.clickable { component.showVendor(vendorProduct.toVendor()) }
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(name = vendorProduct.vendorName(), textSize = 11.sp)
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = vendorProduct.vendorName(),
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            if (canOpenVendor) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_chevron_right),
                    tint = KarikaUiColors.Subtle,
                    size = 16.dp
                )
            }
        }
        KDivider()
        MinOrderAmount(item)
        item.value.forEachIndexed { index, product ->
            if (index > 0) {
                KDivider(modifier = Modifier.padding(horizontal = 14.dp))
            }
            ProductItem(product, component)
        }
    }
}

@Composable
private fun MinOrderAmount(item: Map.Entry<Vendor, List<Pair<Product, Int>>>) {
    if (item.key.minOrderAmount() == null) {
        return
    }
    val reached = item.minAmountRestValue().toDouble() <= 0

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (reached) KarikaUiColors.GreenSoft.copy(alpha = 0.45f) else KarikaUiColors.Field)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KarikaText(
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaUiColors.Muted,
                        fontSize = 12.sp
                    )
                ) {
                    append("Minimum: ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaUiColors.Ink,
                        fontSize = 12.sp
                    )
                ) {
                    append("${item.key.minOrderAmount()}KM")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaUiColors.Subtle,
                        fontSize = 12.sp
                    )
                ) {
                    append(" • ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaUiColors.Muted,
                        fontSize = 12.sp
                    )
                ) {
                    append("Nedostaje: ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = if (reached) KarikaUiColors.Green else KarikaUiColors.Pink,
                        fontSize = 12.sp
                    )
                ) {
                    append("${item.minAmountRest()}KM")
                }
            },
            color = KarikaUiColors.Muted,
            textSize = 12.sp
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(6.dp)
                    .clip(CircleShape)
                    .background(color = KarikaUiColors.Border)
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(color = if (reached) KarikaUiColors.GreenDot else KarikaUiColors.Pink)
                            .weight(item.progress().first)
                    )
                    if (item.progress().second > 0) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .weight(item.progress().second)
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(if (reached) KarikaUiColors.GreenDot else KarikaUiColors.Border),
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_check),
                    tint = KarikaColors.White,
                    size = 12.dp
                )
            }
        }
    }
    KDivider()
}

@Composable
private fun ProductItem(item: Pair<Product, Int>, component: CommonComponent) {
    val product = item.first
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .testTag(cartProductTag(product))
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp))
                .onClick {
                    component.navigateToProduct(product)
                }
        ) {
            KImage(
                modifier = Modifier.fillMaxSize(),
                url = product.image()
            )
            DiscountBadge(product)
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = product.name(),
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 2
                )
                KIcon(
                    modifier = Modifier
                        .testTag(removeFromCartTag(product))
                        .onClick {
                            component.removeFromCart(product)
                        },
                    icon = vectorResource(Res.drawable.ic_k_trash),
                    tint = KarikaUiColors.Subtle,
                    size = 18.dp
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                KPill(
                    text = "Bonus " + product.bonusString(item.second),
                    icon = vectorResource(Res.drawable.ic_k_gift),
                    textSize = 11.sp
                )
                KPill(
                    text = "Min. ${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
                    background = KarikaUiColors.Field,
                    color = KarikaUiColors.Muted,
                    textSize = 11.sp
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    KarikaText(
                        text = product.vpcPdvString(item.second) + " KM",
                        color = KarikaUiColors.Ink,
                        textSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    KarikaText(
                        text = "VPC " + product.vpcString(item.second) + " KM",
                        color = KarikaUiColors.Muted,
                        textSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 1
                    )
                }
                CartQtyStepper(
                    product = product,
                    quantity = item.second,
                    component = component
                )
            }
        }
    }
}

/** Small pink "-x%" label over the photo of a product on special price. */
@Composable
private fun DiscountBadge(product: Product) {
    if (!product.hasSpecialPrice()) {
        return
    }
    KarikaText(
        modifier = Modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(KarikaUiColors.Pink)
            .padding(horizontal = 5.dp, vertical = 2.dp),
        text = "-" + product.calculatePercent() + "%",
        color = KarikaColors.White,
        textSize = 10.sp,
        fontWeight = FontWeight.W700,
        maxLines = 1
    )
}

/**
 * Pink "– n +" stepper of a cart line. Steps by the product's minimum quantity, never below it,
 * accepts a typed quantity and sends a change to the cart after a short pause.
 */
@Composable
private fun CartQtyStepper(product: Product, quantity: Int, component: CommonComponent) {
    val qty = remember(quantity) { mutableStateOf(quantity) }
    var pendingQty by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingQty) {
        pendingQty?.let { newQty ->
            delay(600)
            component.updateCart(product, newQty)
            pendingQty = null
        }
    }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(KarikaUiColors.PinkSoft)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .testTag(cartQtyMinusTag(product))
                .size(30.dp)
                .clip(CircleShape)
                .background(KarikaColors.White)
                .clickable {
                    if (qty.value == product.minQty()) {
                        return@clickable
                    }
                    qty.value -= product.minQty()
                    pendingQty = qty.value
                },
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_minus), tint = KarikaUiColors.Pink, size = 16.dp)
        }
        CartQtyField(
            value = qty,
            minValue = product.minQty(),
            onValueChange = {
                if (it == qty.value) {
                    return@CartQtyField
                }
                val entered = it ?: qty.value
                val min = product.minQty()
                val adjusted = if (entered <= min) {
                    min
                } else {
                    val remainder = entered % min
                    if (remainder == 0) {
                        entered
                    } else {
                        entered + (min - remainder)
                    }
                }
                qty.value = adjusted
                pendingQty = qty.value
            }
        )
        Box(
            modifier = Modifier
                .testTag(cartQtyPlusTag(product))
                .size(30.dp)
                .clip(CircleShape)
                .background(KarikaUiColors.Pink)
                .clickable {
                    qty.value += product.minQty()
                    pendingQty = qty.value
                },
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = KarikaColors.White, size = 16.dp)
        }
    }
}

/** Typed quantity between the stepper buttons, as KarikaIntTextField but sized for the stepper. */
@Composable
private fun CartQtyField(
    value: MutableState<Int>,
    onValueChange: (Int?) -> Unit,
    minValue: Int = 1
) {
    val focusManager = LocalFocusManager.current
    val imeVisible = rememberImeVisible()
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value.value.toString(),
                selection = TextRange(value.value.toString().length)
            )
        )
    }

    BasicTextField(
        value = textFieldValue,
        modifier = Modifier
            .width(IntrinsicSize.Min)
            .defaultMinSize(minWidth = 34.dp)
            .onFocusChanged {
                if (!it.isFocused) {
                    val currentValue = textFieldValue.text.toIntOrNull()
                    val finalValue = when {
                        currentValue == null || currentValue < minValue -> minValue
                        else -> currentValue
                    }
                    textFieldValue = TextFieldValue(
                        text = finalValue.toString(),
                        selection = TextRange(finalValue.toString().length)
                    )
                    onValueChange(finalValue)
                }
            },
        onValueChange = { newValue ->
            if (newValue.text.length > 7) {
                return@BasicTextField
            }
            val filtered = newValue.text.filter { it.isDigit() }
            val withoutLeadingZero = if (filtered.startsWith("0") && filtered.length > 1) {
                filtered.trimStart('0')
            } else if (filtered == "0") {
                ""
            } else {
                filtered
            }
            val parsedValue = withoutLeadingZero.toIntOrNull()
            textFieldValue = TextFieldValue(
                text = withoutLeadingZero,
                selection = TextRange(withoutLeadingZero.length)
            )
            if ((parsedValue ?: return@BasicTextField) % minValue == 0) {
                onValueChange(parsedValue)
            }
        },
        textStyle = TextStyle(
            color = KarikaUiColors.Ink,
            fontSize = 15.sp,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center,
            fontFamily = karikaFonts()
        ),
        cursorBrush = SolidColor(KarikaUiColors.Pink),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done,
            platformImeOptions = textFieldImeOptions(
                onDone = {
                    focusManager.clearFocus()
                },
                onNext = {
                    focusManager.moveFocus(FocusDirection.Next)
                },
                onPrevious = {
                    focusManager.moveFocus(FocusDirection.Previous)
                },
                useAccessoryView = false
            )
        ),
        decorationBox = { innerTextField ->
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.padding(horizontal = 4.dp)
            ) {
                innerTextField()
            }
        },
        singleLine = false,
        maxLines = 1,
        minLines = 1
    )

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            val currentValue = textFieldValue.text.toIntOrNull()
            val finalValue = when {
                currentValue == null || currentValue < minValue -> minValue
                else -> currentValue
            }
            textFieldValue = TextFieldValue(
                text = finalValue.toString(),
                selection = TextRange(finalValue.toString().length)
            )
            onValueChange(finalValue)
        }
    }

    LaunchedEffect(value.value) {
        val valueString = value.value.toString()
        if (valueString != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = valueString,
                selection = TextRange(valueString.length)
            )
        }
    }
}

private fun Map<Vendor, List<Pair<Product, Int>>>.calculateTotal(): String {
    val total = values
        .flatten()
        .sumOf { (product, quantity) -> product.currentPrice() * quantity }

    return karikaPriceFormat(total * 1.17) + " KM"
}

private fun Map<Vendor, List<Pair<Product, Int>>>.calculateTotalVpc(): String {
    val total = values
        .flatten()
        .sumOf { (product, quantity) -> product.vpc(quantity) }

    return karikaPriceFormat(total) + " KM"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRestValue(): String {
    return "${
        ((key.minOrderAmount()
            ?.toDoubleOrNull() ?: 0.0) - (value.sumOf { it.first.currentPrice() * it.second } * 1.17)).coerceAtLeast(
            0.0
        )
    }"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRest(): String {
    return karikaPriceFormat(
        ((key.minOrderAmount()
            ?.toDoubleOrNull()
            ?: 0.0) - (value.sumOf { it.first.currentPrice() * it.second } * 1.17)).coerceAtLeast(
            0.0
        )
    )
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.progress(): Pair<Float, Float> {
    val min = key.minOrderAmount()?.toDoubleOrNull() ?: 0.0
    val current = (value.sumOf { it.first.currentPrice() * it.second } * 1.17).coerceAtLeast(0.0)
    if (current == 0.0) {
        return Pair(1f, 0f)
    }

    return Pair(
        (current / min).toFloat(),
        1f - (current / min).toFloat()
    )
}

private fun Map<Vendor, List<Pair<Product, Int>>>.orderValid(): Boolean {
    return all {
        it.minAmountRestValue().toDouble() == 0.0
    }
}

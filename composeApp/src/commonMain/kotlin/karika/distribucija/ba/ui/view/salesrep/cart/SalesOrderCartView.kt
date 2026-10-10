package karika.distribucija.ba.ui.view.salesrep.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfCartResponseItem
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_minus
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_trash
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOrderCartView(component: SalesOrderCartComponent) {
    val cart by component.cart.collectAsState()
    val items = cart?.items.orEmpty()
    val customer = component.customer

    val vpcTotal = items.sumOf { it.price * it.qty }
    val rowTotal = items.sumOf { it.rowTotal }
    val discountTotal = vpcTotal - rowTotal
    val grandTotal = rowTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        // ── Item list ──────────────────────────────────────────────────────────
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KEmptyPlaceholder(
                    icon = vectorResource(Res.drawable.ic_k_cart),
                    title = "Korpa je prazna",
                    message = "Dodajte artikle iz kataloga za ovog kupca.",
                    iconTint = VendorAccent,
                    iconBackground = VendorAccentSoft
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp)
            ) {
                // Title row with "Isprazni korpu"
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier.weight(1f),
                            text = "Artikli (${items.size})",
                            color = KarikaUiColors.Ink,
                            textSize = 18.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                        KTonalButton(
                            text = "Isprazni korpu",
                            icon = vectorResource(Res.drawable.ic_k_trash),
                            background = KarikaUiColors.RedSoft,
                            color = KarikaUiColors.Red,
                            height = 38.dp,
                            onClick = { component.clearCart() }
                        )
                    }
                }

                // Customer header of the cart card
                item {
                    CustomerHeader(
                        name = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName,
                        email = customer.email
                    )
                }

                itemsIndexed(items, key = { _, item -> item.itemId }) { index, item ->
                    CardSegment(isLast = index == items.lastIndex) {
                        if (index > 0) {
                            KDivider(modifier = Modifier.padding(horizontal = 14.dp))
                        }
                        CartItemRow(
                            item = item,
                            canDiscount = component.canCreateDiscountFor,
                            onQtyChange = { newQty -> component.updateQty(item, newQty) },
                            onDiscountChange = { newDiscount -> component.updateDiscount(item, newDiscount) },
                            onRemove = { component.removeItem(item) }
                        )
                    }
                }
            }

            // ── Sticky bottom summary ──────────────────────────────────────────
            KBottomPanel(modifier = Modifier.navigationBarsPadding()) {
                SummaryRow(label = "Međuzbir", value = karikaPriceFormat(vpcTotal) + " KM")
                SummaryRow(
                    label = "Popust",
                    value = "-" + karikaPriceFormat(discountTotal) + " KM",
                    valueColor = KarikaUiColors.Red
                )
                KDivider(modifier = Modifier.padding(vertical = 8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = "Ukupno",
                        color = KarikaUiColors.Muted,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W600
                    )
                    KarikaText(
                        text = karikaPriceFormat(grandTotal) + " KM",
                        color = KarikaUiColors.Ink,
                        textSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                }

                // Pregledaj narudžbu
                KPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Pregledaj narudžbu",
                    background = VendorAccent,
                    height = 50.dp,
                    enabled = items.isNotEmpty()
                ) { component.openOrderReview() }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = KarikaUiColors.Ink
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier.weight(1f),
            text = label,
            color = KarikaUiColors.Muted,
            textSize = 13.sp
        )
        KarikaText(
            text = value,
            color = valueColor,
            textSize = 14.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

private val CardTopShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)
private val CardBottomShape = RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp)

/** Customer row on top of the cart card, as the vendor row of the customer's cart. */
@Composable
private fun CustomerHeader(name: String, email: String?) {
    KCard(modifier = Modifier.fillMaxWidth(), shape = CardTopShape, border = null) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(
                name = name,
                textSize = 11.sp,
                background = VendorAccentSoft,
                color = VendorAccent
            )
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = name,
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
                if (!email.isNullOrBlank()) {
                    KarikaText(
                        text = email,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }
        KDivider()
    }
}

/** One part of the white cart card (the card is split so the list stays lazy). */
@Composable
private fun CardSegment(isLast: Boolean, content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isLast) Modifier.clip(CardBottomShape) else Modifier)
            .background(KarikaColors.White)
    ) {
        content()
    }
}

// ── Cart item row ──────────────────────────────────────────────────────────────

@Composable
private fun CartItemRow(
    item: OnBehalfCartResponseItem,
    canDiscount: Boolean,
    onQtyChange: (Int) -> Unit,
    onDiscountChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var localQty by remember(item.qty) { mutableIntStateOf(item.qty) }
    var localDiscount by remember(item.discountPercent) {
        mutableStateOf(item.discountPercent?.takeIf { it > 0 }?.toString() ?: "")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        KImage(
            modifier = Modifier
                .size(72.dp)
                .clip(RoundedCornerShape(10.dp)),
            url = item.imageUrl
        )

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
                    text = item.name,
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 2
                )
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .clickable { onRemove() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_k_trash),
                        contentDescription = "Ukloni",
                        tint = KarikaUiColors.Subtle,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (item.sku.isNotBlank()) {
                    KPill(
                        text = "#${item.sku}",
                        background = KarikaUiColors.Field,
                        color = KarikaUiColors.Muted,
                        textSize = 11.sp
                    )
                }
                if (!canDiscount && (item.discountPercent ?: 0) > 0) {
                    KPill(
                        text = "Rabat: ${item.discountPercent}%",
                        background = VendorAccentSoft,
                        color = VendorAccent,
                        textSize = 11.sp
                    )
                }
            }

            val discountPercent = localDiscount.toIntOrNull() ?: 0
            val vpc = item.price * localQty
            val discountedVpc = vpc * (1 - discountPercent / 100.0)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (discountPercent > 0) {
                        KarikaText(
                            text = karikaPriceFormat(vpc) + " KM",
                            color = KarikaUiColors.Subtle,
                            textSize = 11.5.sp,
                            maxLines = 1,
                            decoration = TextDecoration.LineThrough
                        )
                    }
                    KarikaText(
                        text = karikaPriceFormat(discountedVpc) + " KM",
                        color = KarikaUiColors.Ink,
                        textSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    KarikaText(
                        text = item.priceString() + " / " + (item.quantityUnit ?: "kom"),
                        color = KarikaUiColors.Muted,
                        textSize = 11.sp,
                        lineHeight = 14.sp,
                        maxLines = 1
                    )
                }

                // Blue "– n +" stepper
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(VendorAccentSoft)
                        .padding(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(KarikaColors.White)
                            .clickable {
                                if (localQty > 1) {
                                    localQty--
                                    onQtyChange(localQty)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        KIcon(icon = vectorResource(Res.drawable.ic_k_minus), tint = VendorAccent, size = 16.dp)
                    }

                    BasicTextField(
                        value = localQty.toString(),
                        onValueChange = { v ->
                            val n = v.filter { it.isDigit() }.toIntOrNull()
                            if (n != null && n > 0) {
                                localQty = n
                                onQtyChange(n)
                            }
                        },
                        modifier = Modifier.width(40.dp),
                        textStyle = TextStyle(
                            fontFamily = karikaFonts(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.W600,
                            color = KarikaUiColors.Ink,
                            textAlign = TextAlign.Center
                        ),
                        cursorBrush = SolidColor(VendorAccent),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(CircleShape)
                            .background(VendorAccent)
                            .clickable {
                                localQty++
                                onQtyChange(localQty)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_k_plus),
                            contentDescription = "+",
                            tint = KarikaColors.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            if (canDiscount) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(KarikaUiColors.Field)
                        .padding(start = 12.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = "Rabat",
                        color = KarikaUiColors.Muted,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W600
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(KarikaColors.White)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = localDiscount,
                            onValueChange = { v ->
                                val digits = v.filter { it.isDigit() }
                                localDiscount = when {
                                    digits.isEmpty() -> ""
                                    (digits.toIntOrNull() ?: 0) > 100 -> "100"
                                    else -> digits
                                }
                                onDiscountChange(localDiscount.toIntOrNull() ?: 0)
                            },
                            modifier = Modifier.width(34.dp),
                            textStyle = TextStyle(
                                fontFamily = karikaFonts(),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.W700,
                                color = KarikaUiColors.Ink,
                                textAlign = TextAlign.End
                            ),
                            cursorBrush = SolidColor(VendorAccent),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true
                        )
                        KarikaText(
                            text = "%",
                            color = KarikaUiColors.Muted,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }
            }
        }
    }
}

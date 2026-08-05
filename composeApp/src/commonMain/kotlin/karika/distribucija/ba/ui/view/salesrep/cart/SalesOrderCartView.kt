package karika.distribucija.ba.ui.view.salesrep.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_delete
import karikav2.composeapp.generated.resources.ic_products
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOrderCartView(component: SalesOrderCartComponent) {
    val cartItems by component.cartItems.collectAsState()
    val cartDiscounts by component.cartDiscounts.collectAsState()
    val items = cartItems.values.toList()

    val vpcTotal = items.sumOf { (product, qty) -> product.vpc(qty) }
    val discountTotal = items.sumOf { (product, qty) ->
        product.vpc(qty) * (cartDiscounts[product.key] ?: 0) / 100.0
    }
    val grandTotal = vpcTotal - discountTotal

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        // ── Item list ──────────────────────────────────────────────────────────
        if (items.isEmpty()) {
            Box(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_products),
                        contentDescription = "",
                        tint = KarikaColors.Gray9,
                        modifier = Modifier.size(48.dp)
                    )
                    KarikaText(
                        text = "Korpa je prazna",
                        color = KarikaColors.Gray6,
                        textSize = 15.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items, key = { (product, _) -> product.key }) { (product, qty) ->
                    CartItemRow(
                        product = product,
                        qty = qty,
                        discount = cartDiscounts[product.key] ?: 0,
                        onQtyChange = { newQty -> component.updateQty(product, newQty) },
                        onDiscountChange = { newDiscount -> component.updateDiscount(product, newDiscount) },
                        onRemove = { component.removeItem(product) }
                    )
                }
            }
        }

        // ── Sticky bottom summary ──────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Međuzbir
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = "Međuzbir",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
                KarikaText(
                    text = karikaPriceFormat(vpcTotal) + " KM",
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }

            // Popust
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = "Popust",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
                KarikaText(
                    text = "-" + karikaPriceFormat(discountTotal) + " KM",
                    color = KarikaColors.Red,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }

            HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

            // Total
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = "Ukupno",
                    color = KarikaColors.Gray2,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    text = karikaPriceFormat(grandTotal) + " KM",
                    color = KarikaColors.Blue,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W700
                )
            }

            Spacer(Modifier.height(4.dp))

            // Pregledaj narudžbu
            val canReview = items.isNotEmpty()
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canReview) KarikaColors.Blue else KarikaColors.Gray9)
                    .clickable(
                        enabled = canReview,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.openOrderReview() },
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Pregledaj narudžbu",
                    color = KarikaColors.White,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
            }

            // Isprazni korpu
            if (items.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.clearCart() },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Isprazni korpu",
                        color = KarikaColors.Gray4,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
    }
}

// ── Cart item row ──────────────────────────────────────────────────────────────

@Composable
private fun CartItemRow(
    product: OnBehalfProduct,
    qty: Int,
    discount: Int,
    onQtyChange: (Int) -> Unit,
    onDiscountChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    var localQty by remember(qty) { mutableIntStateOf(qty) }
    var localDiscount by remember(discount) { mutableStateOf(if (discount > 0) discount.toString() else "") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
    ) {
        // ── Top row: image + info + delete ────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            // Image
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KarikaColors.Gray20)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
            ) {
                KarikaImage(
                    model = product.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KarikaText(
                    text = product.name,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
                if (product.sku.isNotBlank()) {
                    KarikaText(
                        text = "#${product.sku}",
                        color = KarikaColors.Gray6,
                        textSize = 11.sp,
                        fontWeight = FontWeight.W500
                    )
                }
                KarikaText(
                    text = "${product.categoryLabel}",
                    color = KarikaColors.Gray6,
                    textSize = 11.sp,
                    fontWeight = FontWeight.W500
                )
                Spacer(Modifier.height(4.dp))
                val discountPercent = localDiscount.toIntOrNull() ?: 0
                val vpc = product.vpc(localQty)
                val discountedVpc = vpc * (1 - discountPercent / 100.0)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (discountPercent > 0) {
                        KarikaText(
                            text = karikaPriceFormat(vpc) + " KM",
                            color = KarikaColors.Gray6,
                            textSize = 12.sp,
                            fontWeight = FontWeight.W500,
                            decoration = TextDecoration.LineThrough
                        )
                    }
                    KarikaText(
                        text = karikaPriceFormat(discountedVpc) + " KM",
                        color = KarikaColors.Blue,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            // Delete button
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaColors.Red.copy(alpha = 0.08f))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onRemove() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    contentDescription = "Ukloni",
                    tint = KarikaColors.Red,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // ── Bottom row: stepper + rabat ───────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.Gray20)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KarikaText(
                    text = "Količina:",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
                        .padding(horizontal = 2.dp, vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (localQty > 1) {
                                    localQty--
                                    onQtyChange(localQty)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        KarikaText(
                            text = "−",
                            color = KarikaColors.Gray2,
                            textSize = 18.sp,
                            fontWeight = FontWeight.W700
                        )
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
                        modifier = Modifier.width(46.dp),
                        textStyle = TextStyle(
                            fontFamily = karikaFonts(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            color = KarikaColors.Gray2,
                            textAlign = TextAlign.Center
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                localQty++
                                onQtyChange(localQty)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_add_plus),
                            contentDescription = "+",
                            tint = KarikaColors.Gray2,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    text = "Rabat:",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
                        .padding(horizontal = 8.dp, vertical = 2.dp),
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
                        modifier = Modifier.width(30.dp),
                        textStyle = TextStyle(
                            fontFamily = karikaFonts(),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            color = KarikaColors.Gray2,
                            textAlign = TextAlign.End
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    KarikaText(
                        text = "%",
                        color = KarikaColors.Gray6,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
    }
}

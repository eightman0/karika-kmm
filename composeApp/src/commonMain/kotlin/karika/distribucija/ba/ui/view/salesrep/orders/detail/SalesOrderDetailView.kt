package karika.distribucija.ba.ui.view.salesrep.orders.detail

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_location
import karikav2.composeapp.generated.resources.ic_person
import karikav2.composeapp.generated.resources.ic_phone
import karikav2.composeapp.generated.resources.ic_print
import karikav2.composeapp.generated.resources.ic_send_receipt
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOrderDetailView(component: SalesOrderDetailComponent) {
    val order = component.order
    val vendorOrder by component.vendorOrder.collectAsState()
    val comments by component.comments.collectAsState()
    val isSendingComment by component.isSendingComment.collectAsState()
    var commentText by remember { mutableStateOf("") }

    // Derived values — prefer vendorOrder fields when loaded, fall back to OnBehalfOrder
    val vpcTotal = vendorOrder?.orderTotal?.toDoubleOrNull()
        ?: (order.grandTotal / 1.17).toDouble()
    val pdvTotal = (vpcTotal * 0.17)
    val grandTotal = vpcTotal + pdvTotal
    val commission = vendorOrder?.shopCommissionFee?.toDoubleOrNull()

    val address = vendorOrder?.address?.let { a ->
        listOfNotNull(a.street, a.city, a.postcode).filter { it.isNotBlank() }.joinToString(", ")
    }.takeIf { !it.isNullOrBlank() } ?: "—"

    val phone = vendorOrder?.address?.telephone
        .takeIf { !it.isNullOrBlank() } ?: "—"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ── Informacije o narudžbi ───────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SectionHeader(title = "Informacije o narudžbi")
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(KarikaColors.White)
                            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
                            .padding(20.dp)
                    ) {
                        // Row 1: Broj narudžbe | Datum
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            InfoCell(
                                label = "BROJ NARUDŽBE",
                                value = "#${order.incrementId}",
                                valueColor = KarikaColors.Gray2,
                                modifier = Modifier.weight(1f)
                            )
                            InfoCell(
                                label = "DATUM",
                                value = order.date().ifBlank { "—" },
                                valueColor = KarikaColors.Gray2,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = KarikaColors.Gray9)
                        Spacer(Modifier.height(16.dp))

                        // Row 2: Ukupno VPC | Sa PDV | Karika Provizija
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FinancialCell(
                                label = "UKUPNO VPC",
                                value = karikaPriceFormat(vpcTotal) + " KM",
                                valueColor = KarikaColors.Blue,
                                modifier = Modifier.weight(1f)
                            )
                            FinancialCell(
                                label = "SA PDV",
                                value = karikaPriceFormat(grandTotal) + " KM",
                                valueColor = KarikaColors.Gray2,
                                modifier = Modifier.weight(1f)
                            )
                            FinancialCell(
                                label = "PROVIZIJA",
                                value = if (commission != null) karikaPriceFormat(commission) + " KM" else "—",
                                valueColor = KarikaColors.Blue,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = KarikaColors.Gray9)
                        Spacer(Modifier.height(16.dp))

                        // Customer rows
                        CustomerInfoRow(
                            icon = Res.drawable.ic_person,
                            label = "NAZIV KUPCA",
                            line1 = order.displayName(),
                            line2 = "ID: ${order.customerId}"
                        )
                        Spacer(Modifier.height(16.dp))
                        CustomerInfoRow(
                            icon = Res.drawable.ic_location,
                            label = "ADRESA ZA ISPORUKU",
                            line1 = address
                        )
                        Spacer(Modifier.height(16.dp))
                        CustomerInfoRow(
                            icon = Res.drawable.ic_phone,
                            label = "KONTAKT TELEFON",
                            line1 = phone
                        )
                    }
                }
            }

            // ── Specifikacija narudžbe ───────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                    val products = vendorOrder?.products ?: emptyList()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        SectionHeader(title = "Specifikacija narudžbe")
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(KarikaColors.Gray9)
                                .padding(horizontal = 10.dp, vertical = 3.dp)
                        ) {
                            KarikaText(
                                text = "${products.size} artikala",
                                color = KarikaColors.Gray6,
                                textSize = 11.sp,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(KarikaColors.White)
                                .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                text = "—",
                                color = KarikaColors.Gray6,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    }
                }
            }

            // Product items
            val products = vendorOrder?.products ?: emptyList()
            items(products, key = { it.itemId ?: it.productId ?: "" }) { product ->
                ProductCard(
                    product = product,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            // ── Za plaćanje ──────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    TotalRow(
                        label = "Ukupno VPC",
                        value = karikaPriceFormat(vpcTotal) + " KM",
                        labelColor = KarikaColors.Gray6,
                        valueColor = KarikaColors.Gray2,
                        labelSize = 14.sp,
                        valueSize = 15.sp
                    )
                    YSpacer8()
                    TotalRow(
                        label = "Ukupno PDV 17%",
                        value = karikaPriceFormat(pdvTotal) + " KM",
                        labelColor = KarikaColors.Gray6,
                        valueColor = KarikaColors.Gray2,
                        labelSize = 14.sp,
                        valueSize = 15.sp
                    )
                    YSpacer8()
                    TotalRow(
                        label = "Ukupno sa PDV",
                        value = karikaPriceFormat(grandTotal) + " KM",
                        labelColor = KarikaColors.Gray6,
                        valueColor = KarikaColors.Gray2,
                        labelSize = 14.sp,
                        valueSize = 15.sp
                    )
                    YSpacer8()
                    TotalRow(
                        label = "Karika provizija",
                        value = if (commission != null) karikaPriceFormat(commission) + " KM" else "—",
                        labelColor = KarikaColors.Gray6,
                        valueColor = KarikaColors.Blue,
                        labelSize = 14.sp,
                        valueSize = 15.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = KarikaColors.Gray9)
                    Spacer(Modifier.height(12.dp))
                    TotalRow(
                        label = "ZA PLAĆANJE",
                        value = karikaPriceFormat(grandTotal) + " KM",
                        labelColor = KarikaColors.Gray2,
                        valueColor = KarikaColors.Blue,
                        labelSize = 16.sp,
                        valueSize = 22.sp,
                        bold = true
                    )
                }
            }

            // ── Komentari narudžbe ───────────────────────────────────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    SectionHeader(title = "Komentari narudžbe")
                    Spacer(Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(24.dp))
                            .background(KarikaColors.White)
                            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Existing comments
                        if (comments.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                comments.forEach { comment ->
                                    CommentBubble(comment = comment)
                                }
                            }
                            HorizontalDivider(color = KarikaColors.Gray9)
                        }

                        // Textarea
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(KarikaColors.Gray10)
                                .padding(12.dp)
                        ) {
                            if (commentText.isEmpty()) {
                                KarikaText(
                                    text = "Napiši komentar kupcu...",
                                    color = KarikaColors.Gray8,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                            BasicTextField(
                                value = commentText,
                                onValueChange = { commentText = it },
                                textStyle = TextStyle(
                                    color = KarikaColors.Gray2,
                                    fontSize = 14.sp,
                                    fontFamily = karikaFonts()
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(100.dp)
                            )
                        }

                        // Send button
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    if (isSendingComment) KarikaColors.Gray9 else KarikaColors.Blue
                                )
                                .clickable(
                                    enabled = !isSendingComment,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    component.sendComment(commentText)
                                    commentText = ""
                                }
                                .padding(vertical = 14.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (isSendingComment) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = KarikaColors.Gray6,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_send_receipt),
                                    contentDescription = "",
                                    tint = KarikaColors.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(8.dp))
                                KarikaText(
                                    text = "Pošalji komentar",
                                    color = KarikaColors.White,
                                    textSize = 15.sp,
                                    fontWeight = FontWeight.W700
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Print FAB ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(20.dp)
                .size(56.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(KarikaColors.Blue)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {}
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_print),
                contentDescription = "Printaj",
                tint = KarikaColors.White,
                modifier = Modifier
                    .size(24.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

// ── Comment bubble ────────────────────────────────────────────────────────────

@Composable
private fun CommentBubble(comment: Comment) {
    val isMine = comment.isMine()
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.82f)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isMine) 16.dp else 4.dp,
                        bottomEnd = if (isMine) 4.dp else 16.dp
                    )
                )
                .background(if (isMine) KarikaColors.Blue else KarikaColors.Gray10)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            KarikaText(
                text = comment.message(),
                color = if (isMine) KarikaColors.White else KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        }
        Spacer(Modifier.height(2.dp))
        KarikaText(
            text = comment.createdAt(),
            color = KarikaColors.Gray7,
            textSize = 10.sp,
            fontWeight = FontWeight.W400
        )
    }
}

// ── Product card ──────────────────────────────────────────────────────────────

@Composable
private fun ProductCard(product: VendorProduct, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Header: name + qty + rabat badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = product.name ?: "—",
                    color = KarikaColors.Gray2,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = "Količina: ${product.qtyOrdered ?: "—"} ${product.unit ?: "kom"}",
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(KarikaColors.Blue.copy(alpha = 0.1f))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                KarikaText(
                    text = "Rabat ${product.rabat()}%",
                    color = KarikaColors.Blue,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = KarikaColors.Gray9)
        Spacer(Modifier.height(12.dp))

        // 2x2 price grid
        Row(modifier = Modifier.fillMaxWidth()) {
            PriceCell(
                label = "Cijena VPC",
                value = product.priceVpc(),
                modifier = Modifier.weight(1f)
            )
            PriceCell(
                label = "Ukupno VPC",
                value = product.totalVpc(),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            PriceCell(
                label = "Ukupno sa PDV",
                value = product.totalWithPdv(),
                modifier = Modifier.weight(1f)
            )
            PriceCell(
                label = "Provizija (${product.commissionPercent ?: "—"}%)",
                value = if (product.commission != null)
                    karikaPriceFormat(product.commission?.toDoubleOrNull() ?: 0.0) + " KM"
                else "—",
                valueColor = KarikaColors.Blue,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun PriceCell(
    label: String,
    value: String,
    valueColor: Color = KarikaColors.Gray2,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray7,
            textSize = 11.sp,
            fontWeight = FontWeight.W500
        )
        Spacer(Modifier.height(2.dp))
        KarikaText(
            text = value,
            color = valueColor,
            textSize = 14.sp,
            fontWeight = FontWeight.W700
        )
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

private fun statusDotColor(status: String): Color = when (status) {
    "approved"      -> KarikaColors.Green3
    "rejected"      -> KarikaColors.Error
    "cancelled"     -> KarikaColors.Gray6
    "pending",
    "processing"    -> KarikaColors.Blue
    "bill-sent",
    "estimate-sent" -> KarikaColors.Orange
    else            -> KarikaColors.Gray6
}

@Composable
private fun SectionHeader(title: String) {
    KarikaText(
        text = title,
        color = KarikaColors.Gray6,
        textSize = 15.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun InfoCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray7,
            textSize = 10.sp,
            fontWeight = FontWeight.W700
        )
        Spacer(Modifier.height(2.dp))
        KarikaText(
            text = value,
            color = valueColor,
            textSize = 15.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
private fun FinancialCell(
    label: String,
    value: String,
    valueColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray10)
            .padding(10.dp)
    ) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray7,
            textSize = 9.sp,
            fontWeight = FontWeight.W700
        )
        Spacer(Modifier.height(4.dp))
        KarikaText(
            text = value,
            color = valueColor,
            textSize = 15.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun CustomerInfoRow(
    icon: DrawableResource,
    label: String,
    line1: String,
    line2: String? = null
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KarikaColors.Gray10),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(icon),
                contentDescription = "",
                tint = KarikaColors.Blue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            KarikaText(
                text = label,
                color = KarikaColors.Gray7,
                textSize = 10.sp,
                fontWeight = FontWeight.W700
            )
            Spacer(Modifier.height(2.dp))
            KarikaText(
                text = line1,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 2,
                textOverflow = TextOverflow.Ellipsis
            )
            if (line2 != null) {
                KarikaText(
                    text = line2,
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
}

@Composable
private fun TotalRow(
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color,
    labelSize: TextUnit,
    valueSize: TextUnit,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            text = label,
            color = labelColor,
            textSize = labelSize,
            fontWeight = if (bold) FontWeight.W700 else FontWeight.W500
        )
        KarikaText(
            text = value,
            color = valueColor,
            textSize = valueSize,
            fontWeight = FontWeight.W700
        )
    }
}

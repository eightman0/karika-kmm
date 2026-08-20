package karika.distribucija.ba.ui.view.salesrep.orders.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rememberImeVisible
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import karikav2.composeapp.generated.resources.ic_location
import karikav2.composeapp.generated.resources.ic_person
import karikav2.composeapp.generated.resources.ic_phone
import karikav2.composeapp.generated.resources.ic_print
import karikav2.composeapp.generated.resources.ic_send_receipt
import karikav2.composeapp.generated.resources.img_ab_post
import karikav2.composeapp.generated.resources.img_express_post
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOrderDetailView(component: SalesOrderDetailComponent) {
    val vendorOrder by component.vendorOrder.collectAsState()
    val comments by component.comments.collectAsState()
    val isSendingComment by component.isSendingComment.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var editingItem by component.editOrderItem.asState()
    var deliveryExpanded by remember { mutableStateOf(false) }
    var shippingCost by remember { mutableStateOf<Pair<Double?, Double?>?>(null) }
    val imeVisible = rememberImeVisible()

    val canEdit = vendorOrder.isPending() && !vendorOrder.locked()

    val vpcTotal = vendorOrder.orderTotal?.toDoubleOrNull() ?: 0.0
    val pdvTotal = (vpcTotal * 0.17)
    val grandTotal = vpcTotal + pdvTotal
    val commission = vendorOrder.shopCommissionFee?.toDoubleOrNull()

    val address = vendorOrder.address?.let { a ->
        listOfNotNull(a.street, a.city, a.postcode).filter { it.isNotBlank() }.joinToString(", ")
    }.takeIf { !it.isNullOrBlank() } ?: "—"

    val phone = vendorOrder.address?.telephone
        .takeIf { !it.isNullOrBlank() } ?: "—"

    Box(
        modifier = Modifier
            .hideKeyboard()
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
                                value = "#${vendorOrder.orderId}",
                                valueColor = KarikaColors.Gray2,
                                modifier = Modifier.weight(1f)
                            )
                            InfoCell(
                                label = "DATUM",
                                value = vendorOrder.date(),
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
                            line1 = vendorOrder.b2bPravnoLice ?: "Kupac #${vendorOrder.customerId}",
                            line2 = "ID: ${vendorOrder.customerId}"
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

            // ── Usluga dostave (foldable) ─────────────────────────────────────
            if (canEdit) {
                item {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(KarikaColors.Yellow1)
                            .border(1.dp, KarikaColors.Yellow, RoundedCornerShape(16.dp)),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { deliveryExpanded = !deliveryExpanded }
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                text = "Usluga dostave",
                                color = KarikaColors.Yellow2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W700
                            )
                            Icon(
                                imageVector = vectorResource(
                                    if (deliveryExpanded) Res.drawable.ic_arrow_up else Res.drawable.ic_arrow_down
                                ),
                                contentDescription = "",
                                tint = KarikaColors.Yellow2,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (deliveryExpanded) {
                            KarikaText(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Adresa za utovar",
                                color = KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W700
                            )
                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Kontakt osoba*",
                                    value = component.contactName,
                                    placeholder = "Kontakt osoba",
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Email adresa*",
                                    value = component.contactEmail,
                                    placeholder = "Email adresa",
                                    keyboardType = KeyboardType.Email,
                                    imeAction = ImeAction.Next
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Telefon*",
                                    value = component.contactPhone,
                                    placeholder = "Telefon",
                                    keyboardType = KeyboardType.Phone,
                                    imeAction = ImeAction.Next
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Grad*",
                                    value = component.contactCity,
                                    placeholder = "Grad",
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Adresa*",
                                    value = component.contactAddress,
                                    placeholder = "Adresa",
                                    keyboardType = KeyboardType.Text,
                                    imeAction = ImeAction.Next
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Poštanski broj*",
                                    value = component.contactPostal,
                                    placeholder = "Poštanski broj",
                                    keyboardType = KeyboardType.Number,
                                    allowedChars = KarikaConstants.numbers,
                                    imeAction = ImeAction.Next
                                )
                            }

                            KarikaText(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                                text = "Kalkulator dostave",
                                color = KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W700
                            )

                            Column(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Ukupna širina*",
                                    value = component.packageWidth,
                                    placeholder = "Ukupna širina",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    trailingIcons = {
                                        KarikaText(
                                            text = "cm",
                                            color = KarikaColors.Gray4,
                                            textSize = 16.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Ukupna visina*",
                                    value = component.packageHeight,
                                    placeholder = "Ukupna visina",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    trailingIcons = {
                                        KarikaText(
                                            text = "cm",
                                            color = KarikaColors.Gray4,
                                            textSize = 16.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Ukupna dubina*",
                                    value = component.packageDepth,
                                    placeholder = "Ukupna dubina",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    trailingIcons = {
                                        KarikaText(
                                            text = "cm",
                                            color = KarikaColors.Gray4,
                                            textSize = 16.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                )
                                KarikaTextField1(
                                    modifier = Modifier.fillMaxWidth(),
                                    title = "Ukupna težina*",
                                    value = component.packageWeight,
                                    placeholder = "Ukupna težina",
                                    keyboardType = KeyboardType.Number,
                                    imeAction = ImeAction.Next,
                                    trailingIcons = {
                                        KarikaText(
                                            text = "kg",
                                            color = KarikaColors.Gray4,
                                            textSize = 16.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                )
                            }

                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = KarikaColors.Gray9
                            )

                            ShippingProviderRow(
                                image = Res.drawable.img_ab_post,
                                label = "A2B Express",
                                cost = shippingCost?.first,
                                selected = component.selectedCarrierCode.value == "A2B",
                                onSelect = { component.selectedCarrierCode.value = "A2B" }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = KarikaColors.Gray9
                            )
                            ShippingProviderRow(
                                image = Res.drawable.img_express_post,
                                label = "EuroExpress",
                                cost = shippingCost?.second,
                                selected = component.selectedCarrierCode.value == "EURO_EXPRESS",
                                onSelect = { component.selectedCarrierCode.value = "EURO_EXPRESS" }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = KarikaColors.Gray9
                            )

                            KarikaText(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                text = "Napomena za dostavu",
                                color = KarikaColors.Gray4,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400
                            )
                            KarikaTextField2(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp)
                                    .heightIn(min = 66.dp)
                                    .fillMaxWidth(),
                                value = component.deliveryNote,
                                placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )

                            PrimaryButtonFilled(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, top = 4.dp)
                                    .height(48.dp)
                                    .fillMaxWidth(),
                                title = "Izračunaj cijenu"
                            ) {
                                shippingCost = component.calculateShipping(
                                    component.packageWidth.value,
                                    component.packageHeight.value,
                                    component.packageDepth.value,
                                    component.packageWeight.value
                                )
                            }

                            PrimaryButtonFilled(
                                modifier = Modifier
                                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                    .height(48.dp)
                                    .fillMaxWidth(),
                                title = "Sačuvaj podatke o dostavi"
                            ) {
                                component.saveShippingDetails()
                            }
                        }
                    }
                }
            }

            // ── Specifikacija narudžbe ───────────────────────────────────────────
            item {
                val products = vendorOrder.products
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            text = "Specifikacija narudžbe",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
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

                    HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

                    if (products.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                text = "—",
                                color = KarikaColors.Gray6,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W500
                            )
                        }
                    } else {
                        ProductSpecificationTable(
                            products = products,
                            canEdit = canEdit,
                            onEditClick = { editingItem = it }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno VPC", value = karikaPriceFormat(vpcTotal) + " KM")
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno PDV 17%", value = karikaPriceFormat(pdvTotal) + " KM")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno sa PDV", value = karikaPriceFormat(grandTotal) + " KM")
                        StatCard(
                            modifier = Modifier.weight(1f),
                            label = "Karika provizija",
                            value = if (commission != null) karikaPriceFormat(commission) + " KM" else "—"
                        )
                    }
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
        if (!imeVisible) {
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
                        .onClick {
                            component.printOrder()
                        }
                )
            }
        }
    }

    editingItem?.let { item ->
        EditOrderItemModal(
            item = item,
            canDiscount = component.canCreateDiscountFor,
            onDismiss = { editingItem = null },
            onConfirm = { newQty, newDiscount ->
                component.editOrderProduct(newQty, newDiscount)
            }
        )
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
            HtmlTextWithStyles(
                html = comment.message(),
                textColor = if (isMine) KarikaColors.White else KarikaColors.Gray2,
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

// ── Specifikacija narudžbe (tabela) ─────────────────────────────────────────────

private val PRODUCT_TABLE_COLUMN_WIDTHS =
    listOf(140.dp, 80.dp, 100.dp, 90.dp, 110.dp, 130.dp, 110.dp, 100.dp, 90.dp)

@Composable
private fun ProductSpecificationTable(
    products: List<VendorProduct>,
    canEdit: Boolean,
    onEditClick: (VendorProduct) -> Unit
) {
    val headers = listOf(
        "ARTIKAL", "RABAT %", "CIJENA VPC", "KOLIČINA",
        "UKUPNO VPC", "UKUPNO SA PDV", "PROVIZIJA %", "PROVIZIJA"
    ) + if (canEdit) listOf("AKCIJE") else emptyList()
    val widths = if (canEdit) PRODUCT_TABLE_COLUMN_WIDTHS else PRODUCT_TABLE_COLUMN_WIDTHS.dropLast(1)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
    ) {
        // ── Header row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .background(KarikaColors.Gray20)
        ) {
            headers.forEachIndexed { colIndex, header ->
                TableHeaderCell(header, width = widths[colIndex])
                if (colIndex != headers.lastIndex) {
                    VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                }
            }
        }
        HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

        // ── Data rows ────────────────────────────────────────────────────
        products.forEachIndexed { rowIndex, product ->
            val price = product.price?.toDoubleOrNull() ?: 0.0
            val discountPercent = product.rabat().toIntOrNull() ?: 0
            val discountedPrice = price * (1.0 - discountPercent / 100.0)
            val qty = product.qtyOrdered?.toIntOrNull() ?: 0
            val rowTotalVpc = discountedPrice * qty
            val rowTotalWithPdv = rowTotalVpc * 1.17

            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TableCell(product.name ?: "—", width = widths[0], bold = true)
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(product.rabat(), width = widths[1])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(karikaPriceFormat(discountedPrice) + " KM", width = widths[2])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                QuantityCell(product = product, width = widths[3])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(karikaPriceFormat(rowTotalVpc) + " KM", width = widths[4])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(karikaPriceFormat(rowTotalWithPdv) + " KM", width = widths[5])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(product.commissionPercent(), width = widths[6])
                VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                TableCell(product.commission(), width = widths[7])
                if (canEdit) {
                    VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                    TableActionCell(width = widths[8]) { onEditClick(product) }
                }
            }
            if (rowIndex != products.lastIndex) {
                HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun QuantityCell(product: VendorProduct, width: Dp) {
    val original = product.originalQty()
    Column(
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        if (original.isNotEmpty()) {
            KarikaText(
                text = original,
                color = KarikaColors.Gray7,
                textSize = 12.sp,
                fontWeight = FontWeight.W500,
                decoration = TextDecoration.LineThrough,
                maxLines = 1
            )
        }
        KarikaText(
            text = product.qty(),
            color = KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    KarikaText(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 10.dp),
        color = KarikaColors.Gray6,
        textSize = 10.sp,
        fontWeight = FontWeight.W700,
        maxLines = 2
    )
}

@Composable
private fun TableCell(text: String, width: Dp, bold: Boolean = false) {
    KarikaText(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 10.dp),
        color = KarikaColors.Gray2,
        textSize = 13.sp,
        fontWeight = if (bold) FontWeight.W700 else FontWeight.W500,
        maxLines = 2
    )
}

@Composable
private fun TableActionCell(width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        KarikaText(
            text = "Izmijeni",
            color = KarikaColors.Blue,
            textSize = 13.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray20)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray6,
            textSize = 12.sp,
            fontWeight = FontWeight.W600
        )
        KarikaText(
            text = value,
            color = KarikaColors.Gray2,
            textSize = 15.sp,
            fontWeight = FontWeight.W700
        )
    }
}

// ── Izmjena stavke narudžbe ──────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOrderItemModal(
    item: VendorProduct,
    canDiscount: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newQty: Int, newDiscount: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var discountText by remember {
        mutableStateOf(item.rabat().toIntOrNull()?.takeIf { it > 0 }?.toString() ?: "")
    }
    var qtyText by remember { mutableStateOf(item.qtyOrdered ?: "1") }
    val qtyValid = (qtyText.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KarikaColors.Gray2, width = 60.dp)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                text = "Izmijeni stavku",
                modifier = Modifier.fillMaxWidth(),
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

            KarikaText(
                text = item.name ?: "—",
                color = KarikaColors.Gray2,
                textSize = 15.sp,
                fontWeight = FontWeight.W700
            )

            if (canDiscount) {
                LabeledTextField(
                    label = "Rabat (%)",
                    value = discountText,
                    onValueChange = { v ->
                        val digits = v.filter { it.isDigit() }
                        discountText = when {
                            digits.isEmpty() -> ""
                            (digits.toIntOrNull() ?: 0) > 100 -> "100"
                            else -> digits
                        }
                    },
                    keyboardType = KeyboardType.Number
                )
            } else if ((item.rabat().toIntOrNull() ?: 0) > 0) {
                KarikaText(
                    text = "Rabat: ${item.rabat()}%",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
            }

            LabeledTextField(
                label = "Količina",
                value = qtyText,
                onValueChange = { v -> qtyText = v.filter { it.isDigit() } },
                keyboardType = KeyboardType.Number
            )

            HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Odustani",
                        color = KarikaColors.Blue,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (qtyValid) KarikaColors.Blue else KarikaColors.Gray9)
                        .clickable(
                            enabled = qtyValid,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onConfirm(
                                qtyText.toIntOrNull() ?: (item.qtyOrdered?.toIntOrNull() ?: 1),
                                discountText.toIntOrNull() ?: 0
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Izmijeni",
                        color = KarikaColors.White,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        KarikaText(
            text = label,
            color = KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(KarikaColors.White)
                .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = karikaFonts(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = KarikaColors.Gray2
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true
            )
        }
    }
}

@Composable
private fun ShippingProviderRow(
    image: DrawableResource,
    label: String,
    cost: Double?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) KarikaColors.Blue.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) KarikaColors.Blue else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onSelect() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                modifier = Modifier.width(93.dp),
                contentScale = ContentScale.FillWidth,
                painter = painterResource(image),
                contentDescription = ""
            )
            KarikaText(
                text = label,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
        KarikaText(
            text = "Cijena dostave sa PDV: " + (cost?.let { karikaPriceFormat(it) + " KM" } ?: "—"),
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
}

// ── Shared composables ────────────────────────────────────────────────────────

private fun statusDotColor(status: String): Color = when (status) {
    "approved" -> KarikaColors.Green3
    "rejected" -> KarikaColors.Error
    "cancelled" -> KarikaColors.Gray6
    "pending",
    "processing" -> KarikaColors.Blue

    "bill-sent",
    "estimate-sent" -> KarikaColors.Orange

    else -> KarikaColors.Gray6
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

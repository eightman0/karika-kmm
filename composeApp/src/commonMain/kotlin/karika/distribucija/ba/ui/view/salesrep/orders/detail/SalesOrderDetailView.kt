package karika.distribucija.ba.ui.view.salesrep.orders.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.File
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KKeyValueCard
import karika.distribucija.ba.ui.components.KKeyValueRow
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rememberImeVisible
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.orders.SheetHandle
import karika.distribucija.ba.ui.view.distributer.orders.details.component.AttachBillModal
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderField
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderModalButtons
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderRadioMark
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_menu
import karikav2.composeapp.generated.resources.ic_k_phone
import karikav2.composeapp.generated.resources.ic_k_send
import karikav2.composeapp.generated.resources.ic_k_truck
import karikav2.composeapp.generated.resources.ic_pdf
import karikav2.composeapp.generated.resources.ic_print
import karikav2.composeapp.generated.resources.img_ab_post
import karikav2.composeapp.generated.resources.img_express_post
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

private val NavyLabel = Color(0xFF9AA1B4)
private val AmberCard = Color(0xFFFFFBEB)
private val AmberBorder = Color(0xFFFCD34D)
private val AmberLine = Color(0xFFFDE68A)
private val AmberTitle = Color(0xFF92400E)

private const val LOCKED_TEXT =
    "Za prikaz detalja narudžbe, molimo Vas da zaključite prethodne narudžbe tako što ćete ih označiti kao odobrene ili odbijene!"

private fun Modifier.lockedBlur(locked: Boolean) = blur(radius = if (locked) 5.dp else 0.dp)

@Composable
fun SalesOrderDetailView(component: SalesOrderDetailComponent) {
    val vendorOrder by component.vendorOrder.collectAsState()
    val isOrderLoaded by component.isOrderLoaded.collectAsState()
    val comments by component.comments.collectAsState()
    val isSendingComment by component.isSendingComment.collectAsState()
    var commentText by remember { mutableStateOf("") }
    var editingItem by component.editOrderItem.asState()
    var deliveryExpanded by remember { mutableStateOf(false) }
    var shippingCost by remember { mutableStateOf<Pair<Double?, Double?>?>(null) }
    var showPrintMenu by remember { mutableStateOf(false) }
    var showAttachBillModal by remember { mutableStateOf(false) }
    val imeVisible = rememberImeVisible()

    val canEdit = vendorOrder.isPending() && !vendorOrder.locked()
    // The screen shows a full-screen loader until getOrder() responds (see below), so this
    // only needs to account for a locked order once real data is on screen.
    val blurInfoFields = vendorOrder.locked()

    // Auto-calculate the shipping price whenever the order already carries a saved package
    // size/weight (loaded into these fields by refreshOrder()) - the rep shouldn't have to
    // press "Izračunaj cijenu" again just to see a price that was already computed before.
    LaunchedEffect(vendorOrder) {
        val width = component.packageWidth.value
        val height = component.packageHeight.value
        val depth = component.packageDepth.value
        val weight = component.packageWeight.value
        if (width.isNotBlank() && height.isNotBlank() && depth.isNotBlank() && weight.isNotBlank()) {
            shippingCost = component.calculateShipping(width, height, depth, weight)
        }
    }

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
            .background(KarikaUiColors.Page)
    ) {
        if (!isOrderLoaded) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = VendorAccent)
            }
            return@Box
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp)
        ) {
            // ── Locked notice ────────────────────────────────────────────────────
            if (vendorOrder.locked()) {
                item { LockedNotice() }
            }

            // ── Informacije o narudžbi ───────────────────────────────────────────
            item {
                KSectionTitle(
                    modifier = Modifier.padding(bottom = 12.dp),
                    title = "Informacije o narudžbi"
                )
                OrderSummary(
                    order = vendorOrder,
                    vpcTotal = vpcTotal,
                    grandTotal = grandTotal,
                    commission = commission,
                    blurred = blurInfoFields
                )
            }

            // ── Kupac ────────────────────────────────────────────────────────────
            item {
                CustomerSection(
                    order = vendorOrder,
                    phone = phone,
                    address = address,
                    blurred = blurInfoFields,
                    onError = { component.showMessage(it) }
                )
            }

            // ── Usluga dostave (foldable) ────────────────────────────────────────
            if (canEdit) {
                item {
                    DeliverySection(
                        component = component,
                        expanded = deliveryExpanded,
                        onToggle = { deliveryExpanded = !deliveryExpanded },
                        shippingCost = shippingCost,
                        onCalculate = {
                            shippingCost = component.calculateShipping(
                                component.packageWidth.value,
                                component.packageHeight.value,
                                component.packageDepth.value,
                                component.packageWeight.value
                            )
                        }
                    )
                }
            }

            // ── Specifikacija narudžbe ───────────────────────────────────────────
            item {
                Specification(
                    order = vendorOrder,
                    canEdit = canEdit,
                    vpcTotal = vpcTotal,
                    pdvTotal = pdvTotal,
                    grandTotal = grandTotal,
                    commission = commission,
                    onEditClick = { editingItem = it }
                )
            }

            // ── Komentari narudžbe ───────────────────────────────────────────────
            item {
                KSectionTitle(
                    modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
                    title = "Komentari narudžbe"
                )
                if (comments.isNotEmpty()) {
                    Column(
                        modifier = Modifier.padding(bottom = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        comments.forEach { comment ->
                            CommentBubble(
                                comment = comment,
                                customerName = vendorOrder.b2bPravnoLice,
                                component = component
                            )
                        }
                    }
                }

                KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                    if (vendorOrder.commentsArchived()) {
                        KarikaText(
                            text = "Komentari narudžbe su arhivirani",
                            color = KarikaUiColors.Muted,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W500,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                        )
                    } else {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Textarea
                            val fieldShape = RoundedCornerShape(12.dp)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(fieldShape)
                                    .background(KarikaUiColors.Field)
                                    .padding(horizontal = 14.dp, vertical = 12.dp)
                            ) {
                                if (commentText.isEmpty()) {
                                    KarikaText(
                                        text = "Napiši komentar kupcu...",
                                        color = KarikaUiColors.Subtle,
                                        textSize = 14.sp
                                    )
                                }
                                BasicTextField(
                                    value = commentText,
                                    onValueChange = { commentText = it },
                                    textStyle = TextStyle(
                                        color = KarikaUiColors.Ink,
                                        fontSize = 14.sp,
                                        fontFamily = karikaFonts()
                                    ),
                                    cursorBrush = SolidColor(VendorAccent),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            // Send button
                            if (isSendingComment) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(KarikaUiColors.Field),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        color = VendorAccent,
                                        strokeWidth = 2.dp
                                    )
                                }
                            } else {
                                KPrimaryButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Pošalji komentar",
                                    icon = vectorResource(Res.drawable.ic_k_send),
                                    background = VendorAccent,
                                    height = 48.dp
                                ) {
                                    component.sendComment(commentText)
                                    commentText = ""
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Print FAB ────────────────────────────────────────────────────────────
        // Rejected: nothing. Approved: single tap prints the order directly. Pending: tap
        // opens a menu of all three print/estimate actions.
        if (!imeVisible && !vendorOrder.isRejected()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .navigationBarsPadding()
                    .padding(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(VendorAccent)
                        .semantics { contentDescription = "Printaj" }
                        .clickable {
                            if (vendorOrder.isPending()) {
                                showPrintMenu = true
                            } else {
                                component.printOrder()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    KIcon(
                        icon = vectorResource(
                            if (vendorOrder.isPending()) Res.drawable.ic_k_menu else Res.drawable.ic_print
                        ),
                        tint = KarikaColors.White,
                        size = 24.dp
                    )
                }
                DropdownMenu(
                    expanded = showPrintMenu,
                    onDismissRequest = { showPrintMenu = false },
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    shadowElevation = 10.dp,
                    containerColor = KarikaColors.White,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    PrintMenuItem("Printaj narudžbu", vectorResource(Res.drawable.ic_print), KarikaUiColors.Ink) {
                        showPrintMenu = false
                        component.printOrder()
                    }
                    PrintMenuItem("Printaj predračun", vectorResource(Res.drawable.ic_k_document), KarikaUiColors.Ink) {
                        showPrintMenu = false
                        component.printEstimate()
                    }
                    PrintMenuItem("Pošalji predračun", vectorResource(Res.drawable.ic_k_send), VendorAccent) {
                        showPrintMenu = false
                        showAttachBillModal = true
                    }
                }
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

    if (showAttachBillModal) {
        AttachBillModal(
            component = component,
            onSubmit = { message, file ->
                component.sendEstimate(message, file.second, file.first)
                showAttachBillModal = false
            },
            onCancel = { showAttachBillModal = false }
        )
    }
}

// ── Print menu item ──────────────────────────────────────────────────────────────

@Composable
private fun PrintMenuItem(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    DropdownMenuItem(
        onClick = onClick,
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KIcon(icon = icon, tint = color, size = 18.dp)
                Spacer(Modifier.width(12.dp))
                KarikaText(
                    text = text,
                    color = color,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
            }
        }
    )
}

// ── Locked notice ────────────────────────────────────────────────────────────────

/** Shown while older orders have to be settled first; the order's data stays blurred. */
@Composable
private fun LockedNotice() {
    Row(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(KarikaUiColors.AmberSoft)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_lock), tint = KarikaUiColors.Amber, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = LOCKED_TEXT,
            color = KarikaUiColors.Amber,
            textSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.W600
        )
    }
}

// ── Navy summary ─────────────────────────────────────────────────────────────────

/** Light status colors for the pill on the navy summary card. */
private fun statusOnNavy(status: String?): Color = when (status) {
    "approved", "bill-sent" -> Color(0xFF86EFAC)
    "pending", "estimate-sent" -> Color(0xFFFCD34D)
    "rejected" -> Color(0xFFFCA5A5)
    else -> Color(0xFFD1D5DB)
}

/** Navy card: number, date and status, the total with PDV, total VPC and Karika's commission. */
@Composable
private fun OrderSummary(
    order: VendorOrder,
    vpcTotal: Double,
    grandTotal: Double,
    commission: Double?,
    blurred: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaUiColors.Ink)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            SummaryValue(
                modifier = Modifier.weight(1f),
                label = "BROJ NARUDŽBE",
                value = "#${order.orderId}"
            )
            SummaryValue(
                modifier = Modifier.weight(1f),
                label = "DATUM",
                value = order.date()
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Ukupno sa PDV",
                color = NavyLabel,
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
            val color = statusOnNavy(order.realOrderStatus)
            KPill(
                text = order.status(),
                background = color.copy(alpha = 0.15f),
                color = color,
                dot = color,
                textSize = 11.5.sp
            )
        }
        KarikaText(
            modifier = Modifier.padding(top = 4.dp).lockedBlur(blurred),
            text = karikaPriceFormat(grandTotal) + " KM",
            color = KarikaColors.White,
            textSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .padding(top = 14.dp, bottom = 12.dp)
                .fillMaxWidth()
                .height(1.dp)
                .background(KarikaColors.White.copy(alpha = 0.12f))
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryValue(
                modifier = Modifier.weight(1f),
                label = "Ukupno VPC",
                value = karikaPriceFormat(vpcTotal) + " KM"
            )
            SummaryValue(
                modifier = Modifier.weight(1f).lockedBlur(blurred),
                label = "Karika provizija",
                value = if (commission != null) karikaPriceFormat(commission) + " KM" else "—",
                color = lerp(VendorAccent, KarikaColors.White, 0.45f)
            )
        }
    }
}

@Composable
private fun SummaryValue(modifier: Modifier, label: String, value: String, color: Color = KarikaColors.White) {
    Column(modifier = modifier) {
        KarikaText(text = label, color = NavyLabel, textSize = 11.sp, maxLines = 1)
        KarikaText(
            modifier = Modifier.padding(top = 2.dp),
            text = value,
            color = color,
            textSize = 15.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

// ── Kupac ────────────────────────────────────────────────────────────────────────

/** "Kupac": company and contact person, call/email buttons and the customer's data. */
@Composable
private fun CustomerSection(
    order: VendorOrder,
    phone: String,
    address: String,
    blurred: Boolean,
    onError: (String) -> Unit
) {
    val name = order.b2bPravnoLice ?: "Kupac #${order.customerId}"
    KSectionTitle(
        modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        title = "Kupac"
    )
    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.padding(14.dp).lockedBlur(blurred),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(
                name = name,
                size = 44.dp,
                shape = RoundedCornerShape(12.dp),
                background = VendorAccentSoft,
                color = VendorAccent,
                textSize = 14.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = name,
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.W700
                )
                if (!order.billingName.isNullOrBlank()) {
                    KarikaText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = "${order.billingName} · kontakt osoba",
                        color = KarikaUiColors.Muted,
                        textSize = 12.5.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
    val callNumber = phone.takeIf { it.isNotBlank() && it != "—" && !blurred }
    val email = order.email().takeIf { it.isNotBlank() && it != "-" && !blurred }
    Row(
        modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContactButton(
            modifier = Modifier.weight(1f),
            text = "Pozovi",
            icon = vectorResource(Res.drawable.ic_k_phone),
            enabled = callNumber != null
        ) {
            openPhoneCall(callNumber ?: "") { onError(it) }
        }
        ContactButton(
            modifier = Modifier.weight(1f),
            text = "Email",
            icon = vectorResource(Res.drawable.ic_k_mail),
            enabled = email != null
        ) {
            openEmail(email ?: "") { onError(it) }
        }
    }
    KKeyValueCard(
        modifier = Modifier.padding(top = 10.dp).lockedBlur(blurred),
        rows = listOf(
            "PDV broj" to (order.pdvNumber ?: "-"),
            "ID broj" to (order.idNumber ?: "-"),
            "Kontakt osoba" to (order.billingName ?: "-"),
            "Email" to order.email(),
            "Telefon" to phone,
            "Adresa za isporuku" to address
        )
    )
}

@Composable
private fun ContactButton(
    text: String,
    icon: ImageVector,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(shape)
            .background(KarikaColors.White)
            .border(1.dp, KarikaUiColors.Line, shape)
            .clickable(enabled = enabled, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = icon, tint = if (enabled) VendorAccent else KarikaUiColors.Subtle, size = 18.dp)
        Spacer(Modifier.width(8.dp))
        KarikaText(
            text = text,
            color = if (enabled) KarikaUiColors.Ink else KarikaUiColors.Subtle,
            textSize = 13.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

// ── Usluga dostave ───────────────────────────────────────────────────────────────

/** "Usluga dostave": amber card that opens the pickup address and the delivery calculator. */
@Composable
private fun DeliverySection(
    component: SalesOrderDetailComponent,
    expanded: Boolean,
    onToggle: () -> Unit,
    shippingCost: Pair<Double?, Double?>?,
    onCalculate: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .padding(top = 12.dp)
            .fillMaxWidth()
            .clip(shape)
            .background(AmberCard)
            .border(1.dp, AmberBorder, shape)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KarikaUiColors.AmberSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_truck), tint = KarikaUiColors.Amber, size = 20.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Usluga dostave",
                    color = AmberTitle,
                    textSize = 14.5.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = if (expanded) "Unesite adresu za utovar robe" else "Karika preuzima i dostavlja robu kupcu",
                    color = KarikaUiColors.Amber,
                    textSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            KIcon(
                modifier = Modifier.rotate(if (expanded) 180f else 0f),
                icon = vectorResource(Res.drawable.ic_k_chevron_down),
                tint = KarikaUiColors.Amber,
                size = 18.dp
            )
        }
        if (expanded) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
            ) {
                Box(Modifier.fillMaxWidth().height(1.dp).background(AmberLine))
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    ShippingHeading("Adresa za utovar")
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactName,
                        label = "Kontakt osoba",
                        required = true,
                        placeholder = "Kontakt osoba"
                    )
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactEmail,
                        label = "Email adresa",
                        required = true,
                        placeholder = "Email adresa",
                        keyboardType = KeyboardType.Email
                    )
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactPhone,
                        label = "Telefon",
                        required = true,
                        placeholder = "Telefon",
                        keyboardType = KeyboardType.Phone
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.contactCity,
                            label = "Grad",
                            required = true,
                            placeholder = "Grad"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.contactPostal,
                            label = "Poštanski broj",
                            required = true,
                            placeholder = "Poštanski broj",
                            keyboardType = KeyboardType.Number,
                            allowedChars = KarikaConstants.numbers
                        )
                    }
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactAddress,
                        label = "Adresa",
                        required = true,
                        placeholder = "Adresa"
                    )

                    ShippingHeading("Kalkulator dostave", Modifier.padding(top = 6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageWidth,
                            label = "Ukupna širina",
                            required = true,
                            placeholder = "Širina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "cm"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageHeight,
                            label = "Ukupna visina",
                            required = true,
                            placeholder = "Visina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "cm"
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageDepth,
                            label = "Ukupna dubina",
                            required = true,
                            placeholder = "Dubina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "cm"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageWeight,
                            label = "Ukupna težina",
                            required = true,
                            placeholder = "Težina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "kg"
                        )
                    }

                    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                        CourierRow(
                            logo = Res.drawable.img_ab_post,
                            name = "A2B Express",
                            cost = shippingCost?.first,
                            selected = component.selectedCarrierCode.value == "A2B",
                            onSelect = { component.selectedCarrierCode.value = "A2B" }
                        )
                        KDivider()
                        CourierRow(
                            logo = Res.drawable.img_express_post,
                            name = "EuroExpress",
                            cost = shippingCost?.second,
                            selected = component.selectedCarrierCode.value == "EURO_EXPRESS",
                            onSelect = { component.selectedCarrierCode.value = "EURO_EXPRESS" }
                        )
                    }

                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.deliveryNote,
                        label = "Napomena za dostavu",
                        placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                        singleLine = false,
                        minHeight = 66.dp
                    )

                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Izračunaj cijenu",
                        background = KarikaUiColors.Ink,
                        height = 50.dp,
                        onClick = onCalculate
                    )
                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Sačuvaj podatke o dostavi",
                        background = VendorAccent,
                        height = 50.dp
                    ) {
                        component.saveShippingDetails()
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingHeading(text: String, modifier: Modifier = Modifier) {
    KarikaText(
        modifier = modifier,
        text = text,
        color = KarikaUiColors.Ink,
        textSize = 14.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun CourierRow(
    logo: DrawableResource,
    name: String,
    cost: Double?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (selected) VendorAccent.copy(alpha = 0.06f) else Color.Transparent)
            .clickable(onClick = onSelect)
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OrderRadioMark(selected = selected)
        Spacer(Modifier.width(12.dp))
        Image(
            modifier = Modifier.width(64.dp),
            contentScale = ContentScale.FillWidth,
            painter = painterResource(logo),
            contentDescription = name
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = name,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            KarikaText(
                modifier = Modifier.padding(top = 2.dp),
                text = "Cijena dostave sa PDV: " + (cost?.let { karikaPriceFormat(it) + " KM" } ?: "—"),
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp
            )
        }
    }
}

// ── Specifikacija narudžbe ───────────────────────────────────────────────────────

private fun itemsLabel(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "$count artikal"
        mod10 in 2..4 && mod100 !in 12..14 -> "$count artikla"
        else -> "$count artikala"
    }
}

/** "Specifikacija narudžbe": the items with rabat and commission, and the order's totals. */
@Composable
private fun Specification(
    order: VendorOrder,
    canEdit: Boolean,
    vpcTotal: Double,
    pdvTotal: Double,
    grandTotal: Double,
    commission: Double?,
    onEditClick: (VendorProduct) -> Unit
) {
    val products = order.products
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KSectionTitle(modifier = Modifier.weight(1f), title = "Specifikacija narudžbe")
        KarikaText(
            text = itemsLabel(products.size),
            color = KarikaUiColors.Muted,
            textSize = 13.sp,
            fontWeight = FontWeight.W500
        )
    }
    KCard(
        modifier = Modifier.fillMaxWidth().lockedBlur(order.locked()),
        shape = RoundedCornerShape(14.dp)
    ) {
        if (products.isEmpty()) {
            KarikaText(
                modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                text = "—",
                color = KarikaUiColors.Muted,
                textSize = 15.sp,
                textAlign = TextAlign.Center
            )
        }
        products.forEachIndexed { index, product ->
            if (index > 0) KDivider()
            SpecificationItem(product = product, editable = canEdit) { onEditClick(product) }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
        ) {
            KDivider()
            KKeyValueRow(label = "Ukupno VPC", value = karikaPriceFormat(vpcTotal) + " KM")
            KDivider()
            KKeyValueRow(label = "Ukupno PDV 17%", value = karikaPriceFormat(pdvTotal) + " KM")
            KDivider()
            KKeyValueRow(
                label = "Karika provizija",
                value = if (commission != null) karikaPriceFormat(commission) + " KM" else "—"
            )
            KDivider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = "Ukupno sa PDV",
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    text = karikaPriceFormat(grandTotal) + " KM",
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
private fun SpecificationItem(product: VendorProduct, editable: Boolean, onEdit: () -> Unit) {
    val price = product.price?.toDoubleOrNull() ?: 0.0
    val discountPercent = product.rabat().toIntOrNull() ?: 0
    val discountedPrice = price * (1.0 - discountPercent / 100.0)
    val qty = product.qtyOrdered?.toIntOrNull() ?: 0
    val rowTotalVpc = discountedPrice * qty
    val rowTotalWithPdv = rowTotalVpc * 1.17
    // Rabat also eats into Karika's commission on this row - e.g. a 50% rabat halves it.
    val commissionAmount = (product.commission?.toDoubleOrNull() ?: 0.0) * (1.0 - discountPercent / 100.0)

    val strike = SpanStyle(textDecoration = TextDecoration.LineThrough, color = KarikaUiColors.Subtle)
    val image = (product.thumbnail ?: product.smallImage ?: product.image)
        ?.takeIf { it.isNotBlank() && it != "no_selection" }
        ?.let { imageUrl(it) }

    Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
        KImage(
            modifier = Modifier.size(56.dp).clip(RoundedCornerShape(10.dp)),
            url = image,
            placeholderLabel = "foto"
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = product.name ?: "—",
                color = KarikaUiColors.Ink,
                textSize = 14.5.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.W600
            )
            KarikaText(
                modifier = Modifier.padding(top = 3.dp),
                atext = buildAnnotatedString {
                    val originalQty = product.originalQty()
                    if (originalQty.isNotEmpty()) {
                        withStyle(strike) { append(originalQty) }
                        append(" ")
                    }
                    append(product.qty())
                    append(" × ")
                    append(karikaPriceFormat(discountedPrice) + " KM")
                    append(" · rabat ")
                    append(product.rabat() + "%")
                },
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp,
                lineHeight = 17.sp
            )
            KarikaText(
                modifier = Modifier.padding(top = 2.dp),
                text = "Sa PDV ${karikaPriceFormat(rowTotalWithPdv)} KM · Provizija ${product.commissionPercent()}% (${karikaPriceFormat(commissionAmount)} KM)",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            KarikaText(
                text = karikaPriceFormat(rowTotalVpc) + " KM",
                color = KarikaUiColors.Ink,
                textSize = 14.5.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
            if (editable) {
                KarikaText(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onEdit)
                        .padding(horizontal = 4.dp, vertical = 4.dp),
                    text = "Izmijeni",
                    color = VendorAccent,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

// ── Comments ─────────────────────────────────────────────────────────────────────

private fun String.isImageFile() = lowercase().let {
    it.endsWith(".jpg") || it.endsWith(".jpeg") || it.endsWith(".png") ||
        it.endsWith(".gif") || it.endsWith(".webp")
}

@Composable
private fun CommentBubble(comment: Comment, customerName: String?, component: SalesOrderDetailComponent) {
    if (comment.isMine()) {
        val shape = RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 42.dp)
                    .clip(shape)
                    .background(VendorAccent)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                if (comment.message().isNotBlank()) {
                    HtmlTextWithStyles(
                        html = comment.message(),
                        textColor = KarikaColors.White
                    )
                }
                comment.files?.forEach { file ->
                    CommentFileAttachment(file = file, color = KarikaColors.White, component = component)
                }
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.White.copy(alpha = 0.8f),
                    textSize = 11.sp
                )
            }
        }
    } else {
        val shape = RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
        Row(modifier = Modifier.fillMaxWidth()) {
            KInitials(
                name = customerName,
                size = 32.dp,
                shape = CircleShape,
                background = VendorAccentSoft,
                color = VendorAccent,
                textSize = 11.sp
            )
            Spacer(Modifier.width(10.dp))
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(end = 32.dp)
                    .clip(shape)
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaUiColors.Line, shape)
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                if (comment.message().isNotBlank()) {
                    HtmlTextWithStyles(
                        html = comment.message(),
                        textColor = KarikaUiColors.Ink
                    )
                }
                comment.files?.forEach { file ->
                    CommentFileAttachment(file = file, color = VendorAccent, component = component)
                }
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = listOfNotNull(
                        customerName?.takeIf { it.isNotBlank() },
                        comment.createdAt().takeIf { it.isNotBlank() }
                    ).joinToString(" · "),
                    color = KarikaUiColors.Muted,
                    textSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CommentFileAttachment(file: File, color: Color, component: SalesOrderDetailComponent) {
    val url = file.url ?: return
    if ((file.name ?: url).isImageFile()) {
        KarikaImage(
            modifier = Modifier
                .padding(top = 8.dp)
                .widthIn(max = 200.dp)
                .clip(RoundedCornerShape(10.dp))
                .onClick { component.showImagePreview(imageUrl(url)) },
            model = imageUrl(url),
            contentScale = ContentScale.Inside
        )
    } else {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .onClick { component.downloadReceipt(file) },
            verticalAlignment = Alignment.CenterVertically
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_pdf), tint = color, size = 18.dp)
            Spacer(Modifier.width(8.dp))
            KarikaText(
                text = file.name ?: "",
                color = color,
                textSize = 12.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

// ── Izmjena stavke narudžbe ──────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditOrderItemModal(
    item: VendorProduct,
    canDiscount: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newQty: Int, newDiscount: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val discountText = remember {
        mutableStateOf(item.rabat().toIntOrNull()?.takeIf { it > 0 }?.toString() ?: "")
    }
    val qtyText = remember { mutableStateOf(item.qtyOrdered ?: "1") }
    val qtyValid = (qtyText.value.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Bottom) },
        dragHandle = { SheetHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column {
                KarikaText(
                    text = "Izmijeni stavku",
                    color = KarikaUiColors.Ink,
                    textSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = item.name ?: "—",
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            if (canDiscount) {
                OrderField(
                    modifier = Modifier.fillMaxWidth(),
                    value = discountText,
                    label = "Rabat (%)",
                    keyboardType = KeyboardType.Number,
                    allowedChars = KarikaConstants.numbers,
                    trailingText = "%",
                    onValueChange = { v ->
                        if ((v.toIntOrNull() ?: 0) > 100) discountText.value = "100"
                    }
                )
            } else if ((item.rabat().toIntOrNull() ?: 0) > 0) {
                KarikaText(
                    text = "Rabat: ${item.rabat()}%",
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
            }

            OrderField(
                modifier = Modifier.fillMaxWidth(),
                value = qtyText,
                label = "Količina",
                keyboardType = KeyboardType.Number,
                allowedChars = KarikaConstants.numbers
            )

            OrderModalButtons(
                modifier = Modifier.padding(top = 4.dp),
                primaryText = "Izmijeni",
                primaryEnabled = qtyValid,
                onSecondary = onDismiss,
                onPrimary = {
                    onConfirm(
                        qtyText.value.toIntOrNull() ?: (item.qtyOrdered?.toIntOrNull() ?: 1),
                        discountText.value.toIntOrNull() ?: 0
                    )
                }
            )
        }
    }
}

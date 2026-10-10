package karika.distribucija.ba.ui.view.distributer.orders.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KBackButton
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
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.orders.details.component.ApproveOrderModal
import karika.distribucija.ba.ui.view.distributer.orders.details.component.AttachBillModal
import karika.distribucija.ba.ui.view.distributer.orders.details.component.EditOrderSheet
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderField
import karika.distribucija.ba.ui.view.distributer.orders.details.component.RejectOrderModal
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
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

/** The whole order details screen (tests wait for it after opening an order). */
const val ORDER_DETAILS_TAG = "vendor_order_details"

/** Back button of the order details header. */
const val ORDER_DETAILS_BACK_TAG = "vendor_order_details_back"

/** Comment field and send button at the bottom of the order details. */
const val ORDER_COMMENT_FIELD_TAG = "vendor_order_comment_field"
const val ORDER_COMMENT_SEND_TAG = "vendor_order_comment_send"

private val NavyLabel = Color(0xFF9AA1B4)
private val AmberCard = Color(0xFFFFFBEB)
private val AmberBorder = Color(0xFFFCD34D)
private val AmberLine = Color(0xFFFDE68A)
private val AmberTitle = Color(0xFF92400E)

@Composable
fun OrderDetailsView(component: OrderDetailsComponent) {
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .testTag(ORDER_DETAILS_TAG)
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .hideKeyboard(true)
                .verticalScroll(scroll)
                .padding(bottom = 20.dp)
        ) {
            OrderHeader(component)
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                LockedNotice(component)
                OrderSummary(component)
                Customer(component)
                Specification(component)
                OrderShipping(component)
                Comments(component)
            }
        }
        CommentBar(component)
    }
}

/** White header that scrolls with the content: back, "Narudžba · date", "#number" and Akcije. */
@Composable
private fun OrderHeader(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KBackButton(modifier = Modifier.testTag(ORDER_DETAILS_BACK_TAG)) {
            component.dashBack()
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = "Narudžba · ${order.date()}",
                color = KarikaUiColors.Muted,
                textSize = 11.5.sp,
                lineHeight = 15.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1
            )
            KarikaText(
                text = "#${order.orderId ?: ""}",
                color = KarikaUiColors.Ink,
                textSize = 17.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
        }
        Spacer(Modifier.width(8.dp))
        OrderActions(component)
    }
}

/** Navy "Akcije" button with the order's actions and their modals. */
@Composable
private fun OrderActions(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val dropdownState = remember { mutableStateOf(false) }
    val approveModal = mutableStateOf(false).asState()
    val rejectModal = mutableStateOf(false).asState()
    val attachBillModal = remember { mutableStateOf(false) }
    val enabled = !order.locked() && !order.isRejected() && !order.isCancelled()

    Box {
        Row(
            modifier = Modifier
                .height(38.dp)
                .clip(RoundedCornerShape(50))
                .background(if (enabled) KarikaUiColors.Ink else KarikaUiColors.Ink.copy(alpha = 0.35f))
                .clickable(enabled = enabled) { dropdownState.negate() }
                .padding(start = 14.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                text = "Akcije",
                color = KarikaColors.White,
                textSize = 13.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            Spacer(Modifier.width(6.dp))
            KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_down), tint = KarikaColors.White, size = 16.dp)
        }
        if (dropdownState.value) {
            DropdownMenu(
                offset = DpOffset(x = 0.dp, y = 8.dp),
                shadowElevation = 10.dp,
                shape = RoundedCornerShape(14.dp),
                containerColor = KarikaColors.White,
                expanded = true,
                onDismissRequest = { dropdownState.negate() }
            ) {
                if (!order.isApproved() && !order.isRejected() && !order.isCancelled()) {
                    ActionItem("Odobri narudžbu", vectorResource(Res.drawable.ic_k_check), KarikaUiColors.Green) {
                        dropdownState.negate()
                        approveModal.negate()
                    }
                    ActionItem("Odbij narudžbu", vectorResource(Res.drawable.ic_k_close), KarikaUiColors.Red) {
                        dropdownState.negate()
                        rejectModal.negate()
                    }
                }
                if (!order.isApproved()) {
                    ActionItem("Generiši predračun", vectorResource(Res.drawable.ic_k_document), KarikaUiColors.Ink) {
                        dropdownState.negate()
                        component.createInvoice()
                    }
                }
                ActionItem("Printaj narudžbu", vectorResource(Res.drawable.ic_print), KarikaUiColors.Ink) {
                    dropdownState.negate()
                    component.getBill()
                }
                if (order.isPending()) {
                    ActionItem("Pošalji predračun", vectorResource(Res.drawable.ic_k_send), VendorAccent) {
                        dropdownState.negate()
                        attachBillModal.negate()
                    }
                }
            }
        }
    }

    if (approveModal.value) {
        ApproveOrderModal(
            value = component.getDelivery(),
            onCancel = {
                approveModal.negate()
            },
            onSubmit = { type, typeId, message ->
                approveModal.negate()
                component.approve(message, type, typeId != 0)
            }
        )
    }

    if (rejectModal.value) {
        RejectOrderModal(
            onCancel = {
                rejectModal.negate()
            },
            onSubmit = { message ->
                rejectModal.negate()
                component.reject(message)
            }
        )
    }

    if (attachBillModal.value) {
        AttachBillModal(
            component = component,
            onSubmit = { message, file ->
                component.estimate(message, file.second, file.first)
                attachBillModal.value = false
            },
            onCancel = {
                attachBillModal.value = false
            }
        )
    }
}

@Composable
private fun ActionItem(text: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
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

/** Shown while older orders have to be settled first; the order's data stays blurred. */
@Composable
private fun LockedNotice(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    if (!order.locked()) return
    Row(
        modifier = Modifier
            .padding(top = 16.dp)
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
            text = "Za prikaz detalja narudžbe, molimo Vas da zaključite prethodne narudžbe tako što ćete ih označiti kao odobrene ili odbijene!",
            color = KarikaUiColors.Amber,
            textSize = 13.5.sp,
            lineHeight = 19.sp,
            fontWeight = FontWeight.W600
        )
    }
}

/** Light status colors for the pill on the navy summary card. */
private fun statusOnNavy(status: String?): Color = when (status) {
    "approved" -> Color(0xFF86EFAC)
    "pending", "estimate-sent", "bill-sent" -> Color(0xFFFCD34D)
    "rejected" -> Color(0xFFFCA5A5)
    else -> Color(0xFFD1D5DB)
}

private fun Modifier.lockedBlur(order: VendorOrder) = blur(radius = if (order.locked()) 5.dp else 0.dp)

/** Navy card: total with PDV, status, total VPC and Karika's commission. */
@Composable
private fun OrderSummary(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    Column(
        modifier = Modifier
            .padding(top = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaUiColors.Ink)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
            modifier = Modifier.padding(top = 4.dp).lockedBlur(order),
            text = order.totalAmountWithPdv() + " KM",
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
                value = order.totalAmount() + " KM"
            )
            if (order.shopCommissionFee != null) {
                SummaryValue(
                    modifier = Modifier.weight(1f).lockedBlur(order),
                    label = "Karika provizija",
                    value = order.totalCommission(),
                    color = lerp(VendorAccent, KarikaColors.White, 0.45f)
                )
            }
        }
    }
}

@Composable
private fun SummaryValue(modifier: Modifier, label: String, value: String, color: Color = KarikaColors.White) {
    Column(modifier = modifier) {
        KarikaText(text = label, color = NavyLabel, textSize = 11.sp)
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

/** "Kupac": company and contact person, call/email buttons and the customer's data. */
@Composable
private fun Customer(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    KSectionTitle(
        modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        title = "Kupac"
    )
    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(
            modifier = Modifier.padding(14.dp).lockedBlur(order),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(
                name = order.b2bPravnoLice,
                size = 44.dp,
                shape = RoundedCornerShape(12.dp),
                background = VendorAccentSoft,
                color = VendorAccent,
                textSize = 14.sp
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = order.b2bPravnoLice ?: "-",
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
    val phone = order.telephone().takeIf { it.isNotBlank() && it != "-" && !order.locked() }
    val email = order.email().takeIf { it.isNotBlank() && it != "-" && !order.locked() }
    Row(
        modifier = Modifier.padding(top = 10.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ContactButton(
            modifier = Modifier.weight(1f),
            text = "Pozovi",
            icon = vectorResource(Res.drawable.ic_k_phone),
            enabled = phone != null
        ) {
            openPhoneCall(phone ?: "") { component.showMessage(it) }
        }
        ContactButton(
            modifier = Modifier.weight(1f),
            text = "Email",
            icon = vectorResource(Res.drawable.ic_k_mail),
            enabled = email != null
        ) {
            openEmail(email ?: "") { component.showMessage(it) }
        }
    }
    KKeyValueCard(
        modifier = Modifier.padding(top = 10.dp).lockedBlur(order),
        rows = listOf(
            "PDV broj" to (order.pdvNumber ?: "-"),
            "ID broj" to (order.idNumber ?: "-"),
            "Email" to order.email(),
            "Telefon" to order.telephone(),
            "Adresa za isporuku" to order.address()
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

private fun itemsLabel(count: Int): String {
    val mod10 = count % 10
    val mod100 = count % 100
    return when {
        mod10 == 1 && mod100 != 11 -> "$count artikal"
        mod10 in 2..4 && mod100 !in 12..14 -> "$count artikla"
        else -> "$count artikala"
    }
}

private fun VendorOrder.editable() = !locked() && !isCancelled() && !isRejected() && !isApproved()

/** "Specifikacija": the order's items with rabat and commission, and the order's totals. */
@Composable
private fun Specification(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val edit = component.editOrderItem.asState()

    Row(
        modifier = Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KSectionTitle(modifier = Modifier.weight(1f), title = "Specifikacija")
        KarikaText(
            text = itemsLabel(order.products.size),
            color = KarikaUiColors.Muted,
            textSize = 13.sp,
            fontWeight = FontWeight.W500
        )
    }
    KCard(modifier = Modifier.fillMaxWidth().lockedBlur(order), shape = RoundedCornerShape(14.dp)) {
        order.products.forEachIndexed { index, item ->
            if (index > 0) KDivider()
            SpecificationItem(item = item, editable = order.editable()) {
                edit.value = item
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF9FAFB))
        ) {
            KDivider()
            KKeyValueRow(label = "Ukupno VPC", value = order.totalAmount() + " KM")
            KDivider()
            KKeyValueRow(label = "Karika provizija", value = order.totalCommission())
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
                    text = order.totalAmountWithPdv() + " KM",
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
    if (edit.value != null) {
        EditOrderSheet(component)
    }
}

@Composable
private fun SpecificationItem(item: VendorProduct, editable: Boolean, onEdit: () -> Unit) {
    val strike = SpanStyle(textDecoration = TextDecoration.LineThrough, color = KarikaUiColors.Subtle)
    val image = (item.thumbnail ?: item.smallImage ?: item.image)
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
                text = item.name ?: "",
                color = KarikaUiColors.Ink,
                textSize = 14.5.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.W600
            )
            KarikaText(
                modifier = Modifier.padding(top = 3.dp),
                atext = buildAnnotatedString {
                    val originalQty = item.originalQty()
                    if (originalQty.isNotEmpty()) {
                        withStyle(strike) { append(originalQty) }
                        append(" ")
                    }
                    append(item.qty())
                    append(" × ")
                    append(item.priceVpc())
                    append(" · rabat ")
                    val originalRabat = item.originalRabat()
                    if (originalRabat.isNotEmpty()) {
                        withStyle(strike) { append("$originalRabat%") }
                        append(" ")
                    }
                    append(item.rabat() + "%")
                },
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp,
                lineHeight = 17.sp
            )
            KarikaText(
                modifier = Modifier.padding(top = 2.dp),
                text = "Sa PDV ${item.totalWithPdv()} · Provizija ${item.commissionPercent()}% (${item.commission()})",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            KarikaText(
                text = item.totalVpc(),
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

/** "Usluga dostave": amber card that opens the pickup address and the delivery calculator. */
@Composable
private fun OrderShipping(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val expand = mutableStateOf(false).asState()
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
                .clickable { expand.negate() }
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
                    text = if (expand.value) "Unesite adresu za utovar robe" else "Karika preuzima i dostavlja robu kupcu",
                    color = KarikaUiColors.Amber,
                    textSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
            KIcon(
                modifier = Modifier.rotate(if (expand.value) 180f else 0f),
                icon = vectorResource(Res.drawable.ic_k_chevron_down),
                tint = KarikaUiColors.Amber,
                size = 18.dp
            )
        }
        if (expand.value) {
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
                        value = component.contactName.asState(),
                        label = "Kontakt osoba",
                        required = true,
                        placeholder = "Kontakt osoba"
                    )
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactEmail.asState(),
                        label = "Email adresa",
                        required = true,
                        placeholder = "Email adresa",
                        keyboardType = KeyboardType.Email
                    )
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactPhone.asState(),
                        label = "Telefon",
                        required = true,
                        placeholder = "Telefon",
                        keyboardType = KeyboardType.Phone
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.contactCity.asState(),
                            label = "Grad",
                            required = true,
                            placeholder = "Grad"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.contactPostal.asState(),
                            label = "Poštanski broj",
                            required = true,
                            placeholder = "Poštanski broj",
                            keyboardType = KeyboardType.Number,
                            allowedChars = KarikaConstants.numbers
                        )
                    }
                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.contactAddress.asState(),
                        label = "Adresa",
                        required = true,
                        placeholder = "Adresa"
                    )

                    Column(modifier = Modifier.padding(top = 6.dp)) {
                        ShippingHeading("Kalkulator dostave")
                        KarikaText(
                            modifier = Modifier.padding(top = 4.dp),
                            text = "Unesite dimenzije paketa i izračunajte cijene za brzu dostavu, ukoliko odobrite paket u tom koraku ćete moći izabrati da li i koju opciju dostave želite",
                            color = KarikaUiColors.Muted,
                            textSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageWidth.asState(),
                            label = "Ukupna širina",
                            required = true,
                            placeholder = "Širina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "cm"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageHeight.asState(),
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
                            value = component.packageDepth.asState(),
                            label = "Ukupna dubina",
                            required = true,
                            placeholder = "Dubina",
                            keyboardType = KeyboardType.Number,
                            trailingText = "cm"
                        )
                        OrderField(
                            modifier = Modifier.weight(1f),
                            value = component.packageWeight.asState(),
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
                            price = component.a2b.asState().value
                        )
                        KDivider()
                        CourierRow(
                            logo = Res.drawable.img_express_post,
                            name = "EuroExpress",
                            price = component.express.asState().value
                        )
                    }

                    OrderField(
                        modifier = Modifier.fillMaxWidth(),
                        value = component.deliveryNotes.asState(),
                        label = "Napomena za dostavu",
                        placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                        singleLine = false,
                        minHeight = 66.dp
                    )

                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Izračunaj cijenu",
                        background = VendorAccent,
                        height = 50.dp,
                        enabled = !order.locked() && order.shouldShowShipping()
                    ) {
                        component.calculateShipping()
                    }
                }
            }
        }
    }
}

@Composable
private fun ShippingHeading(text: String) {
    KarikaText(
        text = text,
        color = KarikaUiColors.Ink,
        textSize = 14.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun CourierRow(logo: DrawableResource, name: String, price: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
                text = "Cijena dostave sa PDV: $price",
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp
            )
        }
    }
}

/** "Komentari": the comments between the supplier and the customer. */
@Composable
private fun Comments(component: OrderDetailsComponent) {
    val comments by component.comments.collectAsState()
    val order by component.order.collectAsState()
    if (comments.isEmpty()) return
    KSectionTitle(
        modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        title = "Komentari"
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        comments.forEach {
            CommentItem(it, component, order.b2bPravnoLice)
        }
    }
}

/** Comment field with the round send button, pinned to the bottom of the screen. */
@Composable
private fun CommentBar(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val comment = component.newComment.asState()
    val keyboardController = LocalSoftwareKeyboardController.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .background(KarikaColors.White)
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp)
    ) {
        if (order.commentsArchived()) {
            KarikaText(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                text = "Komentari narudžbe su arhivirani",
                color = KarikaUiColors.Muted,
                textSize = 14.sp,
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center
            )
            return@Box
        }
        val fieldEnabled = !order.locked() && !order.isCancelled()
        val canSend = comment.value.isNotEmpty() && !order.locked() && !order.isCancelled()
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                modifier = Modifier.weight(1f).testTag(ORDER_COMMENT_FIELD_TAG),
                value = comment.value,
                onValueChange = { if (!it.startsWith(" ")) comment.value = it },
                enabled = fieldEnabled,
                singleLine = true,
                textStyle = TextStyle(
                    color = if (fieldEnabled) KarikaUiColors.Ink else KarikaUiColors.Subtle,
                    fontSize = 14.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(VendorAccent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                decorationBox = { innerTextField ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(KarikaUiColors.Field)
                            .padding(horizontal = 16.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (comment.value.isEmpty()) {
                            KarikaText(
                                text = "Napiši komentar kupcu…",
                                color = KarikaUiColors.Subtle,
                                textSize = 14.sp,
                                maxLines = 1
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) VendorAccent else VendorAccent.copy(alpha = 0.4f))
                    .testTag(ORDER_COMMENT_SEND_TAG)
                    .clickable(enabled = canSend) {
                        keyboardController?.hide()
                        component.sendComment()
                    },
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_send), tint = KarikaColors.White, size = 20.dp)
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment, component: OrderDetailsComponent, customerName: String? = null) {
    if (comment.isMine()) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 42.dp)
                    .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 4.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(VendorAccent)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalAlignment = Alignment.End
            ) {
                HtmlTextWithStyles(
                    html = comment.message(),
                    textColor = KarikaColors.White
                )
                CommentFiles(comment, component, KarikaColors.White)
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.White.copy(alpha = 0.8f),
                    textSize = 11.sp
                )
            }
        }
    } else {
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
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp))
                    .background(KarikaColors.White)
                    .border(
                        1.dp,
                        KarikaUiColors.Line,
                        RoundedCornerShape(topStart = 4.dp, topEnd = 14.dp, bottomStart = 14.dp, bottomEnd = 14.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 10.dp)
            ) {
                HtmlTextWithStyles(
                    html = comment.message(),
                    textColor = KarikaUiColors.Ink
                )
                CommentFiles(comment, component, VendorAccent)
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = listOfNotNull(customerName?.takeIf { it.isNotBlank() }, comment.createdAt().takeIf { it.isNotBlank() })
                        .joinToString(" · "),
                    color = KarikaUiColors.Muted,
                    textSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun CommentFiles(comment: Comment, component: OrderDetailsComponent, color: Color) {
    comment.files?.forEach {
        if (it.type?.startsWith("image") == true) {
            KarikaImage(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .width(150.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .clickable {
                        component.showImagePreview(imageUrl(it.url ?: ""))
                    },
                model = imageUrl(it.url ?: ""),
                contentScale = ContentScale.Inside
            )
        } else {
            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .clickable {
                        component.downloadReceipt(it)
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_pdf), tint = color, size = 20.dp)
                Spacer(modifier = Modifier.width(8.dp))
                KarikaText(
                    text = it.name ?: "",
                    fontWeight = FontWeight.Bold,
                    textSize = 12.sp,
                    color = color
                )
            }
        }
    }
}

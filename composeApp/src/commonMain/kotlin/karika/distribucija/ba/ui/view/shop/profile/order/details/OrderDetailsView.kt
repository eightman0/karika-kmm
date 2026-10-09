package karika.distribucija.ba.ui.view.shop.profile.order.details

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrderProduct
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImagePlaceholder
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KKeyValueCard
import karika.distribucija.ba.ui.components.KKeyValueRow
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.shop.profile.order.components.AttachBillModal
import karika.distribucija.ba.ui.view.shop.profile.order.components.CancelOrderModal
import karika.distribucija.ba.ui.view.shop.profile.order.orderStatusColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_document
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrderDetailsView(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()

    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(
                    title = "#${order.incrementId}",
                    overline = order.date().takeIf { it.isNotBlank() }?.let { "Narudžba · $it" } ?: "Narudžba",
                    onBack = { component.appBack() }
                )
            }
        },
        bottomBar = {
            Box(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                KBottomPanel {
                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Naruči ponovo"
                    ) {
                        component.orderAgain(order)
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(KarikaUiColors.Page)
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            OrderCommon(component)
        }
    }
}

@Composable
private fun OrderCommon(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        SummaryCard(order)
        order.shippingAddress?.let { address ->
            KSectionTitle(
                modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
                title = "Dostava"
            )
            ShippingAddress(address)
        }
        VendorOrder(order, component)
    }
}

/** Light status colors for the pill on the navy summary card. */
private fun statusOnNavy(status: String?): Color = when (status) {
    "approved" -> Color(0xFF86EFAC)
    "pending" -> Color(0xFFFCD34D)
    "cancelled", "rejected" -> Color(0xFFFCA5A5)
    else -> Color(0xFFD1D5DB)
}

@Composable
private fun SummaryCard(order: OrdersResponse) {
    val statuses = order.orders.map { it.status }.distinct()
    Column(
        modifier = Modifier
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
                color = Color(0xFF9AA1B4),
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
            if (statuses.size == 1) {
                val first = order.orders.first()
                val color = statusOnNavy(first.status)
                KPill(
                    text = first.status(),
                    background = color.copy(alpha = 0.15f),
                    color = color,
                    dot = color,
                    textSize = 11.5.sp
                )
            }
        }
        KarikaText(
            modifier = Modifier.padding(top = 4.dp),
            text = order.vpcPdvString(),
            color = KarikaColors.White,
            textSize = 28.sp,
            lineHeight = 34.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        HorizontalDivider(
            modifier = Modifier.padding(top = 14.dp, bottom = 12.dp),
            thickness = 1.dp,
            color = KarikaColors.White.copy(alpha = 0.12f)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SummaryValue(modifier = Modifier.weight(1f), label = "Ukupna VPC", value = order.vpcString())
            SummaryValue(modifier = Modifier.weight(1f), label = "PDV 17%", value = order.pdvString())
        }
        if ((order.bonus ?: 0.0) > 0.0) {
            Spacer(Modifier.height(10.dp))
            SummaryValue(modifier = Modifier.fillMaxWidth(), label = "Ostvareni bonus", value = order.bonus())
        }
    }
}

@Composable
private fun SummaryValue(modifier: Modifier, label: String, value: String) {
    Column(modifier = modifier) {
        KarikaText(
            text = label,
            color = Color(0xFF9AA1B4),
            textSize = 11.sp
        )
        KarikaText(
            modifier = Modifier.padding(top = 2.dp),
            text = value,
            color = KarikaColors.White,
            textSize = 14.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

@Composable
fun VendorOrder(order: OrdersResponse, component: OrderDetailsComponent) {
    val cancelModal = remember { mutableStateOf<Order?>(null) }
    val attachBillModal = remember { mutableStateOf<Order?>(null) }

    KSectionTitle(
        modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        title = "Artikli"
    )
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        order.orders.forEach {
            VendorCard(
                order = it,
                component = component,
                onCancel = { cancelModal.value = it },
                onAttachBill = { attachBillModal.value = it }
            )
        }
    }

    if (cancelModal.value != null) {
        CancelOrderModal(
            onSubmit = { reason, com ->
                component.cancelOrder(
                    cancelModal.value?.orderId,
                    cancelModal.value?.vendorId.toString(),
                    reason,
                    com
                )
                cancelModal.value = null
            },
            onCancel = {
                cancelModal.value = null
            }
        )
    }

    if (attachBillModal.value != null) {
        AttachBillModal(
            component = component,
            onSubmit = { message, file ->
                component.attachBill(
                    attachBillModal.value,
                    message,
                    file
                )
                attachBillModal.value = null
            },
            onCancel = {
                attachBillModal.value = null
            }
        )
    }
}

@Composable
private fun VendorCard(
    order: Order,
    component: OrderDetailsComponent,
    onCancel: () -> Unit,
    onAttachBill: () -> Unit,
) {
    val rabats = order.products.map { it.rabat() }.distinct()
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    component.showVendor(
                        Vendor(
                            entityId = order.vendorId ?: 0,
                            publicName = order.vendorName
                        )
                    )
                }
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(name = order.vendorName, size = 28.dp, textSize = 9.sp)
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = order.vendorName,
                color = KarikaUiColors.Ink,
                textSize = 13.5.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            if (rabats.size == 1) {
                KarikaText(
                    text = "Rabat ${rabats.first()}%",
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
        KDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Status",
                color = KarikaUiColors.Muted,
                textSize = 13.sp
            )
            val (background, color, dot) = orderStatusColors(order.status)
            KPill(text = order.status(), background = background, color = color, dot = dot, textSize = 11.5.sp)
        }
        KKeyValueRow(label = "Ukupno VPC", value = order.vpcString())
        KKeyValueRow(label = "Ukupno sa PDV", value = order.vpcPdvString())
        order.products.forEach { product ->
            KDivider()
            ProductRow(product)
        }
        if (order.showAddBill()) {
            KDivider()
            KTonalButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                text = "Pošalji uplatnicu",
                icon = vectorResource(Res.drawable.ic_k_document),
                onClick = onAttachBill
            )
        }
        KDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(44.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable {
                        if (!order.canceled()) {
                            onCancel()
                        }
                    },
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Otkaži narudžbu",
                    color = if (order.canceled()) Color(0xFFC4C9D0) else KarikaUiColors.Pink,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
            VerticalDivider(thickness = 1.dp, color = KarikaUiColors.Line)
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxSize()
                    .clickable {
                        if (order.commentsArchived()) {
                            component.showWarningMessage("Komentari narudžbe su arhivirani.")
                        } else {
                            component.navigateToComments(order)
                        }
                    },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_chat),
                    tint = KarikaUiColors.Ink,
                    size = 16.dp
                )
                Spacer(Modifier.width(6.dp))
                KarikaText(
                    text = "Komentari (${order.commentCount})",
                    color = KarikaUiColors.Ink,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ProductRow(product: OrderProduct) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        KImagePlaceholder(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(10.dp))
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = product.name,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W600,
                maxLines = 3
            )
            KarikaText(
                modifier = Modifier.padding(top = 3.dp),
                text = "${product.qty()} × ${product.vpc()}",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp
            )
            if (product.qtyChanged()) {
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = "Naručeno ${product.originalQty()}",
                    color = KarikaUiColors.Subtle,
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    decoration = TextDecoration.LineThrough
                )
            }
            if (product.rabat() != "0") {
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = "Rabat ${product.rabat()}%",
                    color = KarikaUiColors.Green,
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
        Spacer(Modifier.width(8.dp))
        KarikaText(
            text = product.total(),
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
    }
}

@Composable
private fun ShippingAddress(address: Address) {
    val street = address.street.joinToString(" ")
    KKeyValueCard(
        rows = listOf(
            "Kontakt osoba" to listOfNotNull(address.firstname, address.lastname).joinToString(" "),
            "Telefon" to address.telephone,
            "Adresa" to listOf(street, address.city.orEmpty())
                .filter { it.isNotBlank() }
                .joinToString(", ")
        )
    )
}

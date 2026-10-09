package karika.distribucija.ba.ui.view.shop.profile.order

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.shop.profile.order.components.AttachBillModal
import karika.distribucija.ba.ui.view.shop.profile.order.components.CancelOrderModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_gift
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrdersView(component: OrdersComponent) {
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
                KBackHeader(title = "Moje narudžbe", onBack = { component.appBack() })
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(KarikaUiColors.Page)
        ) {
            FilterView(component)
            Orders(component)
        }
    }
}

private val statusOptions = listOf(
    "Sve" to "",
    "Na čekanju" to "pending",
    "Odobrena" to "approved",
    "Otkazana" to "cancelled",
    "Odbijena" to "rejected",
    "Čekanje na uplatu" to "estimate-sent",
    "Uplaćena" to "bill-sent"
)

@Composable
private fun FilterView(component: OrdersComponent) {
    val statusSort = mutableStateOf(
        statusOptions.firstOrNull { it.second == component.status }?.first ?: "Sve"
    ).asState()
    val dateSort = mutableStateOf(
        if (component.sortDirection == "ASC") "Najstarije" else "Najnovije"
    ).asState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        DropdownChip(
            value = statusSort.value,
            values = statusOptions.map { it.first }
        ) { selected ->
            statusSort.value = selected
            component.status = statusOptions.firstOrNull { it.first == selected }?.second ?: ""
            component.loadNextPage(reset = true)
        }
        DropdownChip(
            value = dateSort.value,
            values = listOf("Najnovije", "Najstarije")
        ) { selected ->
            dateSort.value = selected
            component.sortDirection = if (selected == "Najnovije") "DESC" else "ASC"
            component.loadNextPage(reset = true)
        }
    }
}

/** White chip with a chevron that opens a menu of [values]. */
@Composable
private fun DropdownChip(
    value: String,
    values: List<String>,
    onSelect: (String) -> Unit,
) {
    val expanded = remember { mutableStateOf(false) }
    Box {
        KChip(
            text = value,
            selected = false,
            trailingIcon = vectorResource(Res.drawable.ic_k_chevron_down),
            onClick = { expanded.value = true }
        )
        DropdownMenu(
            modifier = Modifier.background(KarikaColors.White),
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            values.forEach { option ->
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = option,
                            color = KarikaUiColors.Ink,
                            textSize = 14.sp,
                            fontWeight = if (option == value) FontWeight.W700 else FontWeight.W500
                        )
                    },
                    onClick = {
                        expanded.value = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

@Composable
private fun Orders(component: OrdersComponent) {
    val state = rememberLazyListState()
    val items by component.orders.collectAsState()
    val shouldScrollToTop by component.shouldScrollToTop.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize(),
        state = state,
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(items = items, key = { it.orderId }) {
            OrderItem(it, component)
        }
        item { EmptyState(component) }
        item { Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars)) }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    LaunchedEffect(shouldScrollToTop) {
        if (component.shouldScrollToTop.value) {
            state.scrollToItem(0)
            component.scrollHandled()
        }
    }
}

/** Status pill colors: approved green, pending amber, cancelled/rejected red, others gray. */
internal fun orderStatusColors(status: String?): Triple<Color, Color, Color> = when (status) {
    "approved" -> Triple(KarikaUiColors.GreenSoft, KarikaUiColors.Green, KarikaUiColors.Green)
    "pending" -> Triple(KarikaUiColors.AmberSoft, KarikaUiColors.Amber, KarikaUiColors.Amber)
    "cancelled", "rejected" -> Triple(KarikaUiColors.RedSoft, KarikaUiColors.Red, KarikaUiColors.Red)
    else -> Triple(KarikaUiColors.Field, KarikaUiColors.Muted, KarikaUiColors.Muted)
}

@Composable
private fun StatusPill(order: Order) {
    val (background, color, dot) = orderStatusColors(order.status)
    KPill(text = order.status(), background = background, color = color, dot = dot, textSize = 11.5.sp)
}

@Composable
private fun OrderItem(order: OrdersResponse, component: OrdersComponent) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { component.navigateDetails(order) }
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    KarikaText(
                        text = "#${order.incrementId}",
                        color = KarikaUiColors.Ink,
                        textSize = 14.5.sp,
                        lineHeight = 18.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    KarikaText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = order.date(),
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
                val statuses = order.orders.map { it.status }.distinct()
                if (statuses.size == 1) {
                    Spacer(Modifier.width(10.dp))
                    StatusPill(order.orders.first())
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    KarikaText(
                        text = "Ukupno sa PDV",
                        color = KarikaUiColors.Muted,
                        textSize = 11.5.sp,
                        fontWeight = FontWeight.W500
                    )
                    KarikaText(
                        modifier = Modifier.padding(top = 2.dp),
                        text = order.vpcPdvString(),
                        color = KarikaUiColors.Ink,
                        textSize = 22.sp,
                        lineHeight = 26.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    KarikaText(
                        text = "VPC ${order.vpcString()}",
                        color = KarikaUiColors.Muted,
                        textSize = 11.5.sp,
                        maxLines = 1
                    )
                    Row(
                        modifier = Modifier.padding(top = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KIcon(
                            icon = vectorResource(Res.drawable.ic_k_gift),
                            tint = KarikaUiColors.Green,
                            size = 13.dp
                        )
                        Spacer(Modifier.width(4.dp))
                        KarikaText(
                            text = "Bonus ${order.bonus()}",
                            color = KarikaUiColors.Green,
                            textSize = 11.5.sp,
                            fontWeight = FontWeight.W500,
                            maxLines = 1
                        )
                    }
                }
            }
        }
        order.orders.forEach {
            KDivider()
            VendorItem(it, component)
        }
        KDivider()
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KSecondaryButton(
                modifier = Modifier.weight(1f),
                text = "Vidi narudžbu",
                height = 42.dp
            ) {
                component.navigateDetails(order)
            }
            KPrimaryButton(
                modifier = Modifier.weight(1f),
                text = "Naruči ponovo",
                height = 42.dp
            ) {
                component.orderAgain(order)
            }
        }
    }
}

@Composable
private fun VendorItem(order: Order, component: OrdersComponent) {
    val cancelModal = remember { mutableStateOf<Order?>(null) }
    val attachBillModal = remember { mutableStateOf<Order?>(null) }
    val showAdditionalOptions = mutableStateOf(false).asState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KarikaUiColors.Page)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showAdditionalOptions.negate() }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(name = order.vendorName, size = 28.dp, textSize = 9.sp)
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = order.vendorName,
                color = KarikaUiColors.Ink,
                textSize = 13.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1
            )
            KIcon(
                modifier = Modifier.rotate(if (showAdditionalOptions.value) 180f else 0f),
                icon = vectorResource(Res.drawable.ic_k_chevron_down),
                tint = KarikaUiColors.Muted,
                size = 18.dp
            )
        }
        if (showAdditionalOptions.value) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 14.dp, end = 14.dp, bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        modifier = Modifier
                            .weight(1f)
                            .clickable {
                                component.showVendor(
                                    Vendor(
                                        entityId = order.vendorId ?: 0,
                                        publicName = order.vendorName
                                    )
                                )
                            },
                        text = "Profil dobavljača",
                        color = KarikaUiColors.Pink,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W600,
                        decoration = TextDecoration.Underline
                    )
                    StatusPill(order)
                }
                Spacer(Modifier.height(10.dp))
                VendorValueRow(label = "Ukupno VPC", value = order.vpcString())
                VendorValueRow(label = "Ukupno sa PDV", value = order.vpcPdvString())
                Spacer(Modifier.height(10.dp))
                KCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
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
                                        cancelModal.value = order
                                    }
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                text = "Otkaži narudžbu",
                                color = if (order.canceled()) KarikaUiColors.Subtle else KarikaUiColors.Pink,
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
                if (order.showAddBill()) {
                    Spacer(Modifier.height(8.dp))
                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Pošalji uplatnicu",
                        height = 42.dp,
                        icon = vectorResource(Res.drawable.ic_k_document)
                    ) {
                        attachBillModal.value = order
                    }
                }
            }
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
private fun VendorValueRow(label: String, value: String) {
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
            textSize = 12.5.sp
        )
        KarikaText(
            text = value,
            color = KarikaUiColors.Ink,
            textSize = 13.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun EmptyState(component: OrdersComponent) {
    val vendors by component.orders.collectAsState()
    if (vendors.isNotEmpty()) {
        return
    }
    KEmptyState(
        modifier = Modifier.height(200.dp),
        text = if (component.status.isEmpty()) "Nema narudžbi" else "Nema narudžbi za izabrani status."
    )
}

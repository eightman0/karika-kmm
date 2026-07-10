package karika.distribucija.ba.ui.view.salesrep.orders

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_calendar
import karikav2.composeapp.generated.resources.ic_check_circle_filled
import karikav2.composeapp.generated.resources.ic_filter_alt
import karikav2.composeapp.generated.resources.ic_orders
import karikav2.composeapp.generated.resources.ic_search
import karikav2.composeapp.generated.resources.ic_storefront
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

private val orderStatusOptions = listOf(
    "pending" to "Na čekanju",
    "processing" to "U obradi",
    "approved" to "Odobreno",
    "bill-sent" to "Uplaćeno",
    "estimate-sent" to "Čekanje na uplatu",
    "rejected" to "Odbijeno",
    "cancelled" to "Otkazano"
)

private fun statusBg(status: String): Color = when (status) {
    "approved" -> KarikaColors.Green4
    "rejected" -> KarikaColors.Red2
    "cancelled" -> KarikaColors.Gray5
    "pending",
    "processing" -> Color(0xFFE8F0FD)

    "bill-sent",
    "estimate-sent" -> Color(0xFFFFF0E8)

    else -> KarikaColors.Gray5
}

private fun statusColor(status: String): Color = when (status) {
    "approved" -> KarikaColors.Green3
    "rejected" -> KarikaColors.Error
    "cancelled" -> KarikaColors.Gray6
    "pending",
    "processing" -> KarikaColors.Blue

    "bill-sent",
    "estimate-sent" -> KarikaColors.Orange

    else -> KarikaColors.Gray6
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrdersView(component: SalesOrdersComponent) {
    var showStatusSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val orders by component.orders.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val searchText by component.searchQuery.collectAsState()
    val selectedStatus by component.statusFilter.collectAsState()

    Column(
        modifier = Modifier
            .background(KarikaColors.Gray20)
            .fillMaxSize()
    ) {
        // ── Search + Filteri bar ───────────────────────────────────────────────
        /*    Row(
                modifier = Modifier
                    .padding(16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(KarikaColors.White)
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_search),
                    contentDescription = "",
                    tint = KarikaColors.Gray7,
                    modifier = Modifier.size(20.dp)
                )

                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (searchText.isEmpty()) {
                        KarikaText(
                            text = "Pretraži narudžbe...",
                            color = KarikaColors.Gray8,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                    BasicTextField(
                        value = searchText,
                        onValueChange = { component.setSearch(it) },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp,
                            fontFamily = karikaFonts()
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // Status filter chip
                val statusLabel =
                    orderStatusOptions.firstOrNull { it.first == selectedStatus }?.second ?: "Filteri"
                val isFiltered = selectedStatus != null
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isFiltered) KarikaColors.Blue else KarikaColors.Gray10)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { showStatusSheet = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_filter_alt),
                        contentDescription = "",
                        tint = if (isFiltered) KarikaColors.White else KarikaColors.Gray2,
                        modifier = Modifier.size(14.dp)
                    )
                    KarikaText(
                        text = statusLabel,
                        color = if (isFiltered) KarikaColors.White else KarikaColors.Gray2,
                        textSize = 12.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
    */
        YSpacer16()

        // ── Orders list ────────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items = orders, key = { it.orderId }) { order ->
                OrderCard(order = order, onClick = { component.openOrder(order) })
            }

            // Empty state
            if (orders.isEmpty() && !isLoadingMore) {
                item {
                    val hasSearch = searchText.isNotBlank()
                    val hasStatus = selectedStatus != null
                    val statusName =
                        orderStatusOptions.firstOrNull { it.first == selectedStatus }?.second

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(KarikaColors.Gray9),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vectorResource(
                                    if (hasSearch) Res.drawable.ic_search else Res.drawable.ic_filter_alt
                                ),
                                contentDescription = "",
                                tint = KarikaColors.Gray6,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        KarikaText(
                            text = when {
                                hasSearch && hasStatus -> "Nema narudžbi za „$searchText sa statusom „$statusName„"
                                hasSearch -> "Nema narudžbi za „$searchText„"
                                hasStatus -> "Nema narudžbi sa statusom „$statusName„"
                                else -> "Nema narudžbi"
                            },
                            color = KarikaColors.Gray2,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W600,
                            textAlign = TextAlign.Center
                        )

                        if (hasSearch || hasStatus) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.Gray10)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) {
                                        if (hasSearch) component.setSearch("")
                                        if (hasStatus) component.setStatus(null)
                                    }
                                    .padding(horizontal = 20.dp, vertical = 10.dp)
                            ) {
                                KarikaText(
                                    text = "Poništi",
                                    color = KarikaColors.Gray2,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                }
            }

            // Load more
            if (component.hasMore) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = KarikaColors.Blue,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.White)
                                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { component.loadNextPage() }
                                    .padding(horizontal = 24.dp, vertical = 10.dp)
                            ) {
                                KarikaText(
                                    text = "Učitaj više",
                                    color = KarikaColors.Gray2,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // ── Status bottom sheet ────────────────────────────────────────────────────
    if (showStatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatusSheet = false },
            sheetState = sheetState,
            containerColor = KarikaColors.White
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                KarikaText(
                    text = "Filtriraj po statusu",
                    color = KarikaColors.Gray2,
                    textSize = 17.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // "Svi statusi" option
                StatusSheetRow(
                    label = "Svi statusi",
                    selected = selectedStatus == null
                ) {
                    coroutineScope.launch {
                        sheetState.hide()
                        showStatusSheet = false
                        component.setStatus(null)
                    }
                }

                HorizontalDivider(color = KarikaColors.Gray9)

                orderStatusOptions.forEach { (value, label) ->
                    StatusSheetRow(
                        label = label,
                        selected = selectedStatus == value
                    ) {
                        coroutineScope.launch {
                            sheetState.hide()
                            showStatusSheet = false
                            component.setStatus(value)
                        }
                    }
                    HorizontalDivider(color = KarikaColors.Gray9)
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ── Order card ────────────────────────────────────────────────────────────────

@Composable
private fun OrderCard(order: OnBehalfOrder, onClick: () -> Unit = {}) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(16.dp)
    ) {
        // ── Top: customer name + status badge ──────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_storefront),
                        contentDescription = "",
                        tint = KarikaColors.Blue,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    KarikaText(
                        text = order.displayName(),
                        color = KarikaColors.Gray2,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1,
                        textOverflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = "#${order.incrementId}",
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }

            Spacer(Modifier.width(8.dp))

            // Status badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusBg(order.status))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                KarikaText(
                    text = order.statusLabel().uppercase(),
                    color = statusColor(order.status),
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }

        YSpacer16()
        HorizontalDivider(color = KarikaColors.Gray9)
        YSpacer8()

        // ── Bottom: total + date ───────────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Grand total
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_orders),
                    contentDescription = "",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                KarikaText(
                    text = order.totalString(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
            }

            // Date
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_calendar),
                    contentDescription = "",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                KarikaText(
                    text = order.date(),
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }
        }
    }
}

// ── Status sheet row ──────────────────────────────────────────────────────────

@Composable
private fun StatusSheetRow(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KarikaText(
            text = label,
            color = if (selected) KarikaColors.Blue else KarikaColors.Gray2,
            textSize = 15.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500
        )
        if (selected) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_check_circle_filled),
                contentDescription = "",
                tint = KarikaColors.Blue,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

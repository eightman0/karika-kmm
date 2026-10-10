package karika.distribucija.ba.ui.view.salesrep.orders

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
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfOrder
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KChipRow
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KSquareIconButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.orders.SheetHandle
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_filter
import karikav2.composeapp.generated.resources.ic_k_search
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

/** Background and text/dot color of an order's status pill. */
private fun statusPillColors(status: String): Pair<Color, Color> = when (status) {
    "approved", "bill-sent" -> KarikaUiColors.GreenSoft to KarikaUiColors.Green
    "rejected" -> KarikaUiColors.RedSoft to KarikaUiColors.Red
    "cancelled" -> KarikaUiColors.Field to KarikaUiColors.Muted
    "processing" -> VendorAccentSoft to VendorAccent
    "pending", "estimate-sent" -> KarikaUiColors.AmberSoft to KarikaUiColors.Amber
    else -> KarikaUiColors.Field to KarikaUiColors.Muted
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
            .background(KarikaUiColors.Page)
            .fillMaxSize()
    ) {
        // Search and the status filter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OrderSearchField(
                modifier = Modifier.weight(1f),
                value = searchText,
                onValueChange = { component.setSearch(it) }
            )
            KSquareIconButton(
                icon = vectorResource(Res.drawable.ic_k_filter),
                background = if (selectedStatus != null) VendorAccent else KarikaUiColors.Ink,
                onClick = { showStatusSheet = true }
            )
        }

        // Status chips: the same filter as the sheet
        KChipRow(modifier = Modifier.padding(top = 12.dp)) {
            KChip(
                text = "Sve",
                selected = selectedStatus == null,
                selectedColor = VendorAccent,
                onClick = { if (selectedStatus != null) component.setStatus(null) }
            )
            orderStatusOptions.forEach { (value, label) ->
                KChip(
                    text = label,
                    selected = selectedStatus == value,
                    selectedColor = VendorAccent,
                    onClick = { if (selectedStatus != value) component.setStatus(value) }
                )
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(VendorAccentSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            KIcon(
                                icon = vectorResource(
                                    if (hasSearch) Res.drawable.ic_k_search else Res.drawable.ic_k_filter
                                ),
                                tint = VendorAccent,
                                size = 26.dp
                            )
                        }

                        KarikaText(
                            text = when {
                                hasSearch && hasStatus -> "Nema narudžbi za „$searchText“ sa statusom „$statusName“"
                                hasSearch -> "Nema narudžbi za „$searchText“"
                                hasStatus -> "Nema narudžbi sa statusom „$statusName“"
                                else -> "Nema narudžbi"
                            },
                            color = KarikaUiColors.Ink,
                            textSize = 16.sp,
                            lineHeight = 22.sp,
                            fontWeight = FontWeight.W700,
                            textAlign = TextAlign.Center
                        )

                        if (hasSearch || hasStatus) {
                            KSecondaryButton(
                                text = "Poništi",
                                height = 44.dp,
                                onClick = {
                                    if (hasSearch) component.setSearch("")
                                    if (hasStatus) component.setStatus(null)
                                }
                            )
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
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isLoadingMore) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(28.dp),
                                color = VendorAccent,
                                strokeWidth = 2.dp
                            )
                        } else {
                            KSecondaryButton(
                                text = "Učitaj više",
                                height = 44.dp,
                                textColor = VendorAccent,
                                onClick = { component.loadNextPage() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Status bottom sheet
    if (showStatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatusSheet = false },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Bottom) },
            dragHandle = { SheetHandle() }
        ) {
            Column(modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 20.dp)) {
                KarikaText(
                    text = "Filtriraj po statusu",
                    color = KarikaUiColors.Ink,
                    textSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(bottom = 10.dp)
                )

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

                orderStatusOptions.forEach { (value, label) ->
                    KDivider()
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
                }
            }
        }
    }
}

/** White search field on the gray page (the kit's field is gray, which disappears on it). */
@Composable
private fun OrderSearchField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    val keyboard = LocalSoftwareKeyboardController.current
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(46.dp)
            .clip(shape)
            .background(KarikaColors.White)
            .border(1.dp, KarikaUiColors.Line, shape)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_search), tint = KarikaUiColors.Muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                KarikaText(text = "Pretraži narudžbe…", color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
            }
            BasicTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = KarikaUiColors.Ink,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(VendorAccent),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { keyboard?.hide() })
            )
        }
    }
}

// ── Order card ────────────────────────────────────────────────────────────────

@Composable
private fun OrderCard(order: OnBehalfOrder, onClick: () -> Unit = {}) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KInitials(
                    name = order.displayName(),
                    size = 40.dp,
                    shape = RoundedCornerShape(12.dp),
                    background = VendorAccentSoft,
                    color = VendorAccent,
                    textSize = 13.sp
                )
                Column(modifier = Modifier.weight(1f)) {
                    KarikaText(
                        text = order.displayName(),
                        color = KarikaUiColors.Ink,
                        textSize = 15.sp,
                        lineHeight = 19.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                    KarikaText(
                        text = order.date(),
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        maxLines = 1
                    )
                }
                KarikaText(
                    text = order.totalString(),
                    color = KarikaUiColors.Ink,
                    textSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
            KDivider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "#${order.incrementId}",
                    color = KarikaUiColors.Muted,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1
                )
                val (background, color) = statusPillColors(order.status)
                KPill(
                    text = order.statusLabel(),
                    background = background,
                    color = color,
                    dot = color,
                    textSize = 11.5.sp
                )
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_chevron_right),
                    tint = KarikaUiColors.Subtle,
                    size = 16.dp
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
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier.weight(1f),
            text = label,
            color = if (selected) VendorAccent else KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W500
        )
        if (selected) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(VendorAccent),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = KarikaColors.White, size = 14.dp)
            }
        }
    }
}

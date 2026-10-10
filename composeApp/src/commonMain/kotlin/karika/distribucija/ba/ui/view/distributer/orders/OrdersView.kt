package karika.distribucija.ba.ui.view.distributer.orders

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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KSquareIconButton
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_filter
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import org.jetbrains.compose.resources.vectorResource

/** Test tags of the orders screen, for the end-to-end tests. */
const val MIN_ORDER_TAG = "min_order"
const val MIN_ORDER_FIELD_TAG = "min_order_field"
const val ORDER_SEARCH_TAG = "order_search"
const val ORDER_FILTER_TAG = "order_filter"
fun vendorOrderTag(order: VendorOrder) = "vendor_order_${order.orderId}"

/** Amounts offered under the minimum order field; they only fill the field. */
private val QUICK_MIN_AMOUNTS = listOf(50, 100, 150, 200)

@OptIn(FlowPreview::class)
@Composable
fun OrdersView(component: OrdersComponent) {
    val state = rememberLazyListState()
    val items by component.orders.collectAsState()
    val minOrderAmount by component.minOrderValue.asState()
    val searchText = component.searchText.asState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        // Continues the shell's header: search and filters
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(KarikaHeaderShape)
                .background(KarikaColors.White)
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KSearchField(
                cursorColor = VendorAccent,
                modifier = Modifier
                    .testTag(ORDER_SEARCH_TAG)
                    .weight(1f),
                value = searchText.value,
                onValueChange = { searchText.value = it },
                placeholder = "Pretraži narudžbe…",
                onSearch = { component.filter() }
            )
            KSquareIconButton(
                modifier = Modifier.testTag(ORDER_FILTER_TAG),
                icon = vectorResource(Res.drawable.ic_k_filter),
                background = if (component.hasFilter()) VendorAccent else KarikaUiColors.Ink,
                onClick = { component.showFilterState.negate() }
            )
        }

        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = state,
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    MinOrderCard(
                        amount = minOrderAmount,
                        onChange = { component.showMinOrderModal.value = true }
                    )
                }
                if (component.hasFilter()) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                modifier = Modifier.weight(1f),
                                text = "Prikazane su filtrirane narudžbe",
                                color = KarikaUiColors.Muted,
                                textSize = 13.sp
                            )
                            KarikaText(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { component.clear() }
                                    .padding(horizontal = 6.dp, vertical = 4.dp),
                                text = "Očisti",
                                color = VendorAccent,
                                textSize = 14.sp,
                                fontWeight = FontWeight.W700
                            )
                        }
                    }
                }
                items(items = items.toList()) {
                    OrderItem(component, it)
                }
                if (items.isEmpty()) {
                    item {
                        KEmptyState(text = "Nema narudžbi.", modifier = Modifier.padding(top = 24.dp))
                    }
                }
            }
        }
    }

    OrderFilterSheet(component)

    if (component.showMinOrderModal.value) {
        MinOrderSheet(component)
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    LaunchedEffect(Unit) {
        component.loadNextPage(true)
    }

    // Searches as the supplier types, as the old search box did
    LaunchedEffect(Unit) {
        snapshotFlow { searchText.value }
            .drop(1)
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { component.filter() }
    }
}

@Composable
private fun MinOrderCard(amount: String, onChange: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaUiColors.Ink)
    ) {
        // Decorative circle; matchParentSize keeps it from setting the card's height
        Box(modifier = Modifier.matchParentSize()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 24.dp, y = 30.dp)
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.06f))
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Minimalna vrijednost narudžbe",
                    color = Color(0xFF9AA1B4),
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = "$amount KM",
                    color = KarikaColors.White,
                    textSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
            Box(
                modifier = Modifier
                    .testTag(MIN_ORDER_TAG)
                    .height(36.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(VendorAccent)
                    .clickable(onClick = onChange)
                    .padding(horizontal = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Promijeni",
                    color = KarikaColors.White,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MinOrderSheet(component: OrdersComponent) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val value = component.minOrderValueModal.asState()
    val allowed = remember { "0123456789.".toSet() }

    ModalBottomSheet(
        onDismissRequest = { component.showMinOrderModal.value = false },
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
                .padding(start = 20.dp, end = 20.dp, top = 6.dp, bottom = 18.dp)
        ) {
            KarikaText(
                text = "Minimalna vrijednost narudžbe",
                color = KarikaUiColors.Ink,
                textSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W700
            )
            Spacer(Modifier.height(6.dp))
            KarikaText(
                text = "Kupci ne mogu poslati narudžbu čija je vrijednost bez PDV-a ispod ovog iznosa.",
                color = KarikaUiColors.Muted,
                textSize = 13.5.sp,
                lineHeight = 19.sp
            )
            Spacer(Modifier.height(18.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, VendorAccent, RoundedCornerShape(16.dp))
                    .padding(horizontal = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BasicTextField(
                    modifier = Modifier
                        .testTag(MIN_ORDER_FIELD_TAG)
                        .weight(1f),
                    value = value.value,
                    onValueChange = { new -> if (new.all { it in allowed }) value.value = new },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = KarikaUiColors.Ink,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.W700,
                        fontFamily = karikaFonts()
                    ),
                    cursorBrush = SolidColor(VendorAccent),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done)
                )
                Spacer(Modifier.width(8.dp))
                KarikaText(text = "KM", color = KarikaUiColors.Muted, textSize = 16.sp, fontWeight = FontWeight.W700)
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                val current = value.value.toDoubleOrNull()
                QUICK_MIN_AMOUNTS.forEach { amount ->
                    val selected = current == amount.toDouble()
                    val shape = RoundedCornerShape(12.dp)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(shape)
                            .background(if (selected) VendorAccentSoft else KarikaUiColors.Field)
                            .border(1.dp, if (selected) VendorAccent else Color.Transparent, shape)
                            .clickable { value.value = amount.toString() },
                        contentAlignment = Alignment.Center
                    ) {
                        KarikaText(
                            text = "$amount KM",
                            color = if (selected) VendorAccent else KarikaUiColors.Ink,
                            textSize = 13.sp,
                            fontWeight = FontWeight.W600,
                            maxLines = 1
                        )
                    }
                }
            }
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KTonalButton(
                    modifier = Modifier.weight(1f),
                    text = "Otkaži",
                    background = KarikaUiColors.Field,
                    color = KarikaUiColors.Ink,
                    height = 52.dp,
                    onClick = { component.showMinOrderModal.value = false }
                )
                KPrimaryButton(
                    modifier = Modifier.weight(1f),
                    text = "Sačuvaj",
                    background = VendorAccent,
                    onClick = { component.updateMinOrderAmount() }
                )
            }
        }
    }
}

/** Small gray handle on top of the new bottom sheets. */
@Composable
internal fun SheetHandle() {
    Box(
        modifier = Modifier
            .padding(top = 10.dp, bottom = 10.dp)
            .width(40.dp)
            .height(4.dp)
            .clip(RoundedCornerShape(50))
            .background(KarikaUiColors.Border)
    )
}

/** Background, text and dot colors of an order's status pill. */
private fun VendorOrder.pillColors(): Triple<Color, Color, Color> = when (realOrderStatus) {
    "approved" -> Triple(KarikaUiColors.GreenSoft, KarikaUiColors.Green, KarikaUiColors.Green)
    "bill-sent" -> Triple(KarikaUiColors.GreenSoft, KarikaUiColors.Green, KarikaUiColors.Green)
    "rejected" -> Triple(KarikaUiColors.RedSoft, KarikaUiColors.Red, KarikaUiColors.Red)
    "cancelled" -> Triple(KarikaUiColors.Field, KarikaUiColors.Muted, KarikaUiColors.Muted)
    else -> Triple(KarikaUiColors.AmberSoft, KarikaUiColors.Amber, KarikaUiColors.Amber)
}

@Composable
private fun OrderItem(component: OrdersComponent, vendorOrder: VendorOrder) {
    val locked = vendorOrder.locked()
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        KCard(
            modifier = Modifier
                .testTag(vendorOrderTag(vendorOrder))
                .fillMaxWidth()
                .blur(radius = if (locked) 5.dp else 0.dp),
            shape = RoundedCornerShape(14.dp),
            onClick = { component.dashNavigate(DashConfig.OrderDetails(vendorOrder)) }
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KInitials(
                        name = vendorOrder.b2bPravnoLice,
                        size = 40.dp,
                        shape = RoundedCornerShape(12.dp),
                        background = VendorAccentSoft,
                        color = VendorAccent,
                        textSize = 13.sp
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KarikaText(
                                modifier = Modifier.weight(1f, fill = false),
                                text = vendorOrder.b2bPravnoLice ?: "-",
                                color = KarikaUiColors.Ink,
                                textSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.W700,
                                maxLines = 1
                            )
                            if (vendorOrder.hasChanges()) {
                                Spacer(Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .background(color = KarikaUiColors.Red, shape = CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    KarikaText(
                                        text = "1",
                                        textSize = 10.sp,
                                        fontWeight = FontWeight.W700,
                                        color = KarikaColors.White
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(2.dp))
                        KarikaText(
                            text = vendorOrder.date(),
                            color = KarikaUiColors.Muted,
                            textSize = 12.sp,
                            maxLines = 1
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        KarikaText(
                            text = vendorOrder.totalAmount() + " KM",
                            color = KarikaUiColors.Ink,
                            textSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                        Spacer(Modifier.height(2.dp))
                        KarikaText(text = "bez PDV", color = KarikaUiColors.Muted, textSize = 11.sp)
                    }
                }
                KDivider(modifier = Modifier.padding(top = 12.dp, bottom = 12.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        modifier = Modifier.weight(1f),
                        text = "#${vendorOrder.orderId ?: ""}",
                        color = KarikaUiColors.Muted,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    val (background, color, dot) = vendorOrder.pillColors()
                    KPill(
                        text = vendorOrder.status(),
                        background = background,
                        color = color,
                        dot = dot,
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
        if (locked) {
            KarikaText(
                modifier = Modifier.padding(horizontal = 24.dp),
                text = "Za prikaz detalja narudžbe, molimo Vas da zaključite prethodne narudžbe tako što ćete ih označiti kao odobrene ili odbijene!",
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 19.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
        }
    }
}

package karika.distribucija.ba.ui.view.salesrep.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AssignedEmployeeSummary
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KSquareIconButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_filter
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_search
import karikav2.composeapp.generated.resources.ic_k_users
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource

// ── Helpers ───────────────────────────────────────────────────────────────────

/** Partnership status values used for filtering */
private val statusOptions = listOf(
    "active" to "Aktivno",
    "pending" to "Na čekanju",
    "rejected" to "Odbijeno",
    "revoked" to "Opozvano"
)

/** Colors and label of a partnership status pill. */
private data class StatusStyle(val label: String, val background: Color, val color: Color, val dot: Color)

private fun partnershipStyle(status: String): StatusStyle = when (status) {
    "active" -> StatusStyle("Aktivan", KarikaUiColors.GreenSoft, KarikaUiColors.Green, KarikaUiColors.GreenDot)
    "pending" -> StatusStyle("Na čekanju", KarikaUiColors.AmberSoft, KarikaUiColors.Amber, KarikaUiColors.Amber)
    "rejected" -> StatusStyle("Odbijen", KarikaUiColors.RedSoft, KarikaUiColors.Red, KarikaUiColors.Red)
    "revoked" -> StatusStyle("Opozvan", KarikaUiColors.Field, KarikaUiColors.Muted, KarikaUiColors.Subtle)
    else -> StatusStyle(status, KarikaUiColors.Field, KarikaUiColors.Muted, KarikaUiColors.Subtle)
}

private fun OperationalCustomer.title(): String = company?.takeIf { it.isNotBlank() } ?: fullName

// ── Screen ───────────────────────────────────────────────────────────────────

/** Test tag of the button that adds a customer, for the end-to-end tests. */
const val ADD_CUSTOMER_TAG = "add_customer"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesCustomersView(component: SalesCustomersComponent) {
    var showStatusSheet by remember { mutableStateOf(false) }
    var showAddSheet by remember { mutableStateOf(false) }
    var showRepsSheet by remember { mutableStateOf(false) }
    var repsSheetList by remember { mutableStateOf<List<AssignedEmployeeSummary>>(emptyList()) }
    val addSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val repsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val selectedTab by component.selectedTab.collectAsState()
    val customers by component.customers.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val searchText by component.searchQuery.collectAsState()
    val selectedStatus by component.statusFilter.collectAsState()
    val me by component.stateHolder.salesSpecificHandler.me.collectAsState()

    Column(
        modifier = Modifier
            .background(KarikaUiColors.Page)
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // ── Search + add ───────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchBox(
                modifier = Modifier.weight(1f),
                value = searchText,
                placeholder = "Pretraži kupce...",
                onValueChange = { component.setSearch(it) }
            )
            KSquareIconButton(
                modifier = Modifier.testTag(ADD_CUSTOMER_TAG),
                icon = vectorResource(Res.drawable.ic_k_plus),
                background = VendorAccent,
                onClick = { showAddSheet = true }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Tabs (only for reps allowed to see the vendor's full list) + status ──
        val statusLabel = statusOptions.firstOrNull { it.first == selectedStatus }?.second ?: "Svi statusi"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (me.capabilities.canSeeAllVendorCustomers) {
                KChip(
                    text = "Svi kupci",
                    selected = selectedTab == SalesCustomersComponent.CustomerTab.ALL_CUSTOMERS,
                    selectedColor = VendorAccent
                ) { component.selectTab(SalesCustomersComponent.CustomerTab.ALL_CUSTOMERS) }
                KChip(
                    text = "Moji kupci",
                    selected = selectedTab == SalesCustomersComponent.CustomerTab.MY_CUSTOMERS,
                    selectedColor = VendorAccent
                ) { component.selectTab(SalesCustomersComponent.CustomerTab.MY_CUSTOMERS) }
                Spacer(Modifier.weight(1f))
            }
            StatusFilterChip(
                label = statusLabel,
                filtered = selectedStatus != null,
                onClick = { showStatusSheet = true }
            )
        }

        Spacer(Modifier.height(12.dp))

        // ── Customer list ──────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = customers,
                key = { it.customerId }
            ) { customer ->
                CustomerCard(
                    customer = customer,
                    onClick = { component.openCustomer(customer) },
                    onOrder = { component.openOrderCatalog(customer) },
                    onMessage = { component.openMessageCustomer(customer) },
                    onDiscount = { component.openDiscount(customer) },
                    onHistory = { component.openOrderHistory() },
                    onShowReps = { repsSheetList = it; showRepsSheet = true }
                )
            }

            // Empty state
            if (customers.isEmpty() && !isLoadingMore) {
                item {
                    val hasSearch = searchText.isNotBlank()
                    val hasStatus = selectedStatus != null
                    val emptyStatusLabel = statusOptions.firstOrNull { it.first == selectedStatus }?.second

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        KEmptyPlaceholder(
                            icon = vectorResource(if (hasSearch) Res.drawable.ic_k_search else Res.drawable.ic_k_users),
                            title = when {
                                hasSearch -> "Nema rezultata za „$searchText“"
                                hasStatus -> "Nema kupaca sa statusom „$emptyStatusLabel“"
                                else -> "Nema kupaca"
                            },
                            message = if (hasSearch || hasStatus) {
                                "Pokušajte s drugom pretragom ili statusom."
                            } else {
                                "Dodajte kupca dugmetom + iznad liste."
                            },
                            iconTint = VendorAccent,
                            iconBackground = VendorAccentSoft
                        )

                        if (hasSearch || hasStatus) {
                            KSecondaryButton(
                                text = when {
                                    hasSearch && hasStatus -> "Poništi pretragu i filter"
                                    hasSearch -> "Poništi pretragu"
                                    else -> "Poništi filter"
                                },
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

            // Load more / spinner
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
                    } else if (component.hasMore && customers.isNotEmpty()) {
                        KSecondaryButton(
                            text = "Učitaj više kupaca",
                            height = 44.dp,
                            onClick = { component.loadNextPage() }
                        )
                    }
                }
            }
        }
    }

    // ── Add customer bottom sheet ──────────────────────────────────────────────
    if (showAddSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAddSheet = false },
            sheetState = addSheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                SheetTitle("Dodaj kupca")

                AddOptionItem(
                    icon = vectorResource(Res.drawable.ic_k_plus),
                    title = "Novi kupac",
                    subtitle = "Kreiraj novog kupca i partnerstvo"
                ) {
                    showAddSheet = false
                    component.openNewCustomer()
                }

                AddOptionItem(
                    icon = vectorResource(Res.drawable.ic_k_mail),
                    title = "Pozovi kupca",
                    subtitle = "Pošalji zahtjev za partnerstvo postojećem kupcu"
                ) {
                    showAddSheet = false
                    component.openInviteCustomer()
                }
            }
        }
    }

    // ── Komercijalisti bottom sheet ───────────────────────────────────────────
    if (showRepsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showRepsSheet = false },
            sheetState = repsSheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SheetTitle("Komercijalisti", modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(KarikaUiColors.Field)
                            .clickable { showRepsSheet = false },
                        contentAlignment = Alignment.Center
                    ) {
                        KIcon(icon = vectorResource(Res.drawable.ic_k_close), tint = KarikaUiColors.Ink, size = 16.dp)
                    }
                }

                Spacer(Modifier.height(8.dp))

                KCard(modifier = Modifier.fillMaxWidth()) {
                    repsSheetList.forEachIndexed { idx, emp ->
                        if (idx > 0) KDivider()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            KInitials(
                                name = emp.displayName,
                                size = 36.dp,
                                shape = CircleShape,
                                background = if (idx % 2 == 0) VendorAccentSoft else KarikaUiColors.Field,
                                color = if (idx % 2 == 0) VendorAccent else KarikaUiColors.Ink
                            )
                            KarikaText(
                                text = emp.displayName ?: "-",
                                color = KarikaUiColors.Ink,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                }
            }
        }
    }

    // ── Status bottom sheet ────────────────────────────────────────────────────
    if (showStatusSheet) {
        ModalBottomSheet(
            onDismissRequest = { showStatusSheet = false },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
            ) {
                SheetTitle("Filtriraj po statusu")
                Spacer(Modifier.height(8.dp))

                // "Svi statusi" option
                StatusSheetItem(
                    label = "Svi statusi",
                    selected = selectedStatus == null
                ) {
                    component.setStatus(null)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showStatusSheet = false
                    }
                }

                // Individual status options
                statusOptions.forEach { (value, label) ->
                    StatusSheetItem(
                        label = label,
                        selected = selectedStatus == value
                    ) {
                        component.setStatus(value)
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            showStatusSheet = false
                        }
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        component.loadPage(page = 1, replace = true)
    }
}

// ── Search ────────────────────────────────────────────────────────────────────

/** White search field on the gray page, with a clear button while it has text. */
@Composable
private fun SearchBox(
    value: String,
    placeholder: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
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
                decorationBox = { inner ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
                        }
                        inner()
                    }
                }
            )
        }
        if (value.isNotEmpty()) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.Field)
                    .clickable { onValueChange("") },
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_close), tint = KarikaUiColors.Muted, size = 14.dp)
            }
        }
    }
}

/** Chip with the chosen status that opens the status sheet. */
@Composable
private fun StatusFilterChip(label: String, filtered: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(50)
    Row(
        modifier = Modifier
            .height(34.dp)
            .clip(shape)
            .background(if (filtered) VendorAccent else KarikaColors.White)
            .then(if (filtered) Modifier else Modifier.border(1.dp, KarikaUiColors.Border, shape))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        val tint = if (filtered) KarikaColors.White else KarikaUiColors.Ink
        KIcon(icon = vectorResource(Res.drawable.ic_k_filter), tint = tint, size = 15.dp)
        KarikaText(
            text = label,
            color = tint,
            textSize = 14.sp,
            fontWeight = if (filtered) FontWeight.W700 else FontWeight.W500,
            maxLines = 1
        )
        KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_down), tint = tint, size = 15.dp)
    }
}

// ── Sheets ────────────────────────────────────────────────────────────────────

@Composable
private fun SheetTitle(text: String, modifier: Modifier = Modifier) {
    KarikaText(
        modifier = modifier.padding(vertical = 4.dp),
        text = text,
        color = KarikaUiColors.Ink,
        textSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun StatusSheetItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) VendorAccentSoft else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
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
            KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = VendorAccent, size = 18.dp)
        }
    }
}

@Composable
private fun AddOptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    KCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(VendorAccentSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = icon, tint = VendorAccent, size = 20.dp)
            }
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = title,
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = subtitle,
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 17.sp
                )
            }
            KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaUiColors.Subtle, size = 18.dp)
        }
    }
}

// ── Customer card ─────────────────────────────────────────────────────────────

@Composable
private fun CustomerCard(
    customer: OperationalCustomer,
    onClick: () -> Unit = {},
    onOrder: () -> Unit = {},
    onMessage: () -> Unit = {},
    onDiscount: () -> Unit = {},
    onHistory: () -> Unit = {},
    onShowReps: (List<AssignedEmployeeSummary>) -> Unit = {}
) {
    val status = partnershipStyle(customer.partnershipStatus)
    KCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        // Header: initials, name, contact and status
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KInitials(
                name = customer.title(),
                size = 44.dp,
                shape = RoundedCornerShape(12.dp),
                background = VendorAccentSoft,
                color = VendorAccent,
                textSize = 14.sp
            )
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = customer.title(),
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W700,
                    textOverflow = TextOverflow.Ellipsis,
                    maxLines = 2
                )
                val subtitle = listOfNotNull(
                    customer.fullName.takeIf { it != "—" && !customer.company.isNullOrBlank() },
                    customer.email?.takeIf { it.isNotBlank() }
                ).joinToString(" · ")
                if (subtitle.isNotEmpty()) {
                    Spacer(Modifier.height(2.dp))
                    KarikaText(
                        text = subtitle,
                        color = KarikaUiColors.Muted,
                        textSize = 12.5.sp,
                        lineHeight = 17.sp,
                        textOverflow = TextOverflow.Ellipsis,
                        maxLines = 1
                    )
                }
            }
            KPill(
                text = status.label,
                background = status.background,
                color = status.color,
                dot = status.dot
            )
        }

        val isPending = customer.partnershipStatus == "pending"
        if (customer.isActive || isPending) {
            val actionsEnabled = customer.isActive
            KDivider()

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
                    .alpha(if (actionsEnabled) 1f else 0.5f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Komercijalisti using assigned_employees
                val reps = customer.assignedEmployees.take(3)
                if (reps.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable(enabled = actionsEnabled) { onShowReps(customer.assignedEmployees) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val avatarSize = 26
                        val overlap = 16
                        Box(
                            modifier = Modifier
                                .width((avatarSize + overlap * (reps.size - 1)).dp)
                                .height(avatarSize.dp)
                        ) {
                            reps.forEachIndexed { idx, emp ->
                                KInitials(
                                    modifier = Modifier
                                        .offset(x = (idx * overlap).dp)
                                        .border(2.dp, KarikaColors.White, CircleShape),
                                    name = emp.displayName,
                                    size = avatarSize.dp,
                                    shape = CircleShape,
                                    background = if (idx % 2 == 0) VendorAccent else KarikaUiColors.Ink,
                                    color = KarikaColors.White,
                                    textSize = 9.sp
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        KarikaText(
                            text = "Komercijalisti",
                            color = KarikaUiColors.Muted,
                            textSize = 12.5.sp,
                            fontWeight = FontWeight.W600
                        )
                        if (customer.assignedEmployees.size > reps.size) {
                            KarikaText(
                                text = " +${customer.assignedEmployees.size - reps.size}",
                                color = KarikaUiColors.Muted,
                                textSize = 12.5.sp,
                                fontWeight = FontWeight.W600
                            )
                        }
                    }
                }

                // Quiet actions, then the primary "Naruči"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CardActionButton(
                        modifier = Modifier.weight(1f),
                        icon = vectorResource(Res.drawable.ic_k_chat),
                        label = "Poruka",
                        enabled = actionsEnabled,
                        onClick = onMessage
                    )
                    CardActionButton(
                        modifier = Modifier.weight(1f),
                        icon = vectorResource(Res.drawable.ic_k_gift),
                        label = "Rabat",
                        enabled = actionsEnabled,
                        onClick = onDiscount
                    )
                    CardActionButton(
                        modifier = Modifier.weight(1f),
                        icon = vectorResource(Res.drawable.ic_k_document),
                        label = "Historija",
                        enabled = actionsEnabled,
                        onClick = onHistory
                    )
                }
                KPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Naruči",
                    icon = vectorResource(Res.drawable.ic_k_cart),
                    background = VendorAccent,
                    height = 46.dp,
                    enabled = actionsEnabled,
                    onClick = onOrder
                )
            }
        }
    }
}

// ── Card action button ────────────────────────────────────────────────────────

@Composable
private fun CardActionButton(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .height(40.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KarikaUiColors.Field)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = icon, tint = VendorAccent, size = 16.dp)
        Spacer(Modifier.width(6.dp))
        KarikaText(
            text = label,
            color = KarikaUiColors.Ink,
            textSize = 13.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1,
            textOverflow = TextOverflow.Ellipsis
        )
    }
}

package karika.distribucija.ba.ui.view.salesrep.customers

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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.karikaFonts
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_cancel
import karikav2.composeapp.generated.resources.ic_check_circle_filled
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_filter_alt
import karikav2.composeapp.generated.resources.ic_search
import karikav2.composeapp.generated.resources.ic_shopping_cart
import karikav2.composeapp.generated.resources.ic_storefront
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

// ── Helpers ───────────────────────────────────────────────────────────────────

/** "Ada Lovelace" → "AL", "Ada" → "A" */
private fun String?.initials(): String =
    this?.trim()?.split(" ")?.take(2)
        ?.mapNotNull { it.firstOrNull()?.uppercaseChar() }
        ?.joinToString("") ?: "?"

/** Partnership status values used for filtering */
private val statusOptions = listOf(
    "active" to "Aktivno",
    "pending" to "Na čekanju",
    "rejected" to "Odbijeno",
    "revoked" to "Opozvano"
)

// ── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesCustomersView(component: SalesCustomersComponent) {
    var showStatusSheet by remember { mutableStateOf(false) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    val selectedTab by component.selectedTab.collectAsState()
    val customers by component.customers.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val searchText by component.searchQuery.collectAsState()
    val selectedStatus by component.statusFilter.collectAsState()

    Column(
        modifier = Modifier
            .background(KarikaColors.Gray20)
            .fillMaxSize()
    ) {
        // ── Tabs ───────────────────────────────────────────────────────────────
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)) {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(KarikaColors.Gray9)
                    .padding(4.dp)
            ) {
                TabPill(
                    label = "Svi kupci",
                    selected = selectedTab == SalesCustomersComponent.CustomerTab.ALL_CUSTOMERS
                ) { component.selectTab(SalesCustomersComponent.CustomerTab.ALL_CUSTOMERS) }
                TabPill(
                    label = "Moji kupci",
                    selected = selectedTab == SalesCustomersComponent.CustomerTab.MY_CUSTOMERS
                ) { component.selectTab(SalesCustomersComponent.CustomerTab.MY_CUSTOMERS) }
            }
        }

        // ── Search + actions bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
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
                        text = "Pretraži kupce...",
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

            // Status filter chip — replaces generic Filter
            val statusLabel =
                statusOptions.firstOrNull { it.first == selectedStatus }?.second ?: "Svi statusi"
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

            ActionChip(
                icon = Res.drawable.ic_add_plus,
                label = "Novi kupac",
                bgColor = KarikaColors.Blue,
                textColor = KarikaColors.White,
                iconColor = KarikaColors.White
            ) { component.openNewCustomer() }
        }

        YSpacer16()

        // ── Customer list ──────────────────────────────────────────────────────
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 0.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = customers,
                key = { it.customerId }
            ) { customer ->
                CustomerCard(
                    customer = customer,
                    onClick = { component.openCustomer(customer) },
                    onOrder = { component.openOrderCatalog(customer) }
                )
            }

            // Empty state
            if (customers.isEmpty() && !isLoadingMore) {
                item {
                    val hasSearch = searchText.isNotBlank()
                    val hasStatus = selectedStatus != null
                    val statusLabel =
                        statusOptions.firstOrNull { it.first == selectedStatus }?.second

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
                                hasSearch -> "Nema rezultata za „$searchText„"
                                hasStatus -> "Nema kupaca sa statusom „$statusLabel„"
                                else -> "Nema kupaca"
                            },
                            color = KarikaColors.Gray2,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W600,
                            textAlign = TextAlign.Center
                        )

                        if (hasSearch || hasStatus) {
                            Row(
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
                                    .padding(horizontal = 20.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_cancel),
                                    contentDescription = "",
                                    tint = KarikaColors.Gray2,
                                    modifier = Modifier.size(14.dp)
                                )
                                KarikaText(
                                    text = when {
                                        hasSearch && hasStatus -> "Poništi pretragu i filter"
                                        hasSearch -> "Poništi pretragu"
                                        else -> "Poništi filter"
                                    },
                                    color = KarikaColors.Gray2,
                                    textSize = 13.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                }
            }

            // Load more / spinner
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isLoadingMore) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = KarikaColors.Blue,
                            strokeWidth = 2.5.dp
                        )
                    } else if (component.hasMore && customers.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { component.loadNextPage() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                text = "Učitaj više kupaca",
                                color = KarikaColors.Blue,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W500
                            )
                            Spacer(Modifier.width(4.dp))
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_arrow_down),
                                contentDescription = "",
                                tint = KarikaColors.Blue,
                                modifier = Modifier.size(20.dp)
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
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                KarikaText(
                    text = "Filtriraj po statusu",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

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

                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ── Status sheet item ─────────────────────────────────────────────────────────

@Composable
private fun StatusSheetItem(label: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selected) KarikaColors.Blue.copy(alpha = 0.08f) else KarikaColors.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            text = label,
            color = if (selected) KarikaColors.Blue else KarikaColors.Gray2,
            textSize = 15.sp,
            fontWeight = if (selected) FontWeight.W700 else FontWeight.W400
        )
        if (selected) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_check_circle_filled),
                contentDescription = "",
                tint = KarikaColors.Blue,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Tab pill ──────────────────────────────────────────────────────────────────

@Composable
private fun TabPill(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (selected) KarikaColors.White else KarikaColors.Transparent)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = label,
            color = if (selected) KarikaColors.Blue else KarikaColors.Gray6,
            textSize = 12.sp,
            fontWeight = FontWeight.W600
        )
    }
}

// ── Small action chip (Filter / Novi kupac) ───────────────────────────────────

@Composable
private fun ActionChip(
    icon: DrawableResource,
    label: String,
    bgColor: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    iconColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = vectorResource(icon),
            contentDescription = "",
            tint = iconColor,
            modifier = Modifier.size(14.dp)
        )
        KarikaText(text = label, color = textColor, textSize = 12.sp, fontWeight = FontWeight.W600)
    }
}

// ── Customer card ─────────────────────────────────────────────────────────────

@Composable
private fun CustomerCard(
    customer: OperationalCustomer,
    onClick: () -> Unit = {},
    onOrder: () -> Unit = {}
) {
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
    ) {
        // Card body
        Column(modifier = Modifier.padding(16.dp)) {
            // Avatar + status badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val badgeBg = if (customer.isActive) KarikaColors.Green4 else KarikaColors.Gray5
                val badgeLabel = when (customer.partnershipStatus) {
                    "active" -> "AKTIVAN"
                    "pending" -> "NA ČEKANJU"
                    "revoked" -> "OPOZVAN"
                    "rejected" -> "ODBIJEN"
                    else -> customer.partnershipStatus.uppercase()
                }
                val badgeColor = if (customer.isActive) KarikaColors.Green3 else KarikaColors.Gray6

                KarikaText(
                    text = customer.fullName,
                    color = KarikaColors.Gray2,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W700
                )

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(badgeBg)
                        .border(1.dp, badgeColor.copy(alpha = 0.25f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    KarikaText(
                        text = badgeLabel,
                        color = badgeColor,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            YSpacer16()

            if (!customer.company.isNullOrBlank()) {
                InfoRow(iconRes = Res.drawable.ic_storefront, text = customer.company)
                YSpacer8()
            }

            if (!customer.email.isNullOrBlank()) {
                InfoRow(iconRes = Res.drawable.ic_email, text = customer.email)
            }
        }

        // Card footer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                .background(KarikaColors.Gray20)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Gray9,
                    shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
                )
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Company name + Komercijalisti chips
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Komercijalisti chips using assigned_employees
                val reps = customer.assignedEmployees.take(3)
                if (reps.isNotEmpty()) {
                    val avatarSize = 26
                    val overlap = 10
                    val totalWidth =
                        (avatarSize + overlap * (reps.size - 1)).coerceAtLeast(avatarSize)

                    Box(modifier = Modifier.width(totalWidth.dp).height(avatarSize.dp)) {
                        reps.forEachIndexed { idx, emp ->
                            val bg = if (idx % 2 == 0) KarikaColors.Blue else KarikaColors.Secondary
                            Box(
                                modifier = Modifier
                                    .offset(x = (idx * overlap).dp)
                                    .size(avatarSize.dp)
                                    .clip(CircleShape)
                                    .background(bg)
                                    .border(2.dp, KarikaColors.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                KarikaText(
                                    text = emp.displayName.initials(),
                                    color = KarikaColors.White,
                                    textSize = 8.sp,
                                    fontWeight = FontWeight.W700,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(8.dp))
                    KarikaText(
                        text = "Komercijalisti",
                        color = KarikaColors.Gray7,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                } else if (customer.company.isNullOrBlank()) {
                    KarikaText(
                        text = "Nije dodijeljen",
                        color = KarikaColors.Gray7,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            // Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Order button
                if (customer.isActive) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(KarikaColors.Blue)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onOrder() }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_shopping_cart),
                            contentDescription = "",
                            tint = KarikaColors.White,
                            modifier = Modifier.size(14.dp)
                        )
                        KarikaText(
                            text = "Naruči",
                            color = KarikaColors.White,
                            textSize = 12.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(KarikaColors.Gray5)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_cancel),
                            contentDescription = "",
                            tint = KarikaColors.Gray6,
                            modifier = Modifier.size(14.dp)
                        )
                        KarikaText(
                            text = "Naruči",
                            color = KarikaColors.Gray6,
                            textSize = 12.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    }
}

// ── Info row ──────────────────────────────────────────────────────────────────

@Composable
private fun InfoRow(iconRes: DrawableResource, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(KarikaColors.Gray10),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(iconRes),
                contentDescription = "",
                tint = KarikaColors.Gray6,
                modifier = Modifier.size(15.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        KarikaText(
            text = text,
            color = KarikaColors.Gray6,
            textSize = 13.sp,
            fontWeight = FontWeight.W400,
            modifier = Modifier.fillMaxWidth(),
            textOverflow = TextOverflow.Ellipsis,
            maxLines = 1
        )
    }
}

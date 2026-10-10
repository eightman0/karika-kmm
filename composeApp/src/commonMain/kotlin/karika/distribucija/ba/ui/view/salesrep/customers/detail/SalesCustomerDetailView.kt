package karika.distribucija.ba.ui.view.salesrep.customers.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KConfirmDialog
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KKeyValueCard
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_trash
import karikav2.composeapp.generated.resources.ic_k_user
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesCustomerDetailView(component: SalesCustomerDetailComponent) {
    val customer = component.customer
    val discounts by component.discounts.collectAsState()

    var confirmDeleteRule by remember { mutableStateOf<DiscountRule?>(null) }

    // ── Confirmation dialog ────────────────────────────────────────────────────
    confirmDeleteRule?.let { rule ->
        KConfirmDialog(
            title = "Obriši popust",
            message = "Sigurno želite obrisati ovaj popust? Ova radnja se ne može poništiti.",
            icon = vectorResource(Res.drawable.ic_k_trash),
            confirmText = "Obriši",
            dismissText = "Odustani",
            onConfirm = {
                component.deleteDiscount(rule)
                confirmDeleteRule = null
            },
            onDismiss = { confirmDeleteRule = null }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(KarikaUiColors.Page)) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // ── Header card ────────────────────────────────────────────────────
            item {
                val name = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName
                val (statusText, statusBg, statusColor) = when (customer.partnershipStatus) {
                    "active" -> Triple("Aktivno", KarikaUiColors.GreenSoft, KarikaUiColors.Green)
                    "pending" -> Triple("Na čekanju", KarikaUiColors.AmberSoft, KarikaUiColors.Amber)
                    "rejected" -> Triple("Odbijeno", KarikaUiColors.RedSoft, KarikaUiColors.Red)
                    "revoked" -> Triple("Opozvano", KarikaUiColors.Field, KarikaUiColors.Muted)
                    else -> Triple(customer.partnershipStatus, KarikaUiColors.Field, KarikaUiColors.Muted)
                }
                KCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        KInitials(
                            name = name,
                            size = 56.dp,
                            shape = RoundedCornerShape(16.dp),
                            background = VendorAccentSoft,
                            color = VendorAccent,
                            textSize = 18.sp
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = "PROFIL KUPCA",
                                color = KarikaUiColors.Muted,
                                textSize = 11.sp,
                                fontWeight = FontWeight.W700
                            )
                            Spacer(Modifier.height(2.dp))
                            KarikaText(
                                text = name,
                                color = KarikaUiColors.Ink,
                                textSize = 18.sp,
                                lineHeight = 23.sp,
                                fontWeight = FontWeight.W700,
                                maxLines = 2
                            )
                            Spacer(Modifier.height(6.dp))
                            KPill(text = statusText, background = statusBg, color = statusColor, dot = statusColor)
                        }
                    }
                }
            }

            // ── Info ──────────────────────────────────────────────────────────
            item {
                KKeyValueCard(
                    rows = buildList {
                        if (!customer.email.isNullOrBlank()) add("Email adresa" to customer.email)
                        add("Kontakt osoba" to customer.fullName)
                        if (customer.assignedEmployees.isNotEmpty()) {
                            add("Komercijalisti" to customer.assignedEmployees.joinToString(", ") { it.displayName ?: "—" })
                        }
                    }
                )
            }

            // ── Discounts header ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KSectionTitle(modifier = Modifier.weight(1f), title = "Popusti")
                    if (component.canCreateDiscountFor) {
                        NewDiscountButton(onClick = { component.openNewDiscount() })
                    }
                }
            }

            // ── Discount items ─────────────────────────────────────────────────
            items(discounts, key = { it.ruleId ?: 0L }) { rule ->
                DiscountCard(
                    rule = rule,
                    canEdit = component.canCreateDiscountFor && !rule.createdByAdmin(),
                    onEdit = { component.openEditDiscount(rule) },
                    onDelete = { confirmDeleteRule = rule }
                )
            }

            if (discounts.isEmpty()) {
                item {
                    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                        KarikaText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            text = "Nema popusta za ovog kupca",
                            color = KarikaUiColors.Muted,
                            textSize = 13.sp
                        )
                    }
                }
            }
        }

        // ── Order for the customer ─────────────────────────────────────────────
        if (customer.isActive) {
            KBottomPanel {
                KPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Naruči za kupca",
                    icon = vectorResource(Res.drawable.ic_k_cart),
                    background = VendorAccent,
                    onClick = { component.openOrderCatalog() }
                )
            }
        }
    }
}

@Composable
private fun NewDiscountButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VendorAccentSoft)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = VendorAccent, size = 15.dp)
        Spacer(Modifier.width(4.dp))
        KarikaText(
            text = "Novi popust",
            color = VendorAccent,
            textSize = 12.5.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

// ── Discount card ─────────────────────────────────────────────────────────────

/** Test tag of a discount rule's card, for the end-to-end tests. */
fun discountTag(rule: DiscountRule) = "discount_${rule.ruleId}"

@Composable
private fun DiscountCard(
    rule: DiscountRule,
    canEdit: Boolean = true,
    onEdit: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val targetLabel = when {
        rule.productId != null -> rule.productName ?: "Artikal #${rule.productId}"
        rule.categoryId != null -> rule.categoryName ?: "Kategorija #${rule.categoryId}"
        else -> "Svi artikli i kategorije"
    }
    val detail = buildList {
        add(if (rule.categoryId != null && rule.productId == null) "Kategorija" else if (rule.productId != null) "Artikal" else "Svi artikli")
        rule.minQty?.toInt()?.let { add("min. $it kom") }
    }.joinToString(" · ")

    val (approvalText, approvalBg, approvalColor) = when (rule.approvalStatus) {
        "approved" -> Triple("Odobreno", KarikaUiColors.GreenSoft, KarikaUiColors.Green)
        "pending" -> Triple("Na čekanju", KarikaUiColors.AmberSoft, KarikaUiColors.Amber)
        "rejected" -> Triple("Odbijeno", KarikaUiColors.RedSoft, KarikaUiColors.Red)
        else -> Triple(rule.approvalStatus ?: "—", KarikaUiColors.Field, KarikaUiColors.Muted)
    }

    KCard(
        modifier = modifier
            .testTag(discountTag(rule))
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = targetLabel,
                    color = KarikaUiColors.Ink,
                    textSize = 14.5.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 2
                )
                Spacer(Modifier.height(3.dp))
                KarikaText(
                    text = detail,
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 17.sp,
                    maxLines = 1
                )
                Spacer(Modifier.height(6.dp))
                KPill(text = approvalText, background = approvalBg, color = approvalColor, textSize = 11.sp)
            }
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VendorAccentSoft)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "${rule.discountPercent.toInt()}%",
                    color = VendorAccent,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
        }

        KDivider()

        // Footer: who made it and the actions
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 6.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_user), tint = KarikaUiColors.Subtle, size = 15.dp)
            Spacer(Modifier.width(6.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = rule.createdByEmployeeName ?: "Administrator",
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp,
                maxLines = 1
            )
            if (canEdit) {
                CardTextAction(text = "Izmijeni", color = VendorAccent, onClick = onEdit)
                CardTextAction(
                    text = "Obriši",
                    color = KarikaUiColors.Red,
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun CardTextAction(text: String, color: Color, onClick: () -> Unit) {
    KarikaText(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 8.dp),
        text = text,
        color = color,
        textSize = 13.sp,
        fontWeight = FontWeight.W700
    )
}

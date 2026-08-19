package karika.distribucija.ba.ui.view.salesrep.customers.detail

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.DiscountRule
import karika.distribucija.ba.domain.model.OperationalCustomer
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_customers
import karikav2.composeapp.generated.resources.ic_delete
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_gift
import karikav2.composeapp.generated.resources.ic_person
import karikav2.composeapp.generated.resources.ic_shopping_cart
import karikav2.composeapp.generated.resources.ic_storefront
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesCustomerDetailView(component: SalesCustomerDetailComponent) {
    val customer = component.customer
    val discounts by component.discounts.collectAsState()

    var confirmDeleteRule by remember { mutableStateOf<DiscountRule?>(null) }

    // ── Confirmation dialog ────────────────────────────────────────────────────
    confirmDeleteRule?.let { rule ->
        AlertDialog(
            onDismissRequest = { confirmDeleteRule = null },
            containerColor = KarikaColors.White,
            title = {
                KarikaText(
                    text = "Obriši popust",
                    color = KarikaColors.Gray2,
                    textSize = 17.sp,
                    fontWeight = FontWeight.W700
                )
            },
            text = {
                KarikaText(
                    text = "Sigurno želite obrisati ovaj popust? Ova radnja se ne može poništiti.",
                    color = KarikaColors.Gray6,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            },
            confirmButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(KarikaColors.Error)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            component.deleteDiscount(rule)
                            confirmDeleteRule = null
                        }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    KarikaText(
                        text = "Obriši",
                        color = KarikaColors.White,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            },
            dismissButton = {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(KarikaColors.Gray10)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { confirmDeleteRule = null }
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    KarikaText(
                        text = "Odustani",
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize().background(KarikaColors.Gray20)) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ── Profile card ───────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    // Header row: label + status badge
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column {
                            KarikaText(
                                text = "PROFIL KUPCA",
                                color = KarikaColors.Blue,
                                textSize = 10.sp,
                                fontWeight = FontWeight.W700
                            )
                            Spacer(Modifier.height(4.dp))
                            KarikaText(
                                text = customer.company,
                                color = KarikaColors.Gray2,
                                textSize = 20.sp,
                                fontWeight = FontWeight.W700
                            )
                        }

                        val (badgeBg, badgeText, badgeColor) = when (customer.partnershipStatus) {
                            "active"   -> Triple(KarikaColors.Green4, "AKTIVNO",  KarikaColors.Green3)
                            "pending"  -> Triple(KarikaColors.Blue3_10, "NA ČEKANJU", KarikaColors.Blue)
                            "rejected" -> Triple(KarikaColors.Red2, "ODBIJENO", KarikaColors.Error)
                            "revoked"  -> Triple(KarikaColors.Gray5, "OPOZVANO", KarikaColors.Gray6)
                            else       -> Triple(KarikaColors.Gray5, customer.partnershipStatus.uppercase(), KarikaColors.Gray6)
                        }
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(badgeBg)
                                .padding(horizontal = 12.dp, vertical = 5.dp)
                        ) {
                            KarikaText(
                                text = badgeText,
                                color = badgeColor,
                                textSize = 11.sp,
                                fontWeight = FontWeight.W700
                            )
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // Email row
                    if (!customer.email.isNullOrBlank()) {
                        ProfileInfoRow(
                            icon = Res.drawable.ic_email,
                            label = "Email adresa",
                            value = customer.email
                        )
                        YSpacer16()
                    }

                    // Full name row
                    ProfileInfoRow(
                        icon = Res.drawable.ic_person,
                        label = "Kontakt osoba",
                        value = customer.fullName
                    )

                    // Komercijalisti section
                    if (customer.assignedEmployees.isNotEmpty()) {
                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = KarikaColors.Gray9.copy(alpha = 0.5f))
                        Spacer(Modifier.height(16.dp))

                        Row(verticalAlignment = Alignment.Top) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.Blue.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_customers),
                                    contentDescription = "",
                                    tint = KarikaColors.Blue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                KarikaText(
                                    text = "KOMERCIJALISTI",
                                    color = KarikaColors.Gray6,
                                    textSize = 10.sp,
                                    fontWeight = FontWeight.W700
                                )
                                Spacer(Modifier.height(2.dp))
                                KarikaText(
                                    text = customer.assignedEmployees.joinToString(", ") { it.displayName ?: "—" },
                                    color = KarikaColors.Blue,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W700
                                )
                            }
                        }
                    }
                }
            }

            // ── Discounts header ───────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        text = "Popusti",
                        color = KarikaColors.Gray2,
                        textSize = 18.sp,
                        fontWeight = FontWeight.W700
                    )
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(KarikaColors.Blue)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { component.openNewDiscount() }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_add_plus),
                            contentDescription = "",
                            tint = KarikaColors.White,
                            modifier = Modifier.size(16.dp)
                        )
                        KarikaText(
                            text = "Novi popust",
                            color = KarikaColors.White,
                            textSize = 13.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }

            // ── Discount items ─────────────────────────────────────────────────
            items(discounts, key = { it.ruleId ?: 0L }) { rule ->
                DiscountCard(
                    rule = rule,
                    onEdit = { component.openEditDiscount(rule) },
                    onDelete = { confirmDeleteRule = rule },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }

            if (discounts.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        KarikaText(
                            text = "Nema popusta za ovog kupca",
                            color = KarikaColors.Gray6,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            }
        }

        // ── FAB ────────────────────────────────────────────────────────────────
        if (customer.isActive) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(16.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(KarikaColors.Blue)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.openOrderCatalog() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_shopping_cart),
                        contentDescription = "",
                        tint = KarikaColors.White,
                        modifier = Modifier.size(22.dp)
                    )
                    KarikaText(
                        text = "Naruči za kupca",
                        color = KarikaColors.White,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
    }
}

// ── Profile info row ──────────────────────────────────────────────────────────

@Composable
private fun ProfileInfoRow(
    icon: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    value: String
) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(KarikaColors.Gray10),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(icon),
                contentDescription = "",
                tint = KarikaColors.Blue,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column {
            KarikaText(
                text = label,
                color = KarikaColors.Gray6,
                textSize = 11.sp,
                fontWeight = FontWeight.W600
            )
            Spacer(Modifier.height(2.dp))
            KarikaText(
                text = value,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1,
                textOverflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ── Discount card ─────────────────────────────────────────────────────────────

@Composable
private fun DiscountCard(rule: DiscountRule, onEdit: () -> Unit = {}, onDelete: () -> Unit = {}, modifier: Modifier = Modifier) {
    val targetLabel = when {
        rule.productId != null  -> rule.productName ?: "Artikal #${rule.productId}"
        rule.categoryId != null -> rule.categoryName ?: "Kategorija #${rule.categoryId}"
        else                    -> "Svi artikli i kategorije"
    }

    val (approvalBg, approvalText, approvalColor) = when (rule.approvalStatus) {
        "approved" -> Triple(KarikaColors.Green4, "ODOBRENO", KarikaColors.Green3)
        "pending"  -> Triple(KarikaColors.Yellow2, "NA ČEKANJU", KarikaColors.Yellow1)
        "rejected" -> Triple(KarikaColors.Red2, "ODBIJENO", KarikaColors.Error)
        else       -> Triple(KarikaColors.Gray5, rule.approvalStatus?.uppercase() ?: "—", KarikaColors.Gray6)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        // Header: CILJ label + approval badge
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "CILJ",
                    color = KarikaColors.Gray7,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = targetLabel,
                    color = KarikaColors.Gray2,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
            }
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(approvalBg)
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                KarikaText(
                    text = approvalText,
                    color = approvalColor,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }

        YSpacer16()
        HorizontalDivider(color = KarikaColors.Gray9)
        YSpacer16()

        // Min qty + discount percent row
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Min. kol.",
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = rule.minQty?.toInt()?.toString() ?: "—",
                    color = KarikaColors.Blue,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W700
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Rabat %",
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = "${rule.discountPercent.toInt()}%",
                    color = KarikaColors.Blue,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }

        YSpacer16()
        HorizontalDivider(color = KarikaColors.Gray9)
        YSpacer8()

        // Footer: created by + action buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_person),
                    contentDescription = "",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier.size(16.dp)
                )
                KarikaText(
                    text = "Administrator",
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    text = "Izmijeni",
                    color = KarikaColors.Blue,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W700,
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onEdit() }
                )
                Row(
                    modifier = Modifier.clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDelete() },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_delete),
                        contentDescription = "",
                        tint = KarikaColors.Error,
                        modifier = Modifier.size(14.dp)
                    )
                    KarikaText(
                        text = "Obriši",
                        color = KarikaColors.Error,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
    }
}

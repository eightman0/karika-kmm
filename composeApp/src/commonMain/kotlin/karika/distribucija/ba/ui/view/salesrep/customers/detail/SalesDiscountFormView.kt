package karika.distribucija.ba.ui.view.salesrep.customers.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_info
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_tag
import karikav2.composeapp.generated.resources.ic_k_user
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesDiscountFormView(component: SalesDiscountFormComponent) {
    val itemSearch by component.itemSearch.collectAsState()
    val selectedItem by component.selectedItem.collectAsState()
    val searchResults by component.searchResults.collectAsState()
    val minQty by component.minQty.collectAsState()
    val discountPercent by component.discountPercent.collectAsState()
    val isSaving by component.isSaving.collectAsState()

    val showDropdown = searchResults.isNotEmpty() && selectedItem == null
    val customerName = component.customer.company?.takeIf { it.isNotBlank() } ?: component.customer.fullName

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // ── Kupac (fixed: the discount is for this customer) ───────────
                    Column {
                        KFieldLabel(text = "Kupac")
                        ReadOnlyField(icon = vectorResource(Res.drawable.ic_k_user), text = customerName)
                    }

                    // ── Artikal ili kategorija ─────────────────────────────────────
                    Column {
                        KFieldLabel(text = "Artikal ili kategorija")
                        val selected = selectedItem
                        if (selected != null) {
                            // Once chosen, the item is shown read-only with a button to clear it
                            SelectedItemField(
                                text = itemSearch.ifEmpty { selected.displayName },
                                isCategory = selected is DiscountSearchItem.CategoryItem,
                                onClear = { component.clearItem() }
                            )
                        } else {
                            BorderedField(
                                value = itemSearch,
                                onValueChange = { component.setItemSearch(it) },
                                placeholder = "Svi artikli i kategorije",
                                leadingIcon = vectorResource(Res.drawable.ic_k_tag),
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next,
                                focusedBorder = showDropdown,
                                trailing = if (itemSearch.isNotEmpty()) {
                                    { ClearButton(onClick = { component.clearItem() }) }
                                } else null
                            )
                        }

                        // Dropdown results
                        if (showDropdown) {
                            Spacer(Modifier.height(6.dp))
                            KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                                searchResults.forEachIndexed { index, item ->
                                    if (index > 0) KDivider()
                                    SearchItemRow(
                                        item = item,
                                        onClick = { component.selectItem(item) }
                                    )
                                }
                            }
                        }
                    }

                    // ── Min. količina | Rabat ──────────────────────────────────────
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Column(modifier = Modifier.weight(1f)) {
                            KFieldLabel(text = "Min. količina")
                            BorderedField(
                                value = minQty,
                                onValueChange = { component.setMinQty(it) },
                                placeholder = "Opcionalno",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                suffix = "kom"
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            KFieldLabel(text = "Rabat", required = true, requiredColor = VendorAccent)
                            BorderedField(
                                value = discountPercent,
                                onValueChange = { component.setDiscountPercent(it) },
                                placeholder = "0",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Done,
                                suffix = "%"
                            )
                        }
                    }
                }
            }

            InfoNote("Popust važi samo za ovog kupca. Bez odabranog artikla ili kategorije važi za sve artikle i kategorije.")
        }

        KBottomPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KSecondaryButton(
                    modifier = Modifier.weight(1f),
                    text = "Odustani",
                    onClick = { component.goBack() }
                )
                KPrimaryButton(
                    modifier = Modifier.weight(1.6f),
                    text = "Sačuvaj",
                    background = VendorAccent,
                    enabled = !isSaving,
                    onClick = { component.save() }
                )
            }
        }
    }
}

/** White bordered text field with the placeholder inside it and an optional suffix ("kom", "%"). */
@Composable
private fun BorderedField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    leadingIcon: ImageVector? = null,
    suffix: String? = null,
    focusedBorder: Boolean = false,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    BasicTextField(
        modifier = Modifier.fillMaxWidth(),
        value = value,
        onValueChange = onValueChange,
        singleLine = true,
        textStyle = TextStyle(color = KarikaUiColors.Ink, fontSize = 15.sp, fontFamily = karikaFonts()),
        cursorBrush = SolidColor(VendorAccent),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
        decorationBox = { inner ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(shape)
                    .background(KarikaColors.White)
                    .border(1.dp, if (focusedBorder) VendorAccent else KarikaUiColors.Border, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    KIcon(icon = leadingIcon, tint = KarikaUiColors.Muted, size = 18.dp)
                    Spacer(Modifier.width(10.dp))
                }
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
                    }
                    inner()
                }
                if (suffix != null) {
                    Spacer(Modifier.width(6.dp))
                    KarikaText(text = suffix, color = KarikaUiColors.Muted, textSize = 13.sp, fontWeight = FontWeight.W600)
                }
                if (trailing != null) {
                    Spacer(Modifier.width(8.dp))
                    trailing()
                }
            }
        }
    )
}

@Composable
private fun ReadOnlyField(icon: ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaUiColors.Field)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = icon, tint = KarikaUiColors.Muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Ink,
            textSize = 15.sp,
            maxLines = 1
        )
        KIcon(icon = vectorResource(Res.drawable.ic_k_lock), tint = KarikaUiColors.Subtle, size = 16.dp)
    }
}

@Composable
private fun SelectedItemField(text: String, isCategory: Boolean, onClear: () -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(VendorAccentSoft)
            .border(1.dp, VendorAccent, shape)
            .padding(start = 14.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_tag), tint = VendorAccent, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
        Spacer(Modifier.width(8.dp))
        // Not the "KAT."/"ART." badge: the tests take it for the open result list
        KarikaText(
            text = if (isCategory) "Kategorija" else "Artikal",
            color = VendorAccent,
            textSize = 11.sp,
            fontWeight = FontWeight.W700
        )
        Spacer(Modifier.width(8.dp))
        ClearButton(onClick = onClear)
    }
}

@Composable
private fun ClearButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(KarikaUiColors.Field)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_close), tint = KarikaUiColors.Muted, size = 14.dp)
    }
}

/** "KAT." or "ART." label of a search result. */
@Composable
private fun TypeBadge(isCategory: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isCategory) VendorAccentSoft else KarikaUiColors.Field)
            .padding(horizontal = 6.dp, vertical = 3.dp)
    ) {
        KarikaText(
            text = if (isCategory) "KAT." else "ART.",
            color = if (isCategory) VendorAccent else KarikaUiColors.Muted,
            textSize = 10.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaUiColors.Field)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_info), tint = KarikaUiColors.Muted, size = 18.dp)
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Muted,
            textSize = 12.5.sp,
            lineHeight = 18.sp
        )
    }
}

// ── Search result row ──────────────────────────────────────────────────────────

@Composable
private fun SearchItemRow(item: DiscountSearchItem, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TypeBadge(isCategory = item is DiscountSearchItem.CategoryItem)

        Column(modifier = Modifier.weight(1f)) {
            when (item) {
                is DiscountSearchItem.CategoryItem -> {
                    KarikaText(
                        text = item.fullPath,
                        color = KarikaUiColors.Ink,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }

                is DiscountSearchItem.ProductItem -> {
                    KarikaText(
                        text = item.product.name ?: "—",
                        color = KarikaUiColors.Ink,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                    if (!item.product.sku.isNullOrBlank()) {
                        KarikaText(
                            text = item.product.sku!!,
                            color = KarikaUiColors.Muted,
                            textSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_search
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesDiscountFormView(component: SalesDiscountFormComponent) {
    val productSearch by component.productSearch.collectAsState()
    val searchResults by component.searchResults.collectAsState()
    val showDropdown by component.showDropdown.collectAsState()
    val minQty by component.minQty.collectAsState()
    val discountPercent by component.discountPercent.collectAsState()
    val isSaving by component.isSaving.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(KarikaColors.Gray20)) {

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 180.dp)
        ) {
            // ── Form card ──────────────────────────────────────────────────────
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
                    // Artikal ili kategorija label
                    KarikaText(
                        text = "Artikal ili kategorija",
                        color = KarikaColors.Gray6,
                        textSize = 12.sp,
                        fontWeight = FontWeight.W600
                    )
                    Spacer(Modifier.height(6.dp))

                    // Search field
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(KarikaColors.Gray20)
                            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                            .padding(horizontal = 12.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_search),
                            contentDescription = "",
                            tint = KarikaColors.Gray6,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            if (productSearch.isEmpty()) {
                                KarikaText(
                                    text = "Svi artikli i kategorije",
                                    color = KarikaColors.Gray7,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                            BasicTextField(
                                value = productSearch,
                                onValueChange = { component.setProductSearch(it) },
                                textStyle = TextStyle(
                                    color = KarikaColors.Gray2,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.W500
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        if (productSearch.isNotEmpty()) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(KarikaColors.Gray9)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { MutableInteractionSource() }
                                    ) { component.clearProduct() },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = vectorResource(Res.drawable.ic_tertiary),
                                    contentDescription = "Obriši",
                                    tint = KarikaColors.Gray6,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }

                    // Dropdown results
                    if (showDropdown && searchResults.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(KarikaColors.White)
                                .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                        ) {
                            searchResults.forEachIndexed { index, product ->
                                ProductSearchRow(
                                    product = product,
                                    onClick = { component.selectProduct(product) }
                                )
                                if (index < searchResults.lastIndex) {
                                    HorizontalDivider(
                                        color = KarikaColors.Gray9,
                                        modifier = Modifier.padding(horizontal = 12.dp)
                                    )
                                }
                            }
                        }
                    }

                    YSpacer16()

                    // 2-col: Min. količina | Rabat %
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Min. količina
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = "Min. količina",
                                color = KarikaColors.Gray6,
                                textSize = 12.sp,
                                fontWeight = FontWeight.W600
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.Gray20)
                                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (minQty.isEmpty()) {
                                        KarikaText(
                                            text = "Opcionalno",
                                            color = KarikaColors.Gray7,
                                            textSize = 14.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                    BasicTextField(
                                        value = minQty,
                                        onValueChange = { component.setMinQty(it) },
                                        textStyle = TextStyle(
                                            color = KarikaColors.Gray2,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }

                        // Rabat %
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = "Rabat %",
                                color = KarikaColors.Gray6,
                                textSize = 12.sp,
                                fontWeight = FontWeight.W600
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.Gray20)
                                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(modifier = Modifier.weight(1f)) {
                                    if (discountPercent.isEmpty()) {
                                        KarikaText(
                                            text = "0",
                                            color = KarikaColors.Gray7,
                                            textSize = 14.sp,
                                            fontWeight = FontWeight.W400
                                        )
                                    }
                                    BasicTextField(
                                        value = discountPercent,
                                        onValueChange = { component.setDiscountPercent(it) },
                                        textStyle = TextStyle(
                                            color = KarikaColors.Gray2,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.W500
                                        ),
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                KarikaText(
                                    text = "%",
                                    color = KarikaColors.Gray6,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W600
                                )
                            }
                        }
                    }
                }
            }
        }

        // ── Footer ─────────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Sačuvaj
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(if (isSaving) KarikaColors.Gray9 else KarikaColors.Blue)
                    .clickable(
                        enabled = !isSaving,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.save() },
                contentAlignment = Alignment.Center
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        color = KarikaColors.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    KarikaText(
                        text = "Sačuvaj",
                        color = KarikaColors.White,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            // Odustani
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .border(1.dp, KarikaColors.Blue, RoundedCornerShape(18.dp))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.goBack() },
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Odustani",
                    color = KarikaColors.Blue,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
private fun ProductSearchRow(product: VendorProduct, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = product.name ?: "—",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            if (!product.sku.isNullOrBlank()) {
                KarikaText(
                    text = product.sku,
                    color = KarikaColors.Gray6,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
    }
}

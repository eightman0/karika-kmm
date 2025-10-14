package karika.distribucija.ba.ui.view.main.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.main.product.ProductQtyAction
import karika.distribucija.ba.ui.view.main.product.VendorName
import karika.distribucija.ba.ui.view.main.profile.LoginRequired
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_check_circle_filled
import karikav2.composeapp.generated.resources.ic_delete
import karikav2.composeapp.generated.resources.ic_gift
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CartView(component: CartComponent) {
    val items = component.stateHolder.cartHandler.cart.collectAsState()

    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        if (component.isGuest()) {
            LoginRequired(component)
        } else if (items.value.isEmpty()) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                color = KarikaColors.Primary,
                text = "Nema artikala u korpi.",
                textSize = 16.sp,
                fontWeight = FontWeight.W500
            )
        } else {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    color = KarikaColors.Black,
                    text = "Pregled korpe:",
                    fontWeight = FontWeight.W700,
                    textSize = 20.sp
                )
                LazyColumn(
                    modifier = Modifier
                        .hideKeyboard()
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                ) {
                    itemsIndexed(items.value.entries.toList()) { index, it ->
                        CartItem(it, component)
                        YSpacer16()
                        if (index != items.value.entries.toList().lastIndex) {
                            HorizontalDivider(
                                modifier = Modifier.fillMaxWidth(),
                                color = KarikaColors.Gray5,
                                thickness = 1.dp
                            )
                            YSpacer32()
                        }
                    }
                }
                PinnedFooter(component)
            }
        }
    }
}

@Composable
private fun PinnedFooter(component: CartComponent) {
    val cart by component.stateHolder.cartHandler.cart.collectAsState()

    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = KarikaColors.Divider
    )
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            color = KarikaColors.Gray2,
            text = "Ukupno sa PDV:",
            textSize = 16.sp,
            fontWeight = FontWeight.W500
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            text = cart.calculateTotal(),
            textSize = 20.sp,
            fontWeight = FontWeight.W700
        )
    }
    PrimaryButtonFilled(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        title = "Nastavi dalje",
        fontWeight = FontWeight.W700,
        textSize = 18.sp,
        enabled = true
    ) {
        if (cart.orderValid()) {
            component.shippingDetails()
        } else {
            component.showMessage("Nije zadovoljna minimalna količina po dobavljaču!")
        }
    }
}

@Composable
private fun CartItem(
    item: Map.Entry<Vendor, List<Pair<Product, Int>>>,
    component: CommonComponent
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        VendorName(Product(vendorName = item.key.name()), component)
        MinOrderAmount(item)
        item.value.forEach {
            ProductItem(it, component)
            YSpacer8()
        }
    }
}

@Composable
private fun MinOrderAmount(item: Map.Entry<Vendor, List<Pair<Product, Int>>>) {
    if (item.key.minOrderAmount() == null) {
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray6,
                atext = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Minimum: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append("${item.key.minOrderAmount()}KM")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Divider,
                            fontSize = 14.sp
                        )
                    ) {
                        append(" • ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Nedostaje: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append(
                            "${item.minAmountRest()}KM"
                        )
                    }
                },
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .height(6.dp)
                    .background(color = KarikaColors.Gray9, shape = CircleShape)
                    .weight(1f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .height(6.dp)
                            .background(color = KarikaColors.Green2, shape = CircleShape)
                            .weight(item.progress().first)
                    )
                    if (item.progress().second > 0) {
                        Box(
                            modifier = Modifier
                                .height(6.dp)
                                .weight(item.progress().second)
                        )
                    }
                }
            }
            Icon(
                imageVector = vectorResource(Res.drawable.ic_check_circle_filled),
                tint = if (item.minAmountRestValue()
                        .toDouble() > 0
                ) KarikaColors.Gray9 else KarikaColors.Green1,
                contentDescription = ""
            )
        }
    }
}

@Composable
private fun ProductItem(item: Pair<Product, Int>, component: CommonComponent) {
    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .onClick {
                    component.navigateToProduct(item.first)
                }
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Gray5)
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = item.first.image()
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
            ProductName(item.first)
            ProductBonus(item.first)
            ProductQtyAction(item.first, mutableStateOf(item.second), component, false)
        }
        Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                modifier = Modifier
                    .onClick {
                        component.removeFromCart(item.first)
                    },
                imageVector = vectorResource(Res.drawable.ic_delete),
                tint = KarikaColors.Black1,
                contentDescription = ""
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "VPC:",
                textSize = 12.sp,
                fontWeight = FontWeight.W300
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = item.first.vpcString(item.second),
                textSize = 12.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "VPC+PDV:",
                textSize = 12.sp,
                fontWeight = FontWeight.W300
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = item.first.vpcPdvString(item.second),
                textSize = 12.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
fun ProductName(product: Product) {
    KarikaText(
        modifier = Modifier,
        color = KarikaColors.Black,
        text = product.name(),
        textSize = 14.sp,
        maxLines = 2,
        fontWeight = FontWeight.W700
    )
}

@Composable
fun ProductBonus(product: Product) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .padding(4.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_gift),
            tint = KarikaColors.Green1,
            contentDescription = ""
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = product.bonusString(),
            textSize = 14.sp,
            fontWeight = FontWeight.W400
        )
    }
}

private fun Map<Vendor, List<Pair<Product, Int>>>.calculateTotal(): String {
    val total = values
        .flatten()
        .sumOf { (product, quantity) -> product.price() * quantity }

    return karikaPriceFormat(total * 1.17) + " KM"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRestValue(): String {
    return "${
        ((key.minOrderAmount()
            ?.toDoubleOrNull() ?: 0.0) - (value.sumOf { it.first.price() * it.second } * 1.17)).coerceAtLeast(
            0.0
        )
    }"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.minAmountRest(): String {
    return karikaPriceFormat(
        ((key.minOrderAmount()
            ?.toDoubleOrNull()
            ?: 0.0) - (value.sumOf { it.first.price() * it.second } * 1.17)).coerceAtLeast(
            0.0
        )
    )
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.progress(): Pair<Float, Float> {
    val min = key.minOrderAmount()?.toDoubleOrNull() ?: 0.0
    val current = (value.sumOf { it.first.price() * it.second } * 1.17).coerceAtLeast(0.0)
    if (current == 0.0) {
        return Pair(1f, 0f)
    }

    return Pair(
        (current / min).toFloat(),
        1f - (current / min).toFloat()
    )
}

private fun Map<Vendor, List<Pair<Product, Int>>>.orderValid(): Boolean {
    return all {
        it.minAmountRestValue().toDouble() == 0.0
    }
}

package karika.distribucija.ba.ui.view.main.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField3
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.bgWhite
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.toGrid
import karika.distribucija.ba.ui.view.main.home.DiscountView
import karika.distribucija.ba.ui.view.main.home.NewView
import karika.distribucija.ba.ui.view.main.home.NotAvailableOverlay
import karika.distribucija.ba.ui.view.main.home.ProductItem
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import karikav2.composeapp.generated.resources.ic_gift
import karikav2.composeapp.generated.resources.ic_navigation_cart
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProductView(component: ProductComponent) {
    val product by component.product.collectAsState()

    key(product.hashCode()) {
        KarikaScaffold(
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            component = component,
            topBar = {
                TopBarWithBack(product.name()) {
                    component.mainBack()
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .bgWhite()
                    .padding(16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .hideKeyboard()
                        .fillMaxSize()
                        .padding(it),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    if (product.createdAt == null) {
                        return@KarikaScaffold
                    }

                    VendorName(product, component)
                    ProductName(component)
                    ProductImage(component)
                    ProductPrice(component)
                    ProductDescription(component)
                    ProductAvailability(component)
                    ProductMinQty(component)
                    ProductBonus(component)
                    ProductButtons(component)
                    VendorProducts(component)
                }
            }
        }
    }
}

@Composable
fun VendorName(product: Product, component: CommonComponent) {
    if (component.isGuest()) {
        return
    }
    Row(
        modifier = Modifier
            .onClick {
                component.showVendor(product.toVendor())
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Blue,
            text = product.vendorName(),
            textSize = 14.sp,
            maxLines = 1,
            fontWeight = FontWeight.W600,
            decoration = TextDecoration.Underline
        )
    }
}

@Composable
fun ProductName(viewModel: ProductComponent) {
    val product by viewModel.product.collectAsState()
    KarikaText(
        modifier = Modifier,
        color = KarikaColors.Black,
        text = product.name(),
        textSize = 20.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
fun ProductImage(component: ProductComponent) {
    val product by component.product.collectAsState()
    val grid = gridColumnCount()

    Row {
        if (grid == 4) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )
        }
        Box(
            modifier = Modifier
                .onClick {
                    component.navigateToProduct(product)
                }
                .weight(1f)
                .border(width = 1.dp, color = KarikaColors.Gray5)
                .aspectRatio(1f),
        ) {
            Box(
                modifier = Modifier
                    .blur(radius = if (product.hasOnStock()) 0.dp else 5.dp)
                    .fillMaxSize()
            ) {
                KarikaImage(
                    modifier = Modifier
                        .onClick {
                            component.showImagePreview(product.image())
                        }
                        .fillMaxSize(),
                    model = product.image()
                )
                Column {
                    DiscountView(product)
                    NewView(product)
                }
            }
            NotAvailableOverlay(product)
        }
        if (grid == 4) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f),
            )
        }
    }
}

@Composable
fun ProductPrice(component: ProductComponent) {
    val product by component.product.collectAsState()
    val productQty = component.productQty.asState()

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (product.hasSpecialPrice()) {
                KarikaText(
                    color = KarikaColors.Gray2,
                    text = product.specialPriceString(),
                    textSize = 22.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier.drawBehind {
                        drawLine(
                            color = KarikaColors.Gray1,
                            strokeWidth = 1.dp.toPx(),
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2)
                        )
                    },
                    color = KarikaColors.Gray6,
                    text = product.priceString(),
                    textSize = 18.sp,
                    fontWeight = FontWeight.W500
                )
            } else {
                KarikaText(
                    color = KarikaColors.Gray2,
                    text = product.priceString(),
                    textSize = 22.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
        ProductQtyAction(product, productQty, component)
    }
}

@Composable
fun ProductDescription(viewModel: ProductComponent) {
    val product by viewModel.product.collectAsState()
    if (product.description.isNullOrEmpty()) {
        return
    }
    val showDescription = mutableStateOf(false).asState()

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        HtmlTextWithStyles(
            html = product.shortDescription ?: "",
            textColor = KarikaColors.Gray2
        )
        IconTextItem(
            modifier = Modifier
                .onClick {
                    showDescription.negate()
                },
            icon = vectorResource(if (showDescription.value) Res.drawable.ic_arrow_up else Res.drawable.ic_arrow_down),
            iconColor = KarikaColors.Gray2,
            textColor = KarikaColors.Gray2,
            textSize = 18.sp,
            fontWeight = FontWeight.W700,
            text = "Opis proizvoda",
            iconPosition = FabPosition.End
        )
        /* KarikaText(
             modifier = Modifier,
             color = KarikaColors.Black,
             text = product.description
                 ?: "",
             textSize = 16.sp,
             fontWeight = FontWeight.W400
         )*/
        if (showDescription.value) {
            HtmlTextWithStyles(
                html = product.description ?: "",
                textColor = KarikaColors.Gray2
            )
        }
    }
}

@Composable
fun ProductAvailability(viewModel: ProductComponent) {
    val product by viewModel.product.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray6,
            text = "Dostupnost:",
            textSize = 14.sp,
            fontWeight = FontWeight.W400
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = product.isInStockLabel().uppercase(),
            textSize = 14.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
fun ProductMinQty(component: ProductComponent) {
    val product by component.product.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray6,
            text = "Minimalna količina:",
            textSize = 14.sp,
            fontWeight = FontWeight.W400
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = "${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
            textSize = 14.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
fun ProductBonus(viewModel: ProductComponent) {
    val product by viewModel.product.collectAsState()
    Box(
        modifier = Modifier
            .background(color = KarikaColors.Green, shape = RoundedCornerShape(4.dp))
    ) {
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
                text = "Bonus za kupovinu proizvoda:",
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = product.bonusString(),
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

@Composable
fun ProductButtons(component: ProductComponent) {
    val product by component.product.collectAsState()
    val productQty by component.productQty.asState()

    if (product.hasOnStock()) {
        PrimaryButtonFilled(
            modifier = Modifier
                .height(56.dp)
                .fillMaxWidth(),
            title = "Dodaj u Korpu",
            icon = Res.drawable.ic_navigation_cart,
            enabled = product.hasOnStock()
        ) {
            component.addToCartWithPut(product, productQty)
        }
    }

    PrimaryButton(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        title = "Pošalji poruku dobavljaču",
    ) {
        component.sendMessageToVendor(product)
    }
}

@Composable
private fun VendorProducts(viewModel: ProductComponent) {
    val products by viewModel.products.collectAsState()
    if (products.size > 1) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = "Proizvodi istog dobavljača:",
            textSize = 20.sp,
            fontWeight = FontWeight.W700
        )
        products.toGrid()
            .forEach {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        it.forEach {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            ) {
                                ProductItem(it, viewModel)
                            }
                        }
                        repeat(gridColumnCount() - it.size) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                            )
                        }
                    }
                }
            }
    }
}

@Composable
fun ProductQtyAction(
    product: Product,
    qty: MutableState<Int>,
    component: CommonComponent,
    disableUpdate: Boolean = true,
    autoUpdate: Boolean = false
) {
    var pendingQty by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingQty) {
        pendingQty?.let { newQty ->
            delay(500)
            if (!disableUpdate) {
                component.updateCart(product, newQty)
            }
            pendingQty = null
        }
    }

    Row {
        Box(
            modifier = Modifier
                .onClick {
                    if (qty.value == product.minQty()) {
                        return@onClick
                    }
                    qty.value -= 1
                    pendingQty = qty.value
                }
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border,
                    shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "-",
                textSize = 22.sp,
                fontWeight = FontWeight.W600
            )
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(80.dp)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaTextField3(
                modifier = Modifier
                    .fillMaxSize(),
                value = qty.value.toString(),
                onValueChange = {
                    if ((it.toIntOrNull() ?: 0) < product.minQty()) {
                        return@KarikaTextField3
                    }
                    qty.value = it.toIntOrNull() ?: qty.value
                    if (autoUpdate) {
                        component.updateCart(product, qty.value)
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .onClick {
                    qty.value += 1
                    pendingQty = qty.value
                }
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border,
                    shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "+",
                textSize = 22.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}
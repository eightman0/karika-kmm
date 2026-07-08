package karika.distribucija.ba.ui.view.shop.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.CarouselBanners
import karika.distribucija.ba.ui.components.CarouselLogos
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.toGrid
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.product.VendorName
import karika.distribucija.ba.util.KarikaConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_cart_add
import karikav2.composeapp.generated.resources.ic_gift
import org.jetbrains.compose.resources.vectorResource

@Composable
fun HomeView(component: HomeComponent) {
    val state = rememberLazyListState()
    val promotedLogos by component.promotedLogos.collectAsState()
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = state
        ) {
            item {
                CarouselBanners(component)
                YSpacer8()
            }
            item {
                KarikaProducts(component)
                YSpacer8()
            }
            item {
                if (promotedLogos.isNotEmpty()) {
                    KarikaText(
                        modifier = Modifier,
                        color = KarikaColors.Black,
                        text = "Dobavljači",
                        textSize = 20.sp,
                        fontWeight = FontWeight.W700
                    )
                    YSpacer8()
                    CarouselLogos(component)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        state.scrollToItem(0)
        component.loadData()
    }
}

@Composable
fun ProductItem(
    product: Product,
    component: CommonComponent,
    hideVendor: Boolean = false,
    showMinQty: Boolean = false
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .onClick {
                    component.navigateToProduct(product)
                }
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Gray5)
                .aspectRatio(1f),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .blur(radius = if (!product.hasOnStock()) 5.dp else 0.dp)
                    .background(Color.Black.copy(alpha = 0.3f)),
            ) {
                KarikaImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    model = product.image()
                )
            }
            Column {
                BonusView(product)
                DiscountView(product)
                NewView(product)
            }
            AddToCartButton(product, component)
            NotAvailableOverlay(product)
        }
        if (showMinQty) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "Min. kol: ${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
                textSize = 14.sp,
                fontWeight = FontWeight.W500
            )
        }
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            text = product.currentPriceString(),
            textSize = 18.sp,
            fontWeight = FontWeight.W700
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = product.name(),
            textSize = 14.sp,
            maxLines = 3,
            fontWeight = FontWeight.W600
        )
        if (!hideVendor && !component.isGuest()) {
            VendorName(product, component)
        }
    }
}

@Composable
private fun KarikaProducts(component: HomeComponent) {
    val newArrivals by component.newArrivals.collectAsState()

    if (newArrivals.isNotEmpty()) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                color = KarikaColors.Black,
                text = "Karika preporučuje:",
                textSize = 20.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .onClick {
                        component.mainNavigate(
                            MainConfig.CategoryProducts(
                                Category(
                                    id = KarikaConfig.getKarikaProductsId(),
                                    name = "Karika preporučuje"
                                )
                            )
                        )
                    },
                color = KarikaColors.Black,
                text = "Vidi sve",
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
        YSpacer8()
        newArrivals.toGrid().forEach {
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
                        ProductItem(it, component)
                    }
                }
                if (it.size == 1) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
            YSpacer16()
        }
    }
}


@Composable
fun BonusView(product: Product) {
    if (!product.hasBonus()) {
        return
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(color = KarikaColors.Green, shape = RoundedCornerShape(4.dp))
    ) {
        Row(
            modifier = Modifier
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_gift),
                tint = KarikaColors.Gray2,
                contentDescription = ""
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
private fun AddToCartButton(product: Product, component: CommonComponent) {
    if (product.hasOnStock()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.BottomEnd
        ) {
            Box(
                modifier = Modifier
                    .onClick {
                        component.addToCart(product, product.minQty())
                    }
                    .padding(8.dp)
                    .size(40.dp)
                    .background(color = KarikaColors.Primary, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_cart_add),
                    tint = KarikaColors.White,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun NotAvailableOverlay(product: Product) {
    if (!product.hasOnStock()) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .background(color = KarikaColors.Primary, shape = RoundedCornerShape(4.dp)),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    color = KarikaColors.White,
                    text = "RASPRODANO",
                    textSize = 14.sp,
                    maxLines = 1,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }
}

@Composable
fun DiscountView(product: Product) {
    if (!product.hasSpecialPrice()) {
        return
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(color = KarikaColors.Primary, shape = RoundedCornerShape(4.dp))
    ) {
        KarikaText(
            modifier = Modifier
                .padding(4.dp),
            color = KarikaColors.White,
            text = "-" + product.calculatePercent() + "%",
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
fun NewView(product: Product) {
    if (!product.isNew()) {
        return
    }

    Box(
        modifier = Modifier
            .padding(8.dp)
            .background(color = KarikaColors.Green, shape = RoundedCornerShape(4.dp))
    ) {
        KarikaText(
            modifier = Modifier
                .padding(4.dp),
            color = KarikaColors.White,
            text = "Novo",
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}
package karika.distribucija.ba.ui.view.main.menu.categories.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaPickerSmall
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.main.home.ProductItem
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_cart_add
import karikav2.composeapp.generated.resources.ic_filter_alt
import karikav2.composeapp.generated.resources.ic_tertiary
import karikav2.composeapp.generated.resources.star_outline
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProductByCategoryView(component: ProductByCategoryComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack(component.category.value.name) {
                component.mainBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            Products(component)
        }
    }
}

@Composable
private fun Products(component: ProductByCategoryComponent) {
    val products by component.products.collectAsState()
    val state = rememberLazyListState()
    val gridColumnCount = gridColumnCount()

    LazyColumn(
        state = state,
        modifier = Modifier
            .hideKeyboard()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Filter(component)
        }
        item {
            FeaturedProducts(component)
        }
        items(items = products.chunked(gridColumnCount)) { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        ProductItem(it, component, showMinQty = true)
                    }
                }
                repeat(gridColumnCount - item.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }
        item {
            EmptyState(component)
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }
}

@Composable
private fun Filter(component: ProductByCategoryComponent) {
    val showState = remember { mutableStateOf(false) }
    val searchText = component.searchText.asState()
    val filter = component.filter.asState()
    val sort = component.sortBy.asState()
    val selectedVendor = component.selectedVendor.asState()
    val selectedRegions = component.selectedRegion.asState()
    val isInStock = component.isInStock.asState()

    SearchBoxBorder(
        modifier = Modifier
            .fillMaxWidth(),
        onValueChange = {
            searchText.value = it
        },
        onClose = {
            searchText.value = ""
            component.loadNextPage(true)
        },
        onSearchExecute = {
            component.loadNextPage(true)
        }
    )
    YSpacer16()
    Row(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .onClick {
                    showState.negate()
                }
                .fillMaxHeight()
                .weight(1f)
                .border(width = 1.dp, color = KarikaColors.Gray1, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconTextItem(
                modifier = Modifier,
                icon = vectorResource(Res.drawable.ic_filter_alt),
                iconColor = KarikaColors.Black1,
                iconSize = 16.dp,
                text = "Filteri",
                textColor = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W400,
                iconPosition = FabPosition.End
            )
        }
        KarikaPickerSmall(
            modifier = Modifier
                .weight(1f),
            padding = 4.dp,
            borderColor = KarikaColors.Gray2,
            value = sort,
            values = mutableStateOf(
                listOf(
                    "Najnoviji",
                    "Najstariji",
                    "Najjeftiniji",
                    "Najskuplji",
                    "Min. Količina",
                    "Po datumu",
                    "Sa popustom"
                )
            ).asState()
        ) {
            component.loadNextPage(reset = true)
        }
    }

    if (filter.value.first.isNotBlank()
        || selectedRegions.value.isNotEmpty() || selectedVendor.value.second != 0 || isInStock.value == "1"
    ) {
        YSpacer16()
        FlowRow(
            modifier = Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(vertical = 4.dp),
                color = KarikaColors.Black,
                textSize = 16.sp,
                fontWeight = FontWeight.W700,
                text = "Uključeni filter: "
            )
            if (selectedVendor.value.second != 0) {
                Box(
                    modifier = Modifier
                        .rounded(color = KarikaColors.Gray19)
                ) {
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                selectedVendor.value = Pair("", 0)
                                component.loadNextPage(true)
                            }
                            .padding(4.dp),
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        iconColor = KarikaColors.Black1,
                        iconSize = 16.dp,
                        text = selectedVendor.value.first,
                        textColor = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        iconPosition = FabPosition.End
                    )
                }
            }
            selectedRegions.value.forEach {
                Box(
                    modifier = Modifier
                        .rounded(color = KarikaColors.Gray19)
                ) {
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                selectedRegions.value = selectedRegions.value.minus(it)
                                component.loadNextPage(true)
                            }
                            .padding(4.dp),
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        iconColor = KarikaColors.Black1,
                        iconSize = 16.dp,
                        text = it.label(),
                        textColor = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        iconPosition = FabPosition.End
                    )
                }
            }
            if (isInStock.value == "1") {
                Box(
                    modifier = Modifier
                        .rounded(color = KarikaColors.Gray19)
                ) {
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                isInStock.value = ""
                                component.loadNextPage(true)
                            }
                            .padding(4.dp),
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        iconColor = KarikaColors.Black1,
                        iconSize = 16.dp,
                        text = "Prikaži rasprodate",
                        textColor = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        iconPosition = FabPosition.End
                    )
                }
            }
        }
    }

    ProductsFilterSheet(showState, component)
}

@Composable
private fun FeaturedProducts(component: ProductByCategoryComponent) {
    val featuredProducts by component.featuredProducts.collectAsState()

    LaunchedEffect(Unit) {
        component.loadFeatureProducts()
    }

    if (featuredProducts.isNotEmpty()) {
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
                    color = KarikaColors.Black,
                    text = "ISTAKNUTI ARTIKLI",
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = KarikaColors.Primary.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    KarikaText(
                        text = "Sponzorisano",
                        color = KarikaColors.Gray2,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val cardWidth = maxWidth * 0.7f
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(start = 4.dp, end = maxWidth * 0.2f)
                ) {
                    items(items = featuredProducts) { product ->
                        FeaturedProductItem(
                            modifier = Modifier
                                .width(cardWidth),
                            product = product,
                            component = component
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedProductItem(
    modifier: Modifier,
    product: Product,
    component: ProductByCategoryComponent
) {
    Card(
        modifier = modifier
            .onClick {
                component.navigateToProduct(product)
            }
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
        ),
        shape = RoundedCornerShape(12.dp),

        ) {
        Column(
            modifier = Modifier
                .background(color = KarikaColors.White)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .background(color = KarikaColors.Gray5, shape = RoundedCornerShape(16.dp))
                        .border(
                            color = KarikaColors.Gray5,
                            shape = RoundedCornerShape(16.dp),
                            width = 1.dp
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaImage(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .fillMaxWidth(),
                        model = product.image(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(color = KarikaColors.Primary, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            modifier = Modifier.size(12.dp),
                            imageVector = vectorResource(Res.drawable.star_outline),
                            contentDescription = null,
                            tint = KarikaColors.White,
                        )
                        KarikaText(
                            text = "ISTAKNUTO",
                            color = KarikaColors.White,
                            textSize = 10.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }

            YSpacer16()
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                color = KarikaColors.Black,
                text = product.name(),
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 3
            )
            YSpacer8()
            KarikaText(
                modifier = Modifier
                    .height(40.dp)
                    .fillMaxWidth(),
                color = KarikaColors.Secondary,
                text = "${product.vendorName()} ・ ${component.getUnit(product.minQtyUnit())} ・ Min. ${product.minQty()}",
                textSize = 14.sp,
                fontWeight = FontWeight.W400,
                maxLines = 2
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = product.currentPriceString(),
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )

            if (product.hasOnStock()) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
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
    }
}

@Composable
private fun EmptyState(component: ProductByCategoryComponent) {
    val products by component.products.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (products.isEmpty() && !loader) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Primary,
                textSize = 16.sp,
                fontWeight = FontWeight.W700,
                text = "Nema rezultata."
            )
        }
    }
}

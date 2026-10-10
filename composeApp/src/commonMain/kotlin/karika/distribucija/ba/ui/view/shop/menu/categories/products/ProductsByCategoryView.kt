package karika.distribucija.ba.ui.view.shop.menu.categories.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.components.KAddButton
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.shop.vendor.details.KarikaProductCard
import karika.distribucija.ba.ui.view.shop.vendor.details.KarikaSearchInput
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_filter
import karikav2.composeapp.generated.resources.ic_k_sort
import karikav2.composeapp.generated.resources.ic_k_star
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProductByCategoryView(component: ProductByCategoryComponent) {
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // White behind the status bar if it is not already handled by the shell
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
                KBackHeader(
                    title = component.category.value.name,
                    onBack = {
                        component.mainBack()
                    }
                )
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
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
            .fillMaxSize()
            .hideKeyboard(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
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
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item.forEach {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        KarikaProductCard(it, component, showMinQty = true)
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
    val hasFilter = filter.value.first.isNotBlank()
            || selectedRegions.value.isNotEmpty() || selectedVendor.value.second != 0 || isInStock.value == "1"

    val showSort = remember { mutableStateOf(false) }
    val sortChanged = sort.value != DEFAULT_SORT

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaSearchInput(
                modifier = Modifier
                    .weight(1f),
                placeholder = "Pretraži proizvode…",
                initial = searchText.value,
                onValueChange = {
                    searchText.value = it
                },
                onClear = {
                    searchText.value = ""
                    component.loadNextPage(true)
                },
                onSearch = {
                    component.loadNextPage(true)
                }
            )
            HeaderIconButton(
                modifier = Modifier.testTag(PRODUCTS_FILTER_TAG),
                icon = vectorResource(Res.drawable.ic_k_filter),
                active = hasFilter
            ) {
                showState.negate()
            }
            HeaderIconButton(
                modifier = Modifier.testTag(PRODUCTS_SORT_TAG),
                icon = vectorResource(Res.drawable.ic_k_sort),
                active = sortChanged
            ) {
                showSort.value = true
            }
        }

        if (sortChanged) {
            Spacer(Modifier.height(12.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    text = "Sortiranje: "
                )
                KChip(
                    text = sort.value,
                    selected = true,
                    trailingIcon = vectorResource(Res.drawable.ic_tertiary)
                ) {
                    sort.value = DEFAULT_SORT
                    component.loadNextPage(reset = true)
                }
            }
        }

        if (hasFilter) {
            Spacer(Modifier.height(12.dp))
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .align(Alignment.CenterVertically),
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600,
                    text = "Uključeni filter: "
                )
                if (selectedVendor.value.second != 0) {
                    KChip(
                        text = selectedVendor.value.first,
                        selected = true,
                        trailingIcon = vectorResource(Res.drawable.ic_tertiary)
                    ) {
                        selectedVendor.value = Pair("", 0)
                        component.loadNextPage(true)
                    }
                }
                selectedRegions.value.forEach {
                    KChip(
                        text = it.label(),
                        selected = true,
                        trailingIcon = vectorResource(Res.drawable.ic_tertiary)
                    ) {
                        selectedRegions.value = selectedRegions.value.minus(it)
                        component.loadNextPage(true)
                    }
                }
                if (isInStock.value == "1") {
                    KChip(
                        text = "Prikaži rasprodate",
                        selected = true,
                        trailingIcon = vectorResource(Res.drawable.ic_tertiary)
                    ) {
                        isInStock.value = ""
                        component.loadNextPage(true)
                    }
                }
            }
        }
    }

    ProductsFilterSheet(showState, component)
    SortSheet(
        show = showSort,
        selected = sort.value
    ) {
        sort.value = it
        component.loadNextPage(reset = true)
    }
}

/** Test tags of the filter and sort buttons next to the search, for the end-to-end tests. */
const val PRODUCTS_FILTER_TAG = "products_filter"
const val PRODUCTS_SORT_TAG = "products_sort"

private const val DEFAULT_SORT = "Najnoviji"

private val SORT_OPTIONS = listOf(
    "Najnoviji",
    "Najstariji",
    "Najjeftiniji",
    "Najskuplji",
    "Min. Količina",
    "Po datumu",
    "Sa popustom"
)

/** Square button next to the search; navy when its filter or sort is in use. */
@Composable
private fun HeaderIconButton(
    modifier: Modifier,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Box(
        modifier = modifier
            .size(46.dp)
            .clip(shape)
            .background(if (active) KarikaUiColors.Ink else KarikaColors.White)
            .border(1.dp, if (active) KarikaUiColors.Ink else KarikaUiColors.Border, shape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KIcon(
            icon = icon,
            tint = if (active) KarikaColors.White else KarikaUiColors.Ink,
            size = 20.dp
        )
    }
}

/** Sort options as a bottom sheet, the chosen one marked with a pink check. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SortSheet(
    show: MutableState<Boolean>,
    selected: String,
    onSelect: (String) -> Unit,
) {
    if (!show.value) {
        return
    }
    ModalBottomSheet(
        onDismissRequest = { show.value = false },
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Bottom) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KarikaUiColors.Border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 16.dp)
        ) {
            KarikaText(
                text = "Sortiraj po",
                color = KarikaUiColors.Ink,
                textSize = 22.sp,
                lineHeight = 28.sp,
                fontWeight = FontWeight.W800
            )
            Spacer(Modifier.height(14.dp))
            KCard(modifier = Modifier.fillMaxWidth()) {
                SORT_OPTIONS.forEachIndexed { index, option ->
                    if (index > 0) {
                        KDivider()
                    }
                    val isSelected = option == selected
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                show.value = false
                                if (!isSelected) {
                                    onSelect(option)
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier.weight(1f),
                            text = option,
                            color = if (isSelected) KarikaUiColors.Pink else KarikaUiColors.Ink,
                            textSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500,
                            maxLines = 1
                        )
                        if (isSelected) {
                            KIcon(
                                icon = vectorResource(Res.drawable.ic_k_check),
                                tint = KarikaUiColors.Pink,
                                size = 20.dp
                            )
                        }
                    }
                }
            }
        }
    }
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = "Istaknuti artikli",
                    color = KarikaUiColors.Ink,
                    textSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                KPill(
                    text = "Sponzorisano",
                    background = KarikaUiColors.PinkSoft,
                    color = KarikaUiColors.Pink,
                    textSize = 11.sp
                )
            }

            BoxWithConstraints(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                val cardWidth = maxWidth * 0.75f
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
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
    KCard(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        onClick = {
            component.navigateToProduct(product)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                KImage(
                    modifier = Modifier
                        .size(88.dp)
                        .clip(RoundedCornerShape(14.dp)),
                    url = product.image()
                )
                Spacer(Modifier.weight(1f))
                KPill(
                    text = "ISTAKNUTO",
                    background = KarikaUiColors.Pink,
                    color = KarikaColors.White,
                    icon = vectorResource(Res.drawable.ic_k_star),
                    textSize = 11.sp
                )
            }
            Spacer(Modifier.height(12.dp))
            KarikaText(
                modifier = Modifier.fillMaxWidth(),
                text = product.name(),
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W600,
                maxLines = 3
            )
            Spacer(Modifier.height(6.dp))
            KarikaText(
                modifier = Modifier.fillMaxWidth(),
                text = "${product.vendorName()} ・ ${component.getUnit(product.minQtyUnit())} ・ Min. ${product.minQty()}",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 2
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = product.currentPriceString(),
                    color = KarikaUiColors.Ink,
                    textSize = 17.sp,
                    lineHeight = 21.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                if (product.hasOnStock()) {
                    KAddButton {
                        component.addToCart(product, product.minQty())
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
        KEmptyState(
            modifier = Modifier.height(200.dp),
            text = "Nema rezultata."
        )
    }
}

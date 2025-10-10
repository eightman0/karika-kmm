package karika.distribucija.ba.ui.view.main.menu.categories.products

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPickerSmall
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.main.home.ProductItem
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_filter_alt
import karikav2.composeapp.generated.resources.ic_tertiary
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
        items(items = products.chunked(2)) { item ->
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
                        ProductItem(it, component)
                    }
                }
                if (item.size == 1) {
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
                    "Po datumu"
                )
            ).asState()
        ) {
            component.loadNextPage(reset = true)
        }
    }

    if (filter.value.first.isNotBlank()
        || selectedRegions.value.isNotEmpty() || selectedVendor.value.second != 0
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
        }
    }

    ProductsFilterSheet(showState, component)
}

@Composable
private fun EmptyState(component: ProductByCategoryComponent) {
    val products by component.products.collectAsState()
    if (products.isNotEmpty()) {
        return
    }
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
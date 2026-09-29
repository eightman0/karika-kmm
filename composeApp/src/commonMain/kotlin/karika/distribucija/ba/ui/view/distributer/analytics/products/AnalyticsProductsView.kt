package karika.distribucija.ba.ui.view.distributer.analytics.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AnalyticsCategory
import karika.distribucija.ba.domain.model.AnalyticsProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsEmptyState
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsProgressBar
import karika.distribucija.ba.ui.view.distributer.analytics.formatNumber
import karika.distribucija.ba.util.karikaPriceFormat

@Composable
fun AnalyticsProductsView(component: AnalyticsProductsComponent) {
    val selectedSubTab by component.selectedSubTab.collectAsState()
    val searchQuery by component.searchQuery.collectAsState()
    val products by component.products.collectAsState()
    val categories by component.categories.collectAsState()
    val loader by component.loader.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            text = "Proizvodi i kategorije",
            color = KarikaColors.Gray2,
            textSize = 20.sp,
            fontWeight = FontWeight.W700
        )

        SegmentedControl(selectedSubTab = selectedSubTab, component = component)

        when (selectedSubTab) {
            ProductsSubTab.Products -> ProductsList(searchQuery, products, component, loader)
            ProductsSubTab.Categories -> CategoriesList(categories, loader)
        }
    }
}

@Composable
private fun SegmentedControl(selectedSubTab: ProductsSubTab, component: AnalyticsProductsComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(KarikaColors.Gray20)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SegmentButton(
            modifier = Modifier.weight(1f),
            text = "Proizvodi",
            selected = selectedSubTab == ProductsSubTab.Products
        ) { component.selectSubTab(ProductsSubTab.Products) }
        SegmentButton(
            modifier = Modifier.weight(1f),
            text = "Kategorije",
            selected = selectedSubTab == ProductsSubTab.Categories
        ) { component.selectSubTab(ProductsSubTab.Categories) }
    }
}

@Composable
private fun SegmentButton(modifier: Modifier = Modifier, text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) KarikaColors.Blue else KarikaColors.Transparent)
            .onClick(callback = onClick)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        KarikaText(
            text = text,
            color = if (selected) KarikaColors.White else KarikaColors.Gray2,
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
private fun ProductsList(
    searchQuery: String,
    products: List<AnalyticsProduct>,
    component: AnalyticsProductsComponent,
    loader: Boolean
) {
    SearchBoxBorder(
        modifier = Modifier.fillMaxWidth(),
        placeholder = "Pretraži proizvode",
        onValueChange = { component.search(it) },
        onSearchExecute = { component.search(it) }
    )

    val filtered = products.filter {
        searchQuery.isBlank() ||
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.sku.contains(searchQuery, ignoreCase = true)
    }

    if (filtered.isEmpty()) {
        AnalyticsEmptyState(loader)
    }

    filtered.forEach { product ->
        ProductRow(product)
    }
}

@Composable
private fun ProductRow(product: AnalyticsProduct) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            KarikaText(text = product.name, color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W600)
            KarikaText(
                text = "${product.sku} · ${product.qty} kom · ${product.orders} narudžbi",
                color = KarikaColors.Gray7,
                textSize = 12.sp,
                fontWeight = FontWeight.W400
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            KarikaText(text = "${karikaPriceFormat(product.revenue)} KM", color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W700)
            KarikaText(text = "↑ +${formatNumber(product.growthPercent)}%", color = KarikaColors.Green3, textSize = 12.sp, fontWeight = FontWeight.W500)
        }
    }
}

@Composable
private fun CategoriesList(categories: List<AnalyticsCategory>, loader: Boolean) {
    KarikaText(
        text = "Proizvod može pripadati više kategorija, pa zbroj prihoda po kategorijama nije jednak ukupnom prihodu.",
        color = KarikaColors.Gray7,
        textSize = 12.sp,
        fontWeight = FontWeight.W400
    )

    if (categories.isEmpty()) {
        AnalyticsEmptyState(loader)
    }

    val maxRevenue = (categories.maxOfOrNull { it.revenue } ?: 1.0).coerceAtLeast(1.0)

    categories.forEach { category ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(8.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                KarikaText(text = category.name, color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W700)
                KarikaText(text = "${karikaPriceFormat(category.revenue)} KM", color = KarikaColors.Gray2, textSize = 15.sp, fontWeight = FontWeight.W700)
            }
            AnalyticsProgressBar(progress = (category.revenue / maxRevenue).toFloat())
            YSpacer8()
            KarikaText(
                text = "${category.qty} kom   ${category.orders} narudžbi   ↑ +${formatNumber(category.growthPercent)}%",
                color = KarikaColors.Gray7,
                textSize = 12.sp,
                fontWeight = FontWeight.W400
            )
        }
    }
}

package karika.distribucija.ba.ui.view.shop.menu.categories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_left
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import org.jetbrains.compose.resources.vectorResource

/** Name of the "all products" entry that [CategoriesComponent] puts in front of the categories. */
private const val ALL_PRODUCTS_NAME = "SVI PROIZVODI"

@Composable
fun CategoriesView(component: CategoriesComponent) {
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            KBackHeader(title = "Kategorije proizvoda", onBack = { component.mainBack() })
        },
        component = component
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
        ) {
            Categories(component)
        }
    }
}

@Composable
private fun Categories(component: CategoriesComponent) {
    val categories by component.categories.collectAsState()
    val category by component.subCategory.collectAsState()

    val allProducts = if (category == null) {
        categories.firstOrNull { it.name.equals(ALL_PRODUCTS_NAME, ignoreCase = true) }
    } else {
        null
    }
    val gridItems = (categories.find { it.id == category?.id }?.childrenData ?: categories)
        .filter { it !== allProducts }

    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        val selected = category
        if (selected != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                SubCategoryHeader(selected) {
                    component.reset()
                }
            }
            item(span = { GridItemSpan(maxLineSpan) }) {
                PinkRow(text = "Vidi sve u ${selected.name.lowercase()}") {
                    component.showProducts(selected)
                }
            }
        }
        if (allProducts != null) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                PinkRow(text = "Svi proizvodi") {
                    component.showProducts(allProducts)
                }
            }
        }
        items(items = gridItems) {
            CategoryCard(name = it.name) {
                if (category != null || it.childrenData.isEmpty()) {
                    component.showProducts(it)
                } else {
                    component.onSelectCategory(it)
                }
            }
        }
    }
}

/** Selected parent category with a way back to all categories. */
@Composable
private fun SubCategoryHeader(category: Category, onBack: () -> Unit) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = onBack
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaUiColors.Field),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_left), size = 18.dp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = "Sve kategorije",
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 1
                )
                KarikaText(
                    text = category.name,
                    color = KarikaUiColors.Ink,
                    textSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 2
                )
            }
        }
    }
}

/** Full-width pink row, e.g. "Svi proizvodi". */
@Composable
private fun PinkRow(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(KarikaUiColors.Pink)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaColors.White,
            textSize = 15.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        Spacer(Modifier.width(12.dp))
        KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaColors.White, size = 18.dp)
    }
}

/** White category card with the name and a small pink chevron at the bottom right. */
@Composable
private fun CategoryCard(name: String?, onClick: () -> Unit) {
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            KarikaText(
                text = name,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2
            )
            KIcon(
                modifier = Modifier.align(Alignment.End),
                icon = vectorResource(Res.drawable.ic_k_chevron_right),
                tint = KarikaUiColors.Pink,
                size = 14.dp
            )
        }
    }
}

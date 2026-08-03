package karika.distribucija.ba.ui.view.salesrep.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.karikaFonts
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_cancel
import karikav2.composeapp.generated.resources.ic_cart_add
import karikav2.composeapp.generated.resources.ic_check_circle_filled
import karikav2.composeapp.generated.resources.ic_close
import karikav2.composeapp.generated.resources.ic_navigation_category
import karikav2.composeapp.generated.resources.ic_products
import karikav2.composeapp.generated.resources.ic_search
import karikav2.composeapp.generated.resources.ic_shopping_cart
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesOrderCatalogView(component: SalesOrderCatalogComponent) {
    val selectedTab by component.selectedTab.collectAsState()
    val products by component.products.collectAsState()
    val searchText by component.searchText.collectAsState()
    val selectedCategory by component.selectedCategory.collectAsState()
    val cartItems by component.cartItems.collectAsState()
    val cartCount by component.cartCount.collectAsState()
    val isLoading by component.isLoading.collectAsState()
    val isLoadingMore by component.isLoadingMore.collectAsState()
    val hasNext by component.hasNext.collectAsState()
    val allCategories by component.stateHolder.commonHandler.categories.collectAsState()
    val allFlatCategories =
        remember(allCategories) { flattenToDepth(allCategories, emptyList(), 3) }
    val selectedCategoryPath = remember(selectedCategory, allFlatCategories) {
        selectedCategory?.let { cat ->
            val flat = allFlatCategories.find { it.category.id == cat.id }
            (flat?.ancestors ?: emptyList()) + cat
        } ?: emptyList()
    }

    val listState = rememberLazyListState()
    val reachedEnd by remember {
        derivedStateOf {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1
            lastVisible >= listState.layoutInfo.totalItemsCount - 3
        }
    }
    LaunchedEffect(reachedEnd) {
        if (reachedEnd && hasNext && !isLoadingMore && !isLoading) {
            component.loadNextPage()
        }
    }

    var showCategorySheet by remember { mutableStateOf(false) }
    var categorySheetInitialStack by remember { mutableStateOf<List<Category>>(emptyList()) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    var fabOffsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(KarikaColors.Gray20)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Search + category button ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(KarikaColors.Gray20)
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_search),
                        contentDescription = "",
                        tint = KarikaColors.Gray6,
                        modifier = Modifier.size(18.dp)
                    )
                    Box(modifier = Modifier.weight(1f)) {
                        if (searchText.isEmpty()) {
                            KarikaText(
                                text = "Pretraži artikle...",
                                color = KarikaColors.Gray7,
                                textSize = 14.sp
                            )
                        }
                        BasicTextField(
                            value = searchText,
                            onValueChange = component::setSearch,
                            textStyle = TextStyle(
                                fontFamily = karikaFonts(),
                                fontSize = 14.sp,
                                color = KarikaColors.Gray2
                            ),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (searchText.isNotEmpty()) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_cancel),
                            contentDescription = "Obriši",
                            tint = KarikaColors.Gray6,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { component.setSearch("") }
                        )
                    }
                }

                // Category filter button (only applies to the full catalog)
                if (selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(if (selectedCategory != null) KarikaColors.Blue else KarikaColors.Gray20)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                categorySheetInitialStack = emptyList()
                                showCategorySheet = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_navigation_category),
                            contentDescription = "Kategorije",
                            tint = if (selectedCategory != null) KarikaColors.White else KarikaColors.Gray6,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ── Tabs ────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(KarikaColors.White)
                    .padding(start = 16.dp, end = 16.dp, bottom = 14.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CatalogTabPill(
                    label = "Svi artikli",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) }
                CatalogTabPill(
                    label = "Na akciji",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.ON_SALE
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.ON_SALE) }
                CatalogTabPill(
                    label = "Prethodno naručeno",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED) }
            }

            // ── Active category chip ───────────────────────────────────────────
            if (selectedCategory != null && selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .padding(start = 16.dp, end = 16.dp, bottom = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(KarikaColors.Blue.copy(alpha = 0.1f))
                            .border(
                                1.dp,
                                KarikaColors.Blue.copy(alpha = 0.2f),
                                RoundedCornerShape(14.dp)
                            )
                            .padding(start = 12.dp, end = 6.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        FlowRow(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedCategoryPath.forEachIndexed { index, pathCategory ->
                                val isLastLevel = index == selectedCategoryPath.lastIndex
                                KarikaText(
                                    modifier = if (isLastLevel) Modifier else Modifier
                                        .clickable(
                                            indication = null,
                                            interactionSource = remember { MutableInteractionSource() }
                                        ) {
                                            categorySheetInitialStack =
                                                selectedCategoryPath.subList(0, index + 1)
                                            showCategorySheet = true
                                        },
                                    text = pathCategory.name,
                                    color = KarikaColors.Blue,
                                    textSize = 12.sp,
                                    fontWeight = FontWeight.W600
                                )
                                if (!isLastLevel) {
                                    KarikaText(
                                        text = "->",
                                        color = KarikaColors.Blue,
                                        textSize = 12.sp,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(KarikaColors.Blue)
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { component.selectCategory(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_close),
                                contentDescription = "Ukloni",
                                tint = KarikaColors.White,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            // ── Product list ───────────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = KarikaColors.Blue)
                    }
                }

                products.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_products),
                                contentDescription = "",
                                tint = KarikaColors.Gray9,
                                modifier = Modifier.size(48.dp)
                            )
                            KarikaText(
                                text = when (selectedTab) {
                                    SalesOrderCatalogComponent.CatalogTab.ON_SALE -> "Trenutno nema artikala na akciji"
                                    SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED -> "Ovaj kupac nema prethodno naručenih artikala"
                                    SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS -> "Nema artikala"
                                },
                                color = KarikaColors.Gray6,
                                textSize = 15.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(products, key = { it.key }) { product ->
                            ProductCard(
                                product = product,
                                cartQty = cartItems[product.key]?.second ?: 0,
                                onAdd = { qty -> component.setCartQty(product, qty) }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = KarikaColors.Blue,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
        }

        // ── Floating draggable cart FAB ────────────────────────────────────────
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp)
                .offset { IntOffset(0, fabOffsetY.roundToInt()) }
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        fabOffsetY += dragAmount.y
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(KarikaColors.Primary)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        component.openCart()
                    },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_shopping_cart),
                    contentDescription = "Korpa",
                    tint = KarikaColors.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(KarikaColors.Blue)
                        .border(2.dp, KarikaColors.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "$cartCount",
                        color = KarikaColors.White,
                        textSize = 9.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
    }

    // ── Category bottom sheet ──────────────────────────────────────────────────
    if (showCategorySheet) {
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            sheetGesturesEnabled = false
        ) {
            CategorySheet(
                allCategories = allCategories,
                selectedCategory = selectedCategory,
                initialNavStack = categorySheetInitialStack,
                onSelect = { category ->
                    component.selectCategory(category)
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCategorySheet = false
                    }
                },
                onDismiss = {
                    coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                        showCategorySheet = false
                    }
                }
            )
        }
    }
}

// ── Catalog tab pill ──────────────────────────────────────────────────────────

@Composable
private fun CatalogTabPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) KarikaColors.Blue else KarikaColors.Gray20)
            .border(
                width = 1.dp,
                color = if (selected) KarikaColors.Blue else KarikaColors.Gray9,
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 14.dp, vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = label,
            color = if (selected) KarikaColors.White else KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

// ── Product card ───────────────────────────────────────────────────────────────

@Composable
private fun ProductCard(
    product: OnBehalfProduct,
    cartQty: Int,
    onAdd: (Int) -> Unit
) {
    var qty by remember(cartQty) {
        mutableIntStateOf(if (cartQty > 0) cartQty else product.minQty().coerceAtLeast(1))
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
    ) {
        // ── Card body ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Product image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(KarikaColors.Gray20)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
            ) {
                KarikaImage(
                    model = product.imageUrl,
                    modifier = Modifier.fillMaxSize()
                )
                if (cartQty > 0) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .align(Alignment.TopEnd)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(KarikaColors.Green1),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_check_circle_filled),
                            contentDescription = "",
                            tint = KarikaColors.White,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }

            // Info
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                KarikaText(
                    text = product.name,
                    color = KarikaColors.Gray2,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
                if (product.sku.isNotBlank()) {
                    KarikaText(
                        text = "#${product.sku}",
                        color = KarikaColors.Gray6,
                        textSize = 11.sp,
                        fontWeight = FontWeight.W600
                    )
                }
                KarikaText(
                    text = product.categoryLabel ?: "",
                    color = KarikaColors.Gray6,
                    textSize = 11.sp,
                    fontWeight = FontWeight.W600
                )
                Spacer(Modifier.height(6.dp))
                KarikaText(
                    text = product.priceString(),
                    color = KarikaColors.Blue,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }

        // ── Card footer: stepper + button ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.Gray20)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Qty stepper
            Row(
                modifier = Modifier
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
                    .padding(horizontal = 2.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { if (qty > 1) qty-- },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "−",
                        color = KarikaColors.Gray2,
                        textSize = 18.sp,
                        fontWeight = FontWeight.W700
                    )
                }

                BasicTextField(
                    value = qty.toString(),
                    onValueChange = { v ->
                        val n = v.filter { it.isDigit() }.toIntOrNull()
                        if (n != null && n > 0) qty = n
                    },
                    modifier = Modifier.width(42.dp),
                    textStyle = TextStyle(
                        fontFamily = karikaFonts(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.W700,
                        color = KarikaColors.Gray2,
                        textAlign = TextAlign.Center
                    ),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { qty++ },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_add_plus),
                        contentDescription = "+",
                        tint = KarikaColors.Gray2,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Dodaj / Ažuriraj button
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaColors.Blue)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onAdd(qty) },
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_cart_add),
                        contentDescription = "",
                        tint = KarikaColors.White,
                        modifier = Modifier.size(16.dp)
                    )
                    KarikaText(
                        text = if (cartQty > 0) "Ažuriraj" else "Dodaj",
                        color = KarikaColors.White,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
    }
}

// ── Category bottom sheet helpers ──────────────────────────────────────────────

private data class FlatCategory(val category: Category, val ancestors: List<Category>)

private fun flattenToDepth(
    categories: List<Category>,
    parentAncestors: List<Category>,
    remaining: Int
): List<FlatCategory> {
    if (remaining <= 0) return emptyList()
    return categories.flatMap { cat ->
        listOf(FlatCategory(cat, parentAncestors)) +
                if (cat.childrenData.isNotEmpty()) {
                    flattenToDepth(cat.childrenData, parentAncestors + cat, remaining - 1)
                } else emptyList()
    }
}

// ── Category bottom sheet content ──────────────────────────────────────────────

@Composable
private fun CategorySheet(
    allCategories: List<Category>,
    selectedCategory: Category?,
    initialNavStack: List<Category> = emptyList(),
    onSelect: (Category?) -> Unit,
    onDismiss: () -> Unit
) {
    var categoryNavStack by remember { mutableStateOf(initialNavStack) }
    var categorySearch by remember { mutableStateOf("") }

    // Reset search when navigating to a different level
    LaunchedEffect(categoryNavStack) { categorySearch = "" }

    val isSearching = categorySearch.isNotBlank()

    val currentCategories = if (categoryNavStack.isEmpty()) allCategories
    else categoryNavStack.last().childrenData

    // Flat list across all 3 levels for deep search
    val allFlatCategories = remember(allCategories) {
        flattenToDepth(allCategories, emptyList(), 3)
    }

    val flatSearchResults = if (!isSearching) emptyList() else
        allFlatCategories.filter { it.category.name.contains(categorySearch, ignoreCase = true) }

    val currentTitle = if (categoryNavStack.isEmpty()) "Katalog"
    else categoryNavStack.last().name

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.9f)
            .navigationBarsPadding()
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categoryNavStack.isNotEmpty() && !isSearching) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { categoryNavStack = categoryNavStack.dropLast(1) },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_arrow_back),
                        contentDescription = "Nazad",
                        tint = KarikaColors.Blue,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(10.dp))
            }

            KarikaText(
                text = if (isSearching) "Pretraga kategorija" else currentTitle,
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700,
                modifier = Modifier.weight(1f)
            )

            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_cancel),
                    contentDescription = "Zatvori",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Search bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 10.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(KarikaColors.Gray20)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_search),
                contentDescription = "",
                tint = KarikaColors.Gray6,
                modifier = Modifier.size(18.dp)
            )
            Box(modifier = Modifier.weight(1f)) {
                if (categorySearch.isEmpty()) {
                    KarikaText(
                        text = "Pretraži kategorije...",
                        color = KarikaColors.Gray7,
                        textSize = 14.sp
                    )
                }
                BasicTextField(
                    value = categorySearch,
                    onValueChange = { categorySearch = it },
                    textStyle = TextStyle(
                        fontFamily = karikaFonts(),
                        fontSize = 14.sp,
                        color = KarikaColors.Gray2
                    ),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            if (categorySearch.isNotEmpty()) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_cancel),
                    contentDescription = "Obriši",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { categorySearch = "" }
                )
            }
        }

        // Category list
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isSearching) {
                // ── Deep search results (flat, 3 levels) ──────────────────────
                if (flatSearchResults.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            KarikaText(
                                text = "Nema rezultata za \"$categorySearch\"",
                                color = KarikaColors.Gray6,
                                textSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(flatSearchResults, key = { it.category.id }) { flat ->
                        val isSelected = selectedCategory?.id == flat.category.id
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(
                                    if (isSelected) KarikaColors.Blue.copy(alpha = 0.08f)
                                    else KarikaColors.White
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) KarikaColors.Blue.copy(alpha = 0.3f)
                                    else KarikaColors.Gray9,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) { onSelect(flat.category) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            KarikaText(
                                text = flat.category.name,
                                color = if (isSelected) KarikaColors.Blue else KarikaColors.Gray2,
                                textSize = 14.sp,
                                fontWeight = FontWeight.W600
                            )
                            if (flat.ancestors.isNotEmpty()) {
                                KarikaText(
                                    text = flat.ancestors.joinToString(" › ") { it.name },
                                    color = KarikaColors.Gray7,
                                    textSize = 11.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                        }
                    }
                }
            } else {
                // ── Normal hierarchical navigation ────────────────────────────
                item {
                    val parentCat = categoryNavStack.lastOrNull()
                    val isParentSelected =
                        if (parentCat != null) selectedCategory?.id == parentCat.id
                        else selectedCategory == null
                    val label =
                        if (parentCat != null) "Svi artikli: ${parentCat.name}" else "Sve kategorije"

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isParentSelected) KarikaColors.Blue.copy(alpha = 0.08f)
                                else KarikaColors.Gray20
                            )
                            .border(
                                width = 1.dp,
                                color = if (isParentSelected) KarikaColors.Blue.copy(alpha = 0.3f)
                                else KarikaColors.Gray9,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onSelect(parentCat) }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            text = label,
                            color = if (isParentSelected) KarikaColors.Blue else KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W600
                        )
                    }
                }

                items(currentCategories, key = { it.id }) { category ->
                    val hasChildren = category.childrenData.isNotEmpty()
                    val isSelected = selectedCategory?.id == category.id

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(
                                if (isSelected) KarikaColors.Blue.copy(alpha = 0.08f)
                                else KarikaColors.White
                            )
                            .border(
                                width = 1.dp,
                                color = if (isSelected) KarikaColors.Blue.copy(alpha = 0.3f)
                                else KarikaColors.Gray9,
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) {
                                if (hasChildren) categoryNavStack = categoryNavStack + category
                                else onSelect(category)
                            }
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KarikaText(
                            text = category.name,
                            color = if (isSelected) KarikaColors.Blue else KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W600,
                            modifier = Modifier.weight(1f)
                        )
                        if (hasChildren) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_arrow_right),
                                contentDescription = "",
                                tint = KarikaColors.Gray6,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

package karika.distribucija.ba.ui.view.salesrep.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.OnBehalfProduct
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KChipRow
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KSquareIconButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_cart_add
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_left
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_filter
import karikav2.composeapp.generated.resources.ic_k_inbox
import karikav2.composeapp.generated.resources.ic_k_minus
import karikav2.composeapp.generated.resources.ic_k_plus
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
    val cart by component.cart.collectAsState()
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

    var quickViewProduct by remember { mutableStateOf<OnBehalfProduct?>(null) }
    val quickViewSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var fabOffsetY by remember { mutableFloatStateOf(0f) }

    Box(modifier = Modifier.fillMaxSize().background(KarikaUiColors.Page)) {

        Column(modifier = Modifier.fillMaxSize()) {

            // ── Search + category button ───────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                    KSearchField(
                        cursorColor = VendorAccent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, KarikaUiColors.Border, RoundedCornerShape(12.dp)),
                        value = searchText,
                        onValueChange = component::setSearch,
                        placeholder = "Pretraži artikle..."
                    )
                    if (searchText.isNotEmpty()) {
                        ClearButton(contentDescription = "Obriši") { component.setSearch("") }
                    }
                }

                // Category filter button (only applies to the full catalog)
                if (selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) {
                    KSquareIconButton(
                        modifier = Modifier.semantics { contentDescription = "Kategorije" },
                        icon = vectorResource(Res.drawable.ic_k_filter),
                        background = if (selectedCategory != null) VendorAccent else KarikaUiColors.Ink
                    ) {
                        categorySheetInitialStack = emptyList()
                        showCategorySheet = true
                    }
                }
            }

            // ── Tabs ────────────────────────────────────────────────────────────
            KChipRow(modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) {
                KChip(
                    text = "Svi artikli",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS,
                    selectedColor = VendorAccent
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) }
                KChip(
                    text = "Na akciji",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.ON_SALE,
                    selectedColor = VendorAccent
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.ON_SALE) }
                KChip(
                    text = "Prethodno naručeno",
                    selected = selectedTab == SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED,
                    selectedColor = VendorAccent
                ) { component.selectTab(SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED) }
            }

            // ── Active category chip ───────────────────────────────────────────
            if (selectedCategory != null && selectedTab == SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(VendorAccentSoft)
                            .padding(start = 14.dp, end = 5.dp, top = 5.dp, bottom = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
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
                                        .clickable {
                                            categorySheetInitialStack =
                                                selectedCategoryPath.subList(0, index + 1)
                                            showCategorySheet = true
                                        },
                                    text = pathCategory.name,
                                    color = VendorAccent,
                                    textSize = 13.sp,
                                    fontWeight = if (isLastLevel) FontWeight.W700 else FontWeight.W500
                                )
                                if (!isLastLevel) {
                                    KarikaText(
                                        text = "›",
                                        color = VendorAccent,
                                        textSize = 13.sp,
                                        fontWeight = FontWeight.W600
                                    )
                                }
                            }
                        }
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(VendorAccent)
                                .clickable { component.selectCategory(null) },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_k_close),
                                contentDescription = "Ukloni",
                                tint = KarikaColors.White,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }

            // ── Product list ───────────────────────────────────────────────────
            when {
                isLoading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = VendorAccent)
                    }
                }

                products.isEmpty() -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        KEmptyPlaceholder(
                            icon = vectorResource(Res.drawable.ic_k_inbox),
                            title = "Nema artikala",
                            message = when (selectedTab) {
                                SalesOrderCatalogComponent.CatalogTab.ON_SALE -> "Trenutno nema artikala na akciji"
                                SalesOrderCatalogComponent.CatalogTab.PREVIOUSLY_ORDERED -> "Ovaj kupac nema prethodno naručenih artikala"
                                SalesOrderCatalogComponent.CatalogTab.ALL_ITEMS -> "Pokušajte s drugom pretragom ili kategorijom."
                            },
                            iconTint = VendorAccent,
                            iconBackground = VendorAccentSoft
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(products, key = { it.key }) { product ->
                            ProductCard(
                                product = product,
                                cartQty = cart?.items?.find { it.sku == product.sku }?.qty ?: 0,
                                onAdd = { qty -> component.changeQty(product, qty) },
                                onQuickView = { quickViewProduct = product }
                            )
                        }
                        if (isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        color = VendorAccent,
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

        // ── Floating draggable cart button ─────────────────────────────────────
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
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(VendorAccent)
                    .clickable { component.openCart() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_k_cart),
                    contentDescription = "Korpa",
                    tint = KarikaColors.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            if (cartCount > 0) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .align(Alignment.TopEnd)
                        .offset(x = 2.dp, y = (-2).dp)
                        .clip(CircleShape)
                        .background(KarikaUiColors.Ink)
                        .border(2.dp, KarikaColors.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "$cartCount",
                        color = KarikaColors.White,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
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
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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

    // ── Product quick view bottom sheet ─────────────────────────────────────────
    quickViewProduct?.let { product ->
        ModalBottomSheet(
            onDismissRequest = { quickViewProduct = null },
            sheetState = quickViewSheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            ProductQuickViewSheet(
                product = product,
                onDismiss = {
                    coroutineScope.launch { quickViewSheetState.hide() }.invokeOnCompletion {
                        quickViewProduct = null
                    }
                }
            )
        }
    }
}

/** Small round "x" inside a search field. */
@Composable
private fun ClearButton(contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(end = 6.dp)
            .size(34.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = vectorResource(Res.drawable.ic_k_close),
            contentDescription = contentDescription,
            tint = KarikaUiColors.Muted,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Product quick view sheet content ────────────────────────────────────────────

@Composable
private fun ProductQuickViewSheet(product: OnBehalfProduct, onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Brzi pregled artikla",
                color = KarikaUiColors.Ink,
                textSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W700
            )
            KCircleButton(
                modifier = Modifier.semantics { contentDescription = "Zatvori" },
                icon = vectorResource(Res.drawable.ic_k_close),
                size = 36.dp,
                iconSize = 18.dp,
                onClick = onDismiss
            )
        }

        Spacer(Modifier.height(16.dp))

        KImage(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp)),
            url = product.imageUrl
        )

        Spacer(Modifier.height(14.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            StockPill(product.isInStock)
            Spacer(Modifier.width(8.dp))
            KarikaText(
                text = "Dostupno: ${product.salableQty?.toInt() ?: 0}",
                color = KarikaUiColors.Muted,
                textSize = 13.sp,
                fontWeight = FontWeight.W600
            )
        }

        Spacer(Modifier.height(10.dp))

        KarikaText(
            text = product.name,
            color = KarikaUiColors.Ink,
            textSize = 20.sp,
            lineHeight = 25.sp,
            fontWeight = FontWeight.W700
        )

        if (!product.categoryLabel.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            KarikaText(
                text = "Kategorija: ${product.categoryLabel}",
                color = KarikaUiColors.Muted,
                textSize = 13.sp,
                lineHeight = 18.sp
            )
        }

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            QuickViewTile(modifier = Modifier.weight(1f), label = "SKU", value = product.sku)
            QuickViewTile(modifier = Modifier.weight(1f), label = "Cijena", value = product.priceString(), accent = true)
        }

        Spacer(Modifier.height(16.dp))
        KDivider()
        Spacer(Modifier.height(16.dp))

        KarikaText(
            text = "Opis",
            color = KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = FontWeight.W700
        )
        Spacer(Modifier.height(6.dp))
        KarikaText(
            text = product.description?.takeIf { it.isNotBlank() } ?: "Opis nije dostupan.",
            color = KarikaUiColors.Muted,
            textSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun QuickViewTile(modifier: Modifier, label: String, value: String, accent: Boolean = false) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaUiColors.Field)
            .padding(12.dp)
    ) {
        KarikaText(
            text = label,
            color = KarikaUiColors.Muted,
            textSize = 12.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(4.dp))
        KarikaText(
            text = value,
            color = if (accent) VendorAccent else KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = FontWeight.W700,
            maxLines = 2
        )
    }
}

@Composable
private fun StockPill(inStock: Boolean) {
    if (inStock) {
        KPill(text = "Na zalihi", dot = KarikaUiColors.GreenDot, textSize = 11.sp)
    } else {
        KPill(
            text = "Nije na stanju",
            background = KarikaUiColors.RedSoft,
            color = KarikaUiColors.Red,
            textSize = 11.sp
        )
    }
}

// ── Product card ───────────────────────────────────────────────────────────────

/**
 * Product as a white row card (as the customer's product rows): photo, name, SKU, category and
 * price, then the blue stepper and the "Dodaj"/"Ažuriraj" button.
 */
@Composable
private fun ProductCard(
    product: OnBehalfProduct,
    cartQty: Int,
    onAdd: (Int) -> Unit,
    onQuickView: () -> Unit
) {
    var qty by remember(cartQty) {
        mutableIntStateOf(if (cartQty > 0) cartQty else product.minQty().coerceAtLeast(1))
    }
    val inStock = product.isInStock

    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        // ── Card body ──────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onQuickView() }
                .padding(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                KImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (inStock) 1f else 0.45f),
                    url = product.imageUrl
                )
                if (cartQty > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(VendorAccent),
                        contentAlignment = Alignment.Center
                    ) {
                        KIcon(
                            icon = vectorResource(Res.drawable.ic_k_check),
                            tint = KarikaColors.White,
                            size = 14.dp
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!product.categoryLabel.isNullOrBlank()) {
                    KarikaText(
                        text = product.categoryLabel,
                        color = KarikaUiColors.Muted,
                        textSize = 11.5.sp,
                        lineHeight = 15.sp,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                }
                KarikaText(
                    text = product.name,
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 2
                )
                if (product.sku.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    KarikaText(
                        text = "#${product.sku}",
                        color = KarikaUiColors.Subtle,
                        textSize = 11.5.sp,
                        lineHeight = 15.sp,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = product.priceString(),
                        color = KarikaUiColors.Ink,
                        textSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    if (!inStock) {
                        StockPill(false)
                    }
                }
            }
        }

        KDivider()

        // ── Card footer: stepper + button ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            BlueQtyStepper(
                qty = qty,
                enabled = inStock,
                onMinus = { if (qty > 1) qty-- },
                onPlus = { qty++ },
                onTyped = { qty = it }
            )

            // Dodaj / Ažuriraj button
            Row(
                modifier = Modifier
                    .weight(1f)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (inStock) VendorAccent else KarikaUiColors.Field)
                    .then(if (inStock) Modifier.clickable { onAdd(qty) } else Modifier),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_cart_add),
                    tint = if (inStock) KarikaColors.White else KarikaUiColors.Subtle,
                    size = 17.dp
                )
                Spacer(Modifier.width(6.dp))
                KarikaText(
                    text = if (cartQty > 0) "Ažuriraj" else "Dodaj",
                    color = if (inStock) KarikaColors.White else KarikaUiColors.Subtle,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
        }
    }
}

/** Blue "– n +" stepper with a typed quantity, in the sales rep's accent. */
@Composable
private fun BlueQtyStepper(
    qty: Int,
    enabled: Boolean,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    onTyped: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(if (enabled) VendorAccentSoft else KarikaUiColors.Field)
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(KarikaColors.White)
                .then(if (enabled) Modifier.clickable(onClick = onMinus) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_minus),
                tint = if (enabled) VendorAccent else KarikaUiColors.Subtle,
                size = 16.dp
            )
        }

        BasicTextField(
            value = qty.toString(),
            onValueChange = { v ->
                val n = v.filter { it.isDigit() }.toIntOrNull()
                if (n != null && n > 0) onTyped(n)
            },
            enabled = enabled,
            modifier = Modifier.width(42.dp),
            textStyle = TextStyle(
                fontFamily = karikaFonts(),
                fontSize = 15.sp,
                fontWeight = FontWeight.W600,
                color = if (enabled) KarikaUiColors.Ink else KarikaUiColors.Subtle,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            ),
            cursorBrush = SolidColor(VendorAccent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(if (enabled) VendorAccent else KarikaUiColors.Border)
                .then(if (enabled) Modifier.clickable(onClick = onPlus) else Modifier),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_k_plus),
                contentDescription = "+",
                tint = KarikaColors.White,
                modifier = Modifier.size(16.dp)
            )
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
                .padding(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (categoryNavStack.isNotEmpty() && !isSearching) {
                KCircleButton(
                    modifier = Modifier.semantics { contentDescription = "Nazad" },
                    icon = vectorResource(Res.drawable.ic_k_chevron_left),
                    size = 36.dp,
                    iconSize = 18.dp
                ) { categoryNavStack = categoryNavStack.dropLast(1) }
                Spacer(Modifier.width(12.dp))
            }

            KarikaText(
                text = if (isSearching) "Pretraga kategorija" else currentTitle,
                color = KarikaUiColors.Ink,
                textSize = 18.sp,
                lineHeight = 22.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )

            KCircleButton(
                modifier = Modifier.semantics { contentDescription = "Zatvori" },
                icon = vectorResource(Res.drawable.ic_k_close),
                size = 36.dp,
                iconSize = 18.dp
            ) { onDismiss() }
        }

        // Search bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            KSearchField(
                cursorColor = VendorAccent,
                modifier = Modifier.fillMaxWidth(),
                value = categorySearch,
                onValueChange = { categorySearch = it },
                placeholder = "Pretraži kategorije..."
            )
            if (categorySearch.isNotEmpty()) {
                ClearButton(contentDescription = "Obriši") { categorySearch = "" }
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
                                color = KarikaUiColors.Muted,
                                textSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(flatSearchResults, key = { it.category.id }) { flat ->
                        CategoryRow(
                            name = flat.category.name,
                            subtitle = flat.ancestors.takeIf { it.isNotEmpty() }?.joinToString(" › ") { it.name },
                            selected = selectedCategory?.id == flat.category.id,
                            hasChildren = false
                        ) { onSelect(flat.category) }
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

                    CategoryRow(
                        name = label,
                        selected = isParentSelected,
                        hasChildren = false,
                        background = KarikaUiColors.Field
                    ) { onSelect(parentCat) }
                }

                items(currentCategories, key = { it.id }) { category ->
                    val hasChildren = category.childrenData.isNotEmpty()
                    CategoryRow(
                        name = category.name,
                        selected = selectedCategory?.id == category.id,
                        hasChildren = hasChildren
                    ) {
                        if (hasChildren) categoryNavStack = categoryNavStack + category
                        else onSelect(category)
                    }
                }
            }
        }
    }
}

/** Category option: blue tint, border and check when selected, chevron when it has children. */
@Composable
private fun CategoryRow(
    name: String,
    selected: Boolean,
    hasChildren: Boolean,
    subtitle: String? = null,
    background: androidx.compose.ui.graphics.Color = KarikaColors.White,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(if (selected) VendorAccentSoft else background)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) VendorAccent else KarikaUiColors.Line,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = name,
                color = if (selected) VendorAccent else KarikaUiColors.Ink,
                textSize = 14.5.sp,
                lineHeight = 19.sp,
                fontWeight = if (selected) FontWeight.W700 else FontWeight.W600
            )
            if (subtitle != null) {
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = subtitle,
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    lineHeight = 16.sp
                )
            }
        }
        if (hasChildren) {
            Spacer(Modifier.width(8.dp))
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_chevron_right),
                tint = KarikaUiColors.Subtle,
                size = 18.dp
            )
        } else if (selected) {
            Spacer(Modifier.width(8.dp))
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_check),
                tint = VendorAccent,
                size = 18.dp
            )
        }
    }
}

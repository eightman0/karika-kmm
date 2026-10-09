package karika.distribucija.ba.ui.view.shop.vendor.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.KAddButton
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KChipRow
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.shop.home.addToCartTag
import karika.distribucija.ba.ui.view.shop.home.productCardTag
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_pin
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VendorDetailsView(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()

    key(vendor.hashCode()) {
        KarikaScaffold(
            containerColor = KarikaUiColors.Page,
            contentWindowInsets = WindowInsets.systemBars,
            component = component,
            topBar = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    // White behind the status bar when shown at the app root (no-op inside the main shell)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(KarikaColors.White)
                            .windowInsetsPadding(WindowInsets.statusBars)
                    )
                    KBackHeader(
                        title = vendor.name(),
                        onBack = {
                            if (component.fromMain) {
                                component.mainBack()
                            } else {
                                component.appBack()
                            }
                        }
                    )
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(it)
            ) {
                VendorProducts(modifier = Modifier.weight(1f), component)
            }
        }
    }
}

@Composable
private fun VendorProducts(modifier: Modifier, component: VendorDetailsComponent) {
    val products = component.products.collectAsState()
    val state = rememberLazyListState()
    val searchText = component.searchText.asState()
    val gridColumnCount = gridColumnCount()
    Box(contentAlignment = Alignment.Center) {
        KarikaLazyColumn(
            modifier = modifier
                .fillMaxWidth()
                .hideKeyboard(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            state = state
        ) {
            item {
                VendorCard(component)
            }
            item {
                VendorCategories(component)
            }
            item {
                KarikaSearchInput(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    placeholder = "Pretraži proizvode…",
                    onValueChange = { text ->
                        searchText.value = text
                    },
                    onClear = {
                        searchText.value = ""
                        component.loadNextPage(true)
                    },
                    onSearch = {
                        if (searchText.value.length > 2) {
                            component.loadNextPage(true)
                        }
                    }
                )
            }
            items(items = products.value.chunked(gridColumnCount)) { item ->
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            KarikaProductCard(it, component, hideVendor = true)
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
            item { EmptyState(component) }
            item { Spacer(Modifier.height(8.dp)) }
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }
}

@Composable
private fun VendorCategories(component: VendorDetailsComponent) {
    val categories by component.vendorCategories.collectAsState()
    val isInStock = component.isInStock.asState()

    KChipRow {
        KChip(
            text = "Sve",
            selected = component.selectedCategories.value.isEmpty()
        ) {
            if (component.selectedCategories.value.isNotEmpty()) {
                component.selectedCategories.value = emptyList()
                component.loadNextPage(true)
            }
        }
        KChip(
            text = "Prikaži rasprodate",
            selected = isInStock.value == "1"
        ) {
            if (isInStock.value == "1") {
                isInStock.value = ""
            } else {
                isInStock.value = "1"
            }
            component.loadNextPage(true)
        }
        categories.forEach {
            KChip(
                text = it.name,
                selected = component.selectedCategories.value.contains(it)
            ) {
                if (component.selectedCategories.value.contains(it)) {
                    component.selectedCategories.value -= it
                } else {
                    component.selectedCategories.value += it
                }
                component.loadNextPage(true)
            }
        }
    }
}

@Composable
private fun VendorCard(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()
    val categories by component.vendorCategories.collectAsState()
    val logoShape = RoundedCornerShape(16.dp)

    KCard(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(144.dp)
        ) {
            KImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                url = vendor.companyBanner?.takeIf { it.isNotBlank() }?.let { imageUrl(it) }
            )
            Box(
                modifier = Modifier
                    .padding(start = 14.dp, top = 80.dp)
                    .size(64.dp)
                    .clip(logoShape)
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaUiColors.Line, logoShape)
                    .onClick {
                        component.showImagePreview(vendor.image())
                    }
                    .padding(3.dp),
                contentAlignment = Alignment.Center
            ) {
                if (vendor.companyLogo.isNullOrBlank()) {
                    KInitials(
                        name = vendor.name(),
                        size = 58.dp,
                        shape = RoundedCornerShape(13.dp),
                        textSize = 18.sp
                    )
                } else {
                    KImage(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(13.dp)),
                        url = vendor.image(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = 10.dp, bottom = 14.dp)
        ) {
            KarikaText(
                text = vendor.name(),
                color = KarikaUiColors.Ink,
                textSize = 18.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2
            )
            val subtitle = categories.take(2).joinToString(" · ") { it.name }
            if (subtitle.isNotBlank()) {
                Spacer(Modifier.height(3.dp))
                KarikaText(
                    text = subtitle,
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    lineHeight = 17.sp,
                    maxLines = 1
                )
            }
            if (!vendor.address.isNullOrBlank()) {
                Spacer(Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_pin), tint = KarikaUiColors.Muted, size = 13.dp)
                    Spacer(Modifier.width(4.dp))
                    KarikaText(
                        text = vendor.address,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 2
                    )
                }
            }
            Spacer(Modifier.height(14.dp))
            KTonalButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Pošalji poruku dobavljaču",
                icon = vectorResource(Res.drawable.ic_k_chat)
            ) {
                component.sendMessageToVendor(
                    Product(vendorId = vendor.entityId.toString(), vendorName = vendor.publicName)
                )
            }
        }
    }
}

@Composable
private fun EmptyState(component: VendorDetailsComponent) {
    val vendors by component.products.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && !loader) {
        KEmptyState(
            modifier = Modifier.height(200.dp),
            text = "Nema rezultata."
        )
    }
}

/**
 * Product card of the redesign (vendor page and category products): rounded photo with the
 * pink "+" in the corner, then the price and the name.
 */
@Composable
internal fun KarikaProductCard(
    product: Product,
    component: CommonComponent,
    hideVendor: Boolean = false,
    showMinQty: Boolean = false
) {
    val shape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .testTag(productCardTag(product))
                .onClick {
                    component.navigateToProduct(product)
                }
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(shape)
                .background(KarikaColors.White)
                .border(1.dp, KarikaUiColors.Line, shape)
        ) {
            KImage(
                modifier = Modifier.fillMaxSize(),
                url = product.image()
            )
            if (!product.hasOnStock()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(KarikaUiColors.Ink.copy(alpha = 0.35f)),
                    contentAlignment = Alignment.Center
                ) {
                    KPill(
                        text = "RASPRODANO",
                        background = KarikaUiColors.Pink,
                        color = KarikaColors.White
                    )
                }
            }
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (product.hasBonus()) {
                    KPill(
                        text = product.bonusString(),
                        background = KarikaUiColors.GreenSoft,
                        color = KarikaUiColors.Green,
                        icon = vectorResource(Res.drawable.ic_k_gift)
                    )
                }
                if (product.hasSpecialPrice()) {
                    KPill(
                        text = "-" + product.calculatePercent() + "%",
                        background = KarikaUiColors.Pink,
                        color = KarikaColors.White
                    )
                }
                if (product.isNew()) {
                    KPill(
                        text = "Novo",
                        background = KarikaUiColors.Ink,
                        color = KarikaColors.White
                    )
                }
            }
            if (product.hasOnStock()) {
                KAddButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(8.dp)
                        .testTag(addToCartTag(product))
                ) {
                    component.addToCart(product, product.minQty())
                }
            }
        }
        Spacer(Modifier.height(9.dp))
        KarikaText(
            text = product.currentPriceString(),
            color = KarikaUiColors.Ink,
            textSize = 17.sp,
            lineHeight = 21.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        Spacer(Modifier.height(3.dp))
        KarikaText(
            text = product.name(),
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            lineHeight = 18.sp,
            fontWeight = FontWeight.W500,
            maxLines = 3
        )
        if (showMinQty) {
            Spacer(Modifier.height(4.dp))
            KarikaText(
                text = "Min. kol: ${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1
            )
        }
        if (!hideVendor && !component.isGuest()) {
            Spacer(Modifier.height(4.dp))
            KarikaText(
                modifier = Modifier
                    .onClick {
                        component.showVendor(product.toVendor())
                    },
                text = product.vendorName(),
                color = KarikaUiColors.Pink,
                textSize = 12.sp,
                lineHeight = 16.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
        }
    }
}

/**
 * [KSearchField] with the behavior of the old search box: searches 500 ms after typing stops
 * and on the keyboard's search key, and has a clear button.
 */
@OptIn(FlowPreview::class)
@Composable
internal fun KarikaSearchInput(
    placeholder: String,
    modifier: Modifier = Modifier,
    initial: String = "",
    onValueChange: (String) -> Unit,
    onClear: () -> Unit,
    onSearch: (String) -> Unit,
) {
    var text by remember { mutableStateOf(initial) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val currentOnSearch by rememberUpdatedState(onSearch)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterEnd
    ) {
        KSearchField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = {
                text = it
                onValueChange(it)
                if (it.isEmpty()) {
                    onClear()
                }
            },
            placeholder = placeholder,
            onSearch = {
                onSearch(text)
                keyboardController?.hide()
            }
        )
        if (text.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .padding(end = 6.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.Field)
                    .onClick {
                        text = ""
                        onValueChange("")
                        onClear()
                        keyboardController?.hide()
                    },
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_tertiary),
                    tint = KarikaUiColors.Muted,
                    size = 20.dp
                )
            }
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { text }
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { newQuery ->
                if (newQuery.isNotEmpty()) {
                    currentOnSearch(newQuery)
                }
            }
    }
}

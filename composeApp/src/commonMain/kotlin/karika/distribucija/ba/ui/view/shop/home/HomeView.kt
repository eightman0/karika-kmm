package karika.distribucija.ba.ui.view.shop.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.KAddButton
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KChipRow
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KFeatureTile
import karika.distribucija.ba.ui.components.KHeader
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.vendorBannerTag
import karika.distribucija.ba.ui.components.vendorLogoTag
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.product.VendorName
import karika.distribucija.ba.util.KarikaConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_cart_add
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_menu
import karikav2.composeapp.generated.resources.ic_k_outlet
import karikav2.composeapp.generated.resources.ic_k_store
import karikav2.composeapp.generated.resources.ic_k_tag
import org.jetbrains.compose.resources.vectorResource

/** Test tag of the scrolling home page, for the end-to-end tests. */
const val HOME_LIST_TAG = "home_list"

/** Placeholder of the search entry on home, for the end-to-end tests. */
const val HOME_SEARCH_PLACEHOLDER = "Proizvodi, dobavljači…"

@Composable
fun HomeView(component: HomeComponent) {
    val scrollState = rememberScrollState()
    val promotedLogos by component.promotedLogos.collectAsState()
    val promotedVendors by component.promotedVendors.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .testTag(HOME_LIST_TAG)
            .verticalScroll(scrollState)
    ) {
        HomeHeader(component)
        Spacer(Modifier.height(16.dp))
        Shortcuts(component)
        if (promotedLogos.isNotEmpty()) {
            Spacer(Modifier.height(22.dp))
            Suppliers(component, promotedLogos)
        }
        if (promotedVendors.isNotEmpty()) {
            Spacer(Modifier.height(18.dp))
            FeaturedVendors(component, promotedVendors)
        }
        KarikaProducts(component)
        Spacer(Modifier.height(20.dp))
    }

    LaunchedEffect(Unit) {
        scrollState.scrollTo(0)
        component.loadData()
    }
}

/** "Dostava za" with the customer's company and city, the bell, search and the filter button. */
@Composable
private fun HomeHeader(component: HomeComponent) {
    val userDetails by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    val notificationCount by component.stateHolder.customerNotificationHandler.notificationCount.collectAsState()
    val guest = component.isGuest()
    val openSearch = { component.mainNavigate(MainConfig.Search) }

    KHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (guest) Modifier
                        else Modifier.clickable { component.appNavigate(AppConfig.Account) }
                    )
            ) {
                KarikaText(
                    text = if (guest) "Dobrodošli u" else "Dostava za",
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    fontWeight = FontWeight.W500,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KarikaText(
                        modifier = Modifier.weight(1f, fill = false),
                        text = if (guest) "Karika" else deliveryTitle(userDetails),
                        color = KarikaUiColors.Ink,
                        textSize = 16.sp,
                        lineHeight = 20.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                    if (!guest) {
                        Spacer(Modifier.width(4.dp))
                        KIcon(
                            icon = vectorResource(Res.drawable.ic_k_chevron_down),
                            tint = KarikaUiColors.Ink,
                            size = 14.dp
                        )
                    }
                }
            }
            Spacer(Modifier.width(12.dp))
            KCircleButton(
                icon = vectorResource(Res.drawable.ic_k_bell),
                showDot = notificationCount > 0,
                onClick = {
                    if (guest) {
                        component.stateHolder.commonHandler.showLoginRequired("")
                    } else {
                        component.appNavigate(AppConfig.Notifications)
                    }
                }
            )
        }
        Spacer(Modifier.height(14.dp))
        KSearchField(
            modifier = Modifier.fillMaxWidth(),
            value = "",
            onValueChange = {},
            placeholder = HOME_SEARCH_PLACEHOLDER,
            onClick = openSearch
        )
    }
}

/** Company and city of the delivery address, e.g. "Market Centar, Sarajevo". */
private fun deliveryTitle(userDetails: UserDetails): String {
    val address = userDetails.shippingAddress() ?: userDetails.addresses.firstOrNull()
    val name = userDetails.companyNameNullable()?.takeIf { it.isNotBlank() }
        ?: address?.street?.firstOrNull()?.takeIf { it.isNotBlank() }
    val city = address?.city?.takeIf { it.isNotBlank() }
    return listOfNotNull(name, city).joinToString(", ").ifEmpty { "Karika" }
}

/** Outlet and Akcije tiles, and for guests the vendors and all products shortcuts. */
@Composable
private fun Shortcuts(component: HomeComponent) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            KFeatureTile(
                modifier = Modifier.weight(1f),
                title = "Outlet",
                subtitle = "Sniženo sa zaliha",
                icon = vectorResource(Res.drawable.ic_k_outlet),
                background = KarikaUiColors.Pink,
                height = 96.dp,
                onClick = {
                    component.mainNavigate(
                        MainConfig.CategoryProducts(
                            Category(
                                id = KarikaConfig.getOutletId(),
                                name = "OUTLET"
                            )
                        )
                    )
                }
            )
            KFeatureTile(
                modifier = Modifier.weight(1f),
                title = "Akcije",
                subtitle = "Aktivne ponude",
                icon = vectorResource(Res.drawable.ic_k_tag),
                background = KarikaUiColors.Ink,
                height = 96.dp,
                onClick = {
                    component.mainNavigate(
                        MainConfig.CategoryProducts(
                            Category(
                                id = KarikaConfig.getActionId(),
                                name = "AKCIJE"
                            )
                        )
                    )
                }
            )
        }
        if (component.isGuest()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    text = "Svi dobavljači",
                    onClick = { component.mainNavigate(MainConfig.Vendor) },
                    iconIsStore = true
                )
                ShortcutCard(
                    modifier = Modifier.weight(1f),
                    text = "Svi proizvodi",
                    onClick = {
                        component.mainNavigate(
                            MainConfig.CategoryProducts(
                                Category(
                                    id = 10,
                                    name = "SVI PROIZVODI"
                                )
                            )
                        )
                    },
                    iconIsStore = false
                )
            }
        }
    }
}

@Composable
private fun ShortcutCard(
    modifier: Modifier,
    text: String,
    iconIsStore: Boolean,
    onClick: () -> Unit,
) {
    KCard(modifier = modifier, onClick = onClick) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaUiColors.PinkSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(if (iconIsStore) Res.drawable.ic_k_store else Res.drawable.ic_k_menu),
                    tint = KarikaUiColors.Pink,
                    size = 18.dp
                )
            }
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = text,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_chevron_right),
                tint = KarikaUiColors.Subtle,
                size = 16.dp
            )
        }
    }
}

/** "Dobavljači" with the promoted vendors' logos in round tiles. */
@Composable
private fun Suppliers(component: HomeComponent, logos: List<PromotedVendor>) {
    KSectionTitle(
        modifier = Modifier.padding(horizontal = 16.dp),
        title = "Dobavljači",
        actionText = "Svi",
        onAction = { component.mainNavigate(MainConfig.Vendor) }
    )
    Spacer(Modifier.height(12.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        logos.forEach { vendor ->
            Column(
                modifier = Modifier
                    .testTag(vendorLogoTag(vendor))
                    .width(62.dp)
                    .clickable { component.showVendor(vendor.toVendor()) },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .clip(CircleShape)
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaUiColors.Border, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (vendor.companyLogo.isNullOrBlank()) {
                        KInitials(
                            name = vendor.name(),
                            size = 58.dp,
                            shape = CircleShape,
                            background = KarikaColors.White,
                            color = KarikaUiColors.Ink,
                            textSize = 15.sp
                        )
                    } else {
                        KImage(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape),
                            url = vendor.logoImage(),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                Spacer(Modifier.height(6.dp))
                KarikaText(
                    modifier = Modifier.fillMaxWidth(),
                    text = vendor.name(),
                    color = KarikaUiColors.Ink,
                    textSize = 11.sp,
                    lineHeight = 14.sp,
                    fontWeight = FontWeight.W500,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

/** The promoted vendors' banners as "IZDVOJENO" cards with a "Posjeti" button. */
@Composable
private fun FeaturedVendors(component: HomeComponent, vendors: List<PromotedVendor>) {
    if (vendors.size == 1) {
        FeaturedVendorCard(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            vendor = vendors.first(),
            component = component
        )
    } else {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            vendors.forEach { vendor ->
                FeaturedVendorCard(
                    modifier = Modifier.width(300.dp),
                    vendor = vendor,
                    component = component
                )
            }
        }
    }
}

@Composable
private fun FeaturedVendorCard(modifier: Modifier, vendor: PromotedVendor, component: HomeComponent) {
    val subtitle = vendor.categories
        ?.map { it.name }
        ?.filter { it.isNotBlank() }
        ?.take(3)
        ?.joinToString(" · ")
        ?.takeIf { it.isNotBlank() }
        ?: vendor.description
    KCard(
        modifier = modifier.testTag(vendorBannerTag(vendor)),
        onClick = { component.showVendor(vendor.toVendor()) }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            KImage(
                modifier = Modifier.fillMaxSize(),
                url = vendor.bannerImage()
            )
            Box(
                modifier = Modifier
                    .padding(12.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KarikaColors.White)
                    .padding(horizontal = 9.dp, vertical = 4.dp)
            ) {
                KarikaText(
                    text = "IZDVOJENO",
                    color = KarikaUiColors.Pink,
                    textSize = 10.5.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
            }
        }
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = vendor.name(),
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    KarikaText(
                        text = subtitle,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            KTonalButton(
                text = "Posjeti",
                height = 34.dp,
                onClick = { component.showVendor(vendor.toVendor()) }
            )
        }
    }
}

/** Quick filters of the recommended products, shown only when some product matches. */
private enum class RecommendedFilter(val title: String, val matches: (Product) -> Boolean) {
    ALL("Sve", { true }),
    DISCOUNT("Na akciji", { it.hasSpecialPrice() }),
    NEW("Novo", { it.isNew() }),
    BONUS("Sa bonusom", { it.hasBonus() }),
    IN_STOCK("Na stanju", { it.hasOnStock() }),
}

@Composable
private fun KarikaProducts(component: HomeComponent) {
    val newArrivals by component.newArrivals.collectAsState()
    var filter by remember { mutableStateOf(RecommendedFilter.ALL) }

    if (newArrivals.isNotEmpty()) {
        val filters = RecommendedFilter.entries.filter { f ->
            f == RecommendedFilter.ALL || newArrivals.any { f.matches(it) }
        }
        val selected = if (filter in filters) filter else RecommendedFilter.ALL
        val products = newArrivals.filter { selected.matches(it) }
        val perRow = if (gridColumnCount() >= 4) 2 else 1

        Spacer(Modifier.height(24.dp))
        KSectionTitle(
            modifier = Modifier.padding(horizontal = 16.dp),
            title = "Karika preporučuje",
            actionText = "Vidi sve",
            onAction = {
                component.mainNavigate(
                    MainConfig.CategoryProducts(
                        Category(
                            id = KarikaConfig.getKarikaProductsId(),
                            name = "Karika preporučuje"
                        )
                    )
                )
            }
        )
        Spacer(Modifier.height(10.dp))
        if (filters.size > 1) {
            KChipRow {
                filters.forEach { f ->
                    KChip(
                        text = f.title,
                        selected = f == selected,
                        onClick = { filter = f }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            products.chunked(perRow).forEach { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach {
                        Box(modifier = Modifier.weight(1f)) {
                            ProductListRow(it, component)
                        }
                    }
                    repeat(perRow - row.size) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** Test tags of a product card and its add-to-cart button, for the end-to-end tests. */
fun productCardTag(product: Product) = "product_${product.sku}"
fun addToCartTag(product: Product) = "add_to_cart_${product.sku}"

/**
 * Product as a white row card: photo, vendor, name, price and the pink add button
 * (home's "Karika preporučuje" and search results).
 */
@Composable
fun ProductListRow(
    product: Product,
    component: CommonComponent,
    hideVendor: Boolean = false,
    showMinQty: Boolean = false
) {
    val inStock = product.hasOnStock()
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(productCardTag(product)),
        shape = RoundedCornerShape(14.dp),
        onClick = { component.navigateToProduct(product) }
    ) {
        Row(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(RoundedCornerShape(10.dp))
            ) {
                KImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(if (inStock) 1f else 0.45f),
                    url = product.image()
                )
                if (product.hasSpecialPrice()) {
                    KPill(
                        modifier = Modifier.padding(4.dp),
                        text = "-" + product.calculatePercent() + "%",
                        background = KarikaUiColors.Pink,
                        color = KarikaColors.White,
                        textSize = 10.sp
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                if (!hideVendor && !component.isGuest()) {
                    KarikaText(
                        modifier = Modifier.clickable { component.showVendor(product.toVendor()) },
                        text = product.vendorName(),
                        color = KarikaUiColors.Muted,
                        textSize = 11.5.sp,
                        lineHeight = 15.sp,
                        maxLines = 1
                    )
                    Spacer(Modifier.height(2.dp))
                }
                KarikaText(
                    text = product.name(),
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 2
                )
                ProductBadges(product)
                if (showMinQty) {
                    Spacer(Modifier.height(4.dp))
                    KarikaText(
                        text = "Min. kol: ${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        maxLines = 1
                    )
                }
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        if (product.hasSpecialPrice()) {
                            KarikaText(
                                text = product.originalPriceString(),
                                color = KarikaUiColors.Subtle,
                                textSize = 11.5.sp,
                                maxLines = 1,
                                decoration = TextDecoration.LineThrough
                            )
                        }
                        KarikaText(
                            text = product.currentPriceString(),
                            color = KarikaUiColors.Ink,
                            textSize = 16.sp,
                            lineHeight = 20.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                    }
                    if (inStock) {
                        KAddButton(
                            modifier = Modifier.testTag(addToCartTag(product)),
                            size = 36.dp,
                            onClick = { component.addToCart(product, product.minQty()) }
                        )
                    } else {
                        KPill(
                            text = "RASPRODANO",
                            background = KarikaUiColors.RedSoft,
                            color = KarikaUiColors.Red,
                            textSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

/** "Novo" and bonus labels under a product's name. */
@Composable
private fun ProductBadges(product: Product) {
    if (!product.isNew() && !product.hasBonus()) {
        return
    }
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        if (product.isNew()) {
            KPill(text = "Novo", textSize = 11.sp)
        }
        if (product.hasBonus()) {
            KPill(
                text = product.bonusString(),
                icon = vectorResource(Res.drawable.ic_k_gift),
                textSize = 11.sp
            )
        }
    }
}

/** Product as a grid card (categories, vendor page, product details). */
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
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .testTag(productCardTag(product))
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(14.dp))
                .background(KarikaUiColors.Field)
                .border(1.dp, KarikaUiColors.Line, RoundedCornerShape(14.dp))
                .clickable { component.navigateToProduct(product) },
        ) {
            KImage(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (product.hasOnStock()) 1f else 0.45f),
                url = product.image()
            )
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
                color = KarikaUiColors.Muted,
                text = "Min. kol: ${product.minQty()} ${component.getUnit(product.minQtyUnit())}",
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
        }
        KarikaText(
            color = KarikaUiColors.Ink,
            text = product.currentPriceString(),
            textSize = 16.sp,
            lineHeight = 20.sp,
            fontWeight = FontWeight.W700
        )
        KarikaText(
            color = KarikaUiColors.Ink,
            text = product.name(),
            textSize = 14.sp,
            lineHeight = 18.sp,
            maxLines = 3,
            fontWeight = FontWeight.W600
        )
        if (!hideVendor && !component.isGuest()) {
            VendorName(product, component)
        }
    }
}

@Composable
fun BonusView(product: Product) {
    if (!product.hasBonus()) {
        return
    }
    KPill(
        modifier = Modifier.padding(8.dp),
        text = product.bonusString(),
        icon = vectorResource(Res.drawable.ic_k_gift),
        background = KarikaUiColors.GreenSoft,
        color = KarikaUiColors.Green
    )
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
                    .padding(8.dp)
                    .testTag(addToCartTag(product))
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.Pink)
                    .clickable { component.addToCart(product, product.minQty()) },
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_cart_add),
                    tint = KarikaColors.White,
                    size = 20.dp
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
            KPill(
                text = "RASPRODANO",
                background = KarikaUiColors.Ink,
                color = KarikaColors.White
            )
        }
    }
}

@Composable
fun DiscountView(product: Product) {
    if (!product.hasSpecialPrice()) {
        return
    }
    KPill(
        modifier = Modifier.padding(8.dp),
        text = "-" + product.calculatePercent() + "%",
        background = KarikaUiColors.Pink,
        color = KarikaColors.White
    )
}

@Composable
fun NewView(product: Product) {
    if (!product.isNew()) {
        return
    }
    KPill(
        modifier = Modifier.padding(8.dp),
        text = "Novo",
        background = KarikaUiColors.Green,
        color = KarikaColors.White
    )
}

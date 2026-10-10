package karika.distribucija.ba.ui.view.shop.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KSquareIconButton
import karika.distribucija.ba.ui.components.KTitleHeader
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.shop.MainConfig
import karika.distribucija.ba.ui.view.shop.vendor.details.KarikaSearchInput
import karika.distribucija.ba.ui.view.shop.vendor.details.filter.FilterSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_filter
import karikav2.composeapp.generated.resources.ic_k_pin
import karikav2.composeapp.generated.resources.ic_k_star
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

/** Test tags of the vendors tab, for the end-to-end tests. */
const val VENDOR_FILTER_TAG = "vendor_filter"
fun vendorCardTag(vendor: Vendor) = "vendor_${vendor.entityId}"
fun featuredVendorTag(vendor: PromotedVendor) = "featured_vendor_${vendor.entityId}"

@Composable
fun VendorView(viewModel: VendorComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Vendors(viewModel)
    }
}

@Composable
private fun Vendors(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()
    val state = rememberLazyListState()
    val gridColumnCount = gridColumnCount()

    KarikaLazyColumn(
        modifier = Modifier
            .hideKeyboard()
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        state = state
    ) {
        item {
            Header(component)
        }
        item {
            ActiveFilter(component)
        }
        item {
            FeaturedVendors(component)
        }
        item {
            AllVendorsTitle(component)
        }
        item {
            EmptyState(component)
        }
        items(
            items = vendors.chunked(gridColumnCount)
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                it.forEach { vendor ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        VendorItem(vendor, component)
                    }
                }
                repeat(gridColumnCount - it.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                    )
                }
            }
        }
        item {
            Spacer(Modifier.height(10.dp))
        }
    }

    // Outside the list, so the sheet stays open while the list scrolls
    FilterSheet(component)

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    LaunchedEffect(Unit) {
        component.loadFeaturedVendors()
    }
}

@Composable
private fun Header(component: VendorComponent) {
    val showState = component.showFilter.asState()
    val searchText = component.searchText.asState()
    val notificationCount by component.stateHolder.customerNotificationHandler.notificationCount.collectAsState()

    KTitleHeader(
        title = "Dobavljači",
        trailing = {
            if (!component.isGuest()) {
                KCircleButton(
                    icon = vectorResource(Res.drawable.ic_k_bell),
                    showDot = notificationCount > 0
                ) {
                    component.appNavigate(AppConfig.Notifications)
                }
            }
        },
        below = {
            Spacer(Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaSearchInput(
                    modifier = Modifier
                        .weight(1f),
                    placeholder = "Pretraži dobavljače…",
                    initial = searchText.value,
                    onValueChange = {
                        searchText.value = it
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
                KSquareIconButton(
                    modifier = Modifier
                        .testTag(VENDOR_FILTER_TAG),
                    icon = vectorResource(Res.drawable.ic_k_filter)
                ) {
                    showState.negate()
                }
            }
        }
    )
}

@Composable
private fun ActiveFilter(component: VendorComponent) {
    val filter = component.selectedRegion.asState()

    if (filter.value.isNotEmpty()) {
        FlowRow(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 2.dp),
                color = KarikaUiColors.Muted,
                textSize = 13.sp,
                fontWeight = FontWeight.W600,
                text = "Uključeni filter: "
            )
            filter.value.forEach {
                KChip(
                    text = it.label(),
                    selected = true,
                    trailingIcon = vectorResource(Res.drawable.ic_tertiary)
                ) {
                    component.selectedRegion.value -= it
                    component.loadNextPage(reset = true)
                }
            }
        }
    }
}

@Composable
private fun AllVendorsTitle(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()

    Row(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 2.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            text = "Svi dobavljači",
            color = KarikaUiColors.Ink,
            textSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        if (vendors.isNotEmpty()) {
            KarikaText(
                text = if (component.hasNextPage) "${vendors.size}+" else "${vendors.size}",
                color = KarikaUiColors.Pink,
                textSize = 13.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
        }
    }
}

/** Vendor card of the 2-column grid: logo area, name and city. */
@Composable
fun VendorItem(vendor: Vendor, component: CommonComponent) {
    KCard(
        modifier = Modifier
            .testTag(vendorCardTag(vendor))
            .fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = {
            component.showVendor(vendor)
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .background(KarikaColors.White),
            contentAlignment = Alignment.Center
        ) {
            if (vendor.companyLogo.isNullOrBlank()) {
                KInitials(
                    name = vendor.name(),
                    size = 64.dp,
                    shape = RoundedCornerShape(16.dp),
                    textSize = 20.sp
                )
            } else {
                KImage(
                    modifier = Modifier
                        .fillMaxSize(),
                    url = vendor.image(),
                    contentScale = ContentScale.Crop
                )
            }
        }
        KDivider()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 12.dp)
        ) {
            KarikaText(
                text = vendor.name(),
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W600,
                maxLines = 2
            )
            val city = vendor.city?.takeIf { it.isNotBlank() } ?: vendor.b2bVendorGrad?.takeIf { it.isNotBlank() }
            if (city != null) {
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_pin), tint = KarikaUiColors.Muted, size = 12.dp)
                    Spacer(Modifier.width(4.dp))
                    KarikaText(
                        text = city,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedVendors(component: VendorComponent) {
    val featuredVendors by component.promotedVendors.collectAsState()

    if (featuredVendors.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    text = "Istaknuti",
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

            LazyRow(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(items = featuredVendors) { vendor ->
                    FeaturedVendorItem(
                        modifier = Modifier
                            .width(290.dp),
                        vendor = vendor,
                        component = component
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedVendorItem(
    modifier: Modifier,
    vendor: PromotedVendor,
    component: CommonComponent
) {
    KCard(
        modifier = modifier
            .testTag(featuredVendorTag(vendor)),
        shape = RoundedCornerShape(14.dp),
        onClick = {
            component.showVendor(vendor.toVendor())
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (vendor.companyLogo.isNullOrBlank()) {
                    KInitials(
                        name = vendor.name(),
                        size = 52.dp,
                        shape = RoundedCornerShape(14.dp),
                        textSize = 17.sp
                    )
                } else {
                    KImage(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(KarikaUiColors.PinkSoft),
                        url = vendor.logoImage()
                    )
                }
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
                text = vendor.name(),
                color = KarikaUiColors.Ink,
                textSize = 16.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2
            )
            if (!vendor.description.isNullOrBlank()) {
                Spacer(Modifier.height(4.dp))
                KarikaText(
                    text = vendor.description,
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    lineHeight = 16.sp,
                    maxLines = 2
                )
            }
            val categories = vendor.categories.orEmpty()
            if (categories.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    categories.forEach { tag ->
                        KChip(
                            text = tag.name,
                            selected = false
                        ) {
                            component.mainNavigate(
                                MainConfig.CategoryProducts(
                                    Category(
                                        id = tag.categoryId ?: 0,
                                        name = tag.name
                                    )
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && !loader) {
        KEmptyState(text = "Nema rezultata.")
    }
}

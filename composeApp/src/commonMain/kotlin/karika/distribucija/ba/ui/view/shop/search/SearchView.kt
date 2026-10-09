package karika.distribucija.ba.ui.view.shop.search

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.KBackButton
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KHeader
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.view.shop.home.ProductListRow
import karika.distribucija.ba.ui.view.shop.vendor.vendorCardTag
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SearchView(component: SearchComponent) {
    val products by component.products.collectAsState()
    val vendors by component.vendors.collectAsState()
    val state = rememberLazyListState()
    // Rows on phones, two per row on tablets
    val perRow = if (gridColumnCount() >= 4) 2 else 1
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            SearchHeader(component)
        },
        component = component
    ) {
        KarikaLazyColumn(
            state = state,
            modifier = Modifier
                .fillMaxSize()
                .hideKeyboard()
                .padding(it)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item { Spacer(Modifier.height(6.dp)) }
            if (vendors.isNotEmpty()) {
                item {
                    KSectionTitle(title = "Dobavljači")
                }
            }
            items(items = vendors.chunked(perRow)) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            VendorRow(it, component)
                        }
                    }
                    repeat(perRow - item.size) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }

            if (products.isNotEmpty()) {
                item {
                    KSectionTitle(
                        modifier = Modifier.padding(top = if (vendors.isNotEmpty()) 12.dp else 0.dp),
                        title = "Proizvodi"
                    )
                }
            }
            items(items = products.chunked(perRow)) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            ProductListRow(it, component)
                        }
                    }
                    repeat(perRow - item.size) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
            item { EmptyState(component) }
            item { Spacer(Modifier.height(10.dp)) }
        }

        LaunchedEffect(state.canScrollForward) {
            if (!state.canScrollForward) {
                component.search(false)
            }
        }
    }
}

/** White header with the back button and the search field, which searches as the customer types. */
@OptIn(FlowPreview::class)
@Composable
private fun SearchHeader(component: SearchComponent) {
    val focusRequester = remember { FocusRequester() }
    val searchText = component.searchText
    KHeader {
        Row(verticalAlignment = Alignment.CenterVertically) {
            KBackButton(onClick = { component.mainBack() })
            Spacer(Modifier.width(10.dp))
            KSearchField(
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                value = searchText.value,
                onValueChange = {
                    searchText.value = it
                    if (it.isEmpty()) {
                        component.search(true)
                    }
                },
                placeholder = "Pretraži proizvode i dobavljače",
                onSearch = {
                    if (searchText.value.length > 2) {
                        component.search(true)
                    }
                }
            )
        }
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(Unit) {
        snapshotFlow { searchText.value }
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { query ->
                if (query.length > 2) {
                    component.search(true)
                }
            }
    }
}

/** Vendor result as a white row card: logo, name, city and a chevron. */
@Composable
private fun VendorRow(vendor: Vendor, component: SearchComponent) {
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(vendorCardTag(vendor)),
        shape = RoundedCornerShape(14.dp),
        onClick = { component.showVendor(vendor) }
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (vendor.companyLogo.isNullOrBlank()) {
                KInitials(
                    name = vendor.name(),
                    size = 52.dp,
                    shape = RoundedCornerShape(12.dp),
                    textSize = 15.sp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaUiColors.Line, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    KImage(
                        modifier = Modifier.size(42.dp),
                        url = vendor.image(),
                        contentScale = ContentScale.Fit
                    )
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = vendor.name(),
                    color = KarikaUiColors.Ink,
                    textSize = 15.sp,
                    lineHeight = 19.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                val city = vendor.city?.takeIf { it.isNotBlank() } ?: vendor.b2bVendorGrad
                if (!city.isNullOrBlank()) {
                    Spacer(Modifier.height(2.dp))
                    KarikaText(
                        text = city,
                        color = KarikaUiColors.Muted,
                        textSize = 12.sp,
                        lineHeight = 16.sp,
                        maxLines = 1
                    )
                }
            }
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_chevron_right),
                tint = KarikaUiColors.Subtle,
                size = 18.dp
            )
        }
    }
}

@Composable
private fun EmptyState(component: SearchComponent) {
    val vendors by component.vendors.collectAsState()
    val products by component.products.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && products.isEmpty() && !loader) {
        KEmptyState(
            modifier = Modifier.height(200.dp),
            text = "Nema rezultata."
        )
    }
}

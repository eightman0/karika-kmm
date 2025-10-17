package karika.distribucija.ba.ui.view.main.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.main.vendor.details.filter.FilterSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_filter_alt
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VendorView(viewModel: VendorComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .hideKeyboard()
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Vendors(viewModel)
        }
    }
}

@Composable
private fun Vendors(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()
    val state = rememberLazyListState()

    KarikaText(
        modifier = Modifier,
        color = KarikaColors.Black,
        text = "DOBAVLJAČI",
        textSize = 20.sp,
        fontWeight = FontWeight.W700
    )
    Box(modifier = Modifier) {
        KarikaLazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = state
        ) {
            item {
                Filter(component)
            }
            items(
                items = vendors.chunked(2)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    it.forEach { vendor ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            VendorItem(vendor, component)
                        }
                    }
                    if (it.size == 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
        }
        EmptyState(component)
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }
    LaunchedEffect(Unit) {
        component.loadNextPage(reset = true)
    }
}

@Composable
fun VendorItem(vendor: Vendor, component: CommonComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .onClick {
                    component.showVendor(vendor)
                }
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Gray5)
                .aspectRatio(1f),
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = vendor.image(),
                contentScale = ContentScale.Inside
            )
        }
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Black,
            text = vendor.name(),
            textSize = 14.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Filter(component: VendorComponent) {
    val showState = component.showFilter.asState()
    val filter = component.selectedRegion.asState()
    val searchText = component.searchText.asState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SearchBoxBorder(
            modifier = Modifier
                .height(50.dp)
                .weight(1f),
            onValueChange = {
                searchText.value = it
            },
            onClose = {
                searchText.value = ""
                component.loadNextPage(true)
            },
            onSearchExecute = {
                if (searchText.value.length > 2) {
                    component.loadNextPage(true)
                }
            },
            placeholder = "Pretraži dobavljače.."
        )
        Box(
            modifier = Modifier
                .height(50.dp)
                .aspectRatio(1f)
                .onClick {
                    showState.negate()
                }
                .border(
                    width = 1.dp,
                    color = KarikaColors.Divider,
                    shape = RoundedCornerShape(12.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(12.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier
                    .size(24.dp),
                imageVector = vectorResource(Res.drawable.ic_filter_alt),
                contentDescription = "",
                tint = KarikaColors.Black1
            )
        }
    }

    if (filter.value.isNotEmpty()) {
        FlowRow(
            modifier = Modifier
                .padding(top = 16.dp),
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
            filter.value.forEach {
                Box(
                    modifier = Modifier
                        .rounded(color = KarikaColors.Gray19)
                ) {
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                component.selectedRegion.value -= it
                                component.loadNextPage(reset = true)
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

    FilterSheet(component)
}

@Composable
private fun EmptyState(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && !loader) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
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
}
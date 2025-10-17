package karika.distribucija.ba.ui.view.main.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarSearch
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.view.main.home.ProductItem
import karika.distribucija.ba.ui.view.main.vendor.VendorItem

@Composable
fun SearchView(component: SearchComponent) {
    val products by component.products.collectAsState()
    val vendors by component.vendors.collectAsState()
    val state = rememberLazyListState()
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopBarSearch(component)
        },
        component = component
    ) {
        KarikaLazyColumn(
            state = state,
            modifier = Modifier
                .hideKeyboard()
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (vendors.isNotEmpty()) {
                item {
                    KarikaText(
                        modifier = Modifier,
                        color = KarikaColors.Black,
                        text = "Dobavljači",
                        textSize = 20.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
            items(items = vendors.chunked(2)) { item ->
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
                            VendorItem(it, component)
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

            if (products.isNotEmpty()) {
                item {
                    KarikaText(
                        modifier = Modifier,
                        color = KarikaColors.Black,
                        text = "Proizvodi",
                        textSize = 20.sp,
                        fontWeight = FontWeight.W700
                    )
                }
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
            item { EmptyState(component) }
        }

        LaunchedEffect(state.canScrollForward) {
            if (!state.canScrollForward) {
                component.search(false)
            }
        }
    }
}

@Composable
private fun EmptyState(component: SearchComponent) {
    val vendors by component.vendors.collectAsState()
    val products by component.products.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && products.isNotEmpty() && !loader) {
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
}
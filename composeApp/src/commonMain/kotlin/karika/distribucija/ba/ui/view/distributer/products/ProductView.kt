package karika.distribucija.ba.ui.view.distributer.products

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig

@Composable
fun ProductsView(component: ProductsComponent) {
    val state = rememberLazyListState()
    val items by component.products.collectAsState()

    Box(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
    )

    LazyColumn(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Upravljanje artiklima",
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )
            YSpacer16()
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SearchBoxBorder(
                    modifier = Modifier
                        .height(50.dp)
                        .weight(1f),
                    borderShape = 100.dp,
                    onValueChange = {
                        component.searchText.value = it
                    },
                    onClose = {
                        component.searchText.value = ""
                        component.loadNextPage(true)
                    },
                    onSearchExecute = {
                        component.loadNextPage(true)
                    },
                    placeholder = "Pretraži artikle...",
                    preselected = component.searchText.value
                )
            }
        }
        items(items = items) {
            ProductItem(component, it)
        }
    }

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        SecondaryButtonFilled(
            modifier = Modifier
                .height(47.dp),
            title = "Dodaj novi artikal",
            fontWeight = FontWeight.W600,
            textSize = 18.sp
        ) {
            component.dashNavigate(DashConfig.ProductDetails(VendorProduct()))
        }
    }
    LoadingView1(component)

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    LaunchedEffect(Unit) {
        component.loadNextPage(true)
    }
}

@Composable
private fun ProductItem(component: ProductsComponent, product: VendorProduct) {
    Row(
        modifier = Modifier
            .onClick {
                component.dashNavigate(DashConfig.ProductDetails(product))
            }
            .background(color = KarikaColors.White)
            .border(width = 1.dp, color = KarikaColors.Gray21, shape = RoundedCornerShape(4.dp))
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                modifier = Modifier,
                text = product.name,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
            Box(
                modifier = Modifier
                    .rounded(color = product.isInStockColor(), shape = 6.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    text = product.isInStockLabel(),
                    fontWeight = FontWeight.W700,
                    color = product.isInStockTextColor(),
                    textSize = 12.sp
                )
            }
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Status artikla: ${product.status()}",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
        }
        Column(
            modifier = Modifier
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                modifier = Modifier,
                text = product.price(),
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}
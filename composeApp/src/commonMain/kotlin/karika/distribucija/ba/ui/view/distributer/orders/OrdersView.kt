package karika.distribucija.ba.ui.view.distributer.orders

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
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_filter_outline
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrdersView(component: OrdersComponent) {
    val state = rememberLazyListState()
    val items by component.orders.collectAsState()

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
                text = "Upravljanje narudžbama",
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
                        component.addSearchQuery()
                        component.loadNextPage(true)
                    },
                    onSearchExecute = {
                        component.addSearchQuery()
                        component.loadNextPage(true)
                    },
                    placeholder = "Pretraži narudžbe..."
                )
                IconTextItem(
                    modifier = Modifier
                        .onClick {
                            component.showFilterState.negate()
                        },
                    icon = vectorResource(Res.drawable.ic_filter_outline),
                    iconColor = if (component.hasFilter()) KarikaColors.Blue else KarikaColors.Gray2,
                    textColor = if (component.hasFilter()) KarikaColors.Blue else KarikaColors.Gray2,
                    text = "Filteri",
                    fontWeight = FontWeight.W400,
                    textSize = 14.sp,
                    iconPosition = FabPosition.Start
                )
            }
            if (component.hasFilter()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    KarikaText(
                        modifier = Modifier
                            .onClick {
                                component.clear()
                            },
                        text = "Očisti",
                        color = KarikaColors.Blue,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
        }
        items(items = items) {
            OrderItem(component, it)
        }
    }
    LoadingView1(component)

    OrderFilterSheet(component)

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
private fun OrderItem(component: OrdersComponent, vendorOrder: VendorOrder) {
    Row(
        modifier = Modifier
            .onClick {
                component.dashNavigate(DashConfig.OrderDetails(vendorOrder))
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
                text = vendorOrder.b2bPravnoLice,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
            Box(
                modifier = Modifier
                    .rounded(color = vendorOrder.statusColor(), shape = 6.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    text = vendorOrder.status(),
                    fontWeight = FontWeight.W700,
                    color = vendorOrder.statusTextColor(),
                    textSize = 12.sp
                )
            }
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = vendorOrder.createdAt,
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
                text = vendorOrder.totalAmount() + " KM",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}
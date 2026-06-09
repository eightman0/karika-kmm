package karika.distribucija.ba.ui.view.distributer.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.domain.model.VendorOrder
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_filter_outline
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrdersView(component: OrdersComponent) {
    val state = rememberLazyListState()
    val items by component.orders.collectAsState()
    val minOrderAmount by component.minOrderValue.asState()

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
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KarikaColors.White, shape = RoundedCornerShape(4.dp))
                    .border(
                        width = 1.dp,
                        color = KarikaColors.Gray21,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(16.dp)
                    .height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    KarikaText(
                        text = "Postavi minimalnu vrijednost narudžbe",
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                    KarikaText(
                        text = "$minOrderAmount KM",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(color = KarikaColors.Gray20, shape = CircleShape)
                        .onClick {
                            component.showMinOrderModal.value = true
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier.size(20.dp),
                        imageVector = vectorResource(Res.drawable.ic_arrow_right),
                        tint = KarikaColors.Gray2,
                        contentDescription = null
                    )
                }
            }
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
                        component.filter()
                        component.loadNextPage(true)
                    },
                    onSearchExecute = {
                        component.filter()
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
        items(items = items.toList()) {
            OrderItem(component, it)
        }
    }
    EmptyState(component)

    OrderFilterSheet(component)

    if (component.showMinOrderModal.value) {
        MinOrderModal(component)
    }

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
private fun MinOrderModal(component: OrdersComponent) {
    Dialog(
        onDismissRequest = {
            component.showMinOrderModal.value = false
        },
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = KarikaColors.White, shape = RoundedCornerShape(12.dp))
                    .padding(24.dp)
            ) {
                Column(
                    modifier = Modifier,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaText(
                        text = "Postavi minimalnu vrijednost narudžbe",
                        color = KarikaColors.Gray2,
                        textSize = 18.sp,
                        fontWeight = FontWeight.W700
                    )
                    KarikaTextField1(
                        modifier = Modifier.fillMaxWidth(),
                        title = "Iznos",
                        value = component.minOrderValueModal.asState(),
                        placeholder = "Unesite iznos",
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Done,
                        allowedChars = KarikaConstants.numbers.plus(".").map { it },
                        trailingIcons = {
                            KarikaText(
                                text = "KM",
                                color = KarikaColors.Gray4,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400
                            )
                        }
                    )
                    HorizontalSecondaryButtons(
                        modifier = Modifier.fillMaxWidth(),
                        primaryTitle = "Sačuvaj",
                        secondaryTitle = "Otkaži",
                        onClick = {
                            if (it == "Sačuvaj") {
                                component.updateMinOrderAmount()
                            } else {
                                component.showMinOrderModal.value = false
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun OrderItem(component: OrdersComponent, vendorOrder: VendorOrder) {
    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .blur(radius = if (vendorOrder.locked()) 5.dp else 0.dp)
                .background(Color.White.copy(alpha = 0.3f)),
        ) {
            Row(
                modifier = Modifier
                    .onClick {
                        component.dashNavigate(DashConfig.OrderDetails(vendorOrder))
                    }
                    .background(color = KarikaColors.White)
                    .border(
                        width = 1.dp,
                        color = KarikaColors.Gray21,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KarikaText(
                            modifier = Modifier,
                            text = vendorOrder.b2bPravnoLice,
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W700
                        )
                        if (vendorOrder.hasChanges()) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = KarikaColors.Red, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                KarikaText(
                                    modifier = Modifier
                                        .padding(0.dp),
                                    text = "1",
                                    textSize = 10.sp,
                                    fontWeight = FontWeight.W400,
                                    color = KarikaColors.White
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        KarikaText(
                            modifier = Modifier,
                            text = "#${vendorOrder.orderId}",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W700,
                            textAlign = TextAlign.End
                        )
                    }
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
                        text = vendorOrder.date(),
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (vendorOrder.locked()) {
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Za prikaz detalja narudžbe, molimo Vas da zaključite prethodne narudžbe tako što ćete ih označiti kao odobrene ili odbijene!",
                        color = KarikaColors.Primary,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W700
                    )
                }
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
}

@Composable
private fun EmptyState(component: OrdersComponent) {
    val orders by component.orders.collectAsState()
    if (orders.isNotEmpty()) {
        return
    }
    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Blue,
            textSize = 16.sp,
            fontWeight = FontWeight.W700,
            text = "Nema narudžbi."
        )
    }
}
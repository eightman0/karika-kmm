package karika.distribucija.ba.ui.view.main.profile.order.details

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrderProduct
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.main.profile.order.components.AttachBillModal
import karika.distribucija.ba.ui.view.main.profile.order.components.CancelOrderModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.max

@Composable
fun OrderDetailsView(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("#${order.incrementId}") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OrderCommon(component)
        }
    }
}

@Composable
private fun OrderCommon(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth(),
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            fontWeight = FontWeight.W700,
            textSize = 16.sp,
            text = "Narudžba br.${order.incrementId}"
        )
        YSpacer8()
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray15,
            fontWeight = FontWeight.W600,
            textSize = 14.sp,
            text = order.date()
        )
        YSpacer16()
        TableHeaderRow()
        order.orders.flatMap { it.products }.forEach {
            TableRow(it)
        }
        YSpacer16()
        PriceBox(order)
        YSpacer16()
       //HorizontalButtons(
       //    modifier = Modifier,
       //    primaryTitle = "Naruči ponovo",
       //    secondaryTitle = "Isprintaj"
       //) {
       //    if (it == "Naruči ponovo") {
       //        component.orderAgain(order)
       //    } else {

       //    }
       //}
        PrimaryButtonFilled(
            title = "Naruči ponovo",
        ) {
            component.orderAgain(order)
        }
        YSpacer16()
        VendorOrder(order, component)
    }
}

@Composable
fun VendorOrder(order: OrdersResponse, component: OrderDetailsComponent) {
    val comment = remember { mutableStateOf("") }
    val cancelModal = remember { mutableStateOf<Order?>(null) }
    val attachBillModal = remember { mutableStateOf<Order?>(null) }

    KarikaText(
        modifier = Modifier,
        color = KarikaColors.Black,
        fontWeight = FontWeight.W700,
        textSize = 16.sp,
        text = "Detalji narudžbe po dobavljaču"
    )
    YSpacer16()
    order.orders.forEach {
        Column(
            modifier = Modifier
                .border(width = 0.5.dp, color = KarikaColors.Border)
                .background(color = KarikaColors.Gray12)
                .fillMaxWidth()
        ) {
            YSpacer16()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Gray13,
                    text = "Dobavljač:",
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Blue,
                    text = it.vendorName,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    decoration = TextDecoration.Underline
                )
            }
            YSpacer8()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Gray13,
                    text = "UKUPNO VPC",
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Gray2,
                    text = it.vpcString(),
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }
            YSpacer8()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Gray13,
                    text = "UKUPNO SA PDV",
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    color = KarikaColors.Gray2,
                    text = it.vpcPdvString(),
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }
            YSpacer8()
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(start = 16.dp),
                    text = "STATUS",
                    fontWeight = FontWeight.W600,
                    color = KarikaColors.Gray13,
                    textSize = 10.sp
                )
                Box(
                    modifier = Modifier
                        .rounded(color = it.statusColor(), shape = 6.dp)
                ) {
                    KarikaText(
                        modifier = Modifier
                            .padding(8.dp),
                        text = it.status(),
                        fontWeight = FontWeight.W700,
                        color = it.statusTextColor(),
                        textSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                //Icon(
                //    modifier = Modifier
                //        .padding(end = 16.dp),
                //    imageVector = vectorResource(Res.drawable.ic_arrow_down),
                //    tint = KarikaColors.Black1,
                //    contentDescription = ""
                //)
            }
            YSpacer8()
            TableHeaderRow()
            it.products.forEach { vp ->
                TableRow(vp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .onClick {
                            if (!it.canceled()) {
                                cancelModal.value = it
                            }
                        }
                        .weight(1f),
                    text = "Otkaži narudžbu",
                    fontWeight = FontWeight.W600,
                    color = if (it.canceled()) KarikaColors.Divider else KarikaColors.Primary,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                KarikaText(
                    modifier = Modifier
                        .onClick {
                            component.navigateToComments(it)
                        }
                        .weight(1f),
                    text = "Komentari(${it.commentCount})",
                    fontWeight = FontWeight.W600,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
            if (it.showAddBill()) {
                YSpacer8()
                Box(
                    modifier = Modifier
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .onClick {
                                attachBillModal.value = it
                            },
                        text = "Pošalji predračun",
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Primary,
                        textSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                }
                YSpacer8()
            }
        }
        YSpacer16()
    }


    if (cancelModal.value != null) {
        CancelOrderModal(
            onSubmit = { reason, com ->
                component.cancelOrder(
                    cancelModal.value?.orderId,
                    cancelModal.value?.vendorId.toString(),
                    reason,
                    com
                )
                cancelModal.value = null
            },
            onCancel = {
                cancelModal.value = null
            }
        )
    }

    if (attachBillModal.value != null) {
        AttachBillModal(
            component = component,
            onSubmit = { message, file ->
                component.attachBill(
                    attachBillModal.value,
                    message,
                    file
                )
                attachBillModal.value = null
            },
            onCancel = {
                attachBillModal.value = null
            }
        )
    }
}

@Composable
fun PriceBox(order: OrdersResponse) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            text = "Ukupna VPC:"
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W600,
            textSize = 16.sp,
            text = order.vpcString()
        )
    }
    YSpacer16()
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            text = "Ukupno PDV 17%:"
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W600,
            textSize = 16.sp,
            text = order.pdvString()
        )
    }
    YSpacer16()
    Row(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            text = "Ukupno sa PDV:"
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray2,
            fontWeight = FontWeight.W600,
            textSize = 16.sp,
            text = order.vpcPdvString()
        )
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .background(color = KarikaColors.Gray16)
            .border(width = 0.5.dp, color = KarikaColors.Border)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(1.5f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "ARTIKAL"
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "VPC"
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "KOLIČINA"
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "UKUPNO"
            )
        }
    }
}

@Composable
private fun TableRow(order: OrderProduct) {
    val height = remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .border(width = 0.5.dp, color = KarikaColors.Border)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    height.value = it.size.height
                }
                .weight(1.5f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                atext = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Gray15,
                            fontSize = 12.sp
                        )
                    ) {
                        append(order.name).append("\n")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray15,
                            fontSize = 12.sp
                        )
                    ) {
                        append(order.vendorName())
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = order.vpc()
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = order.qty()
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .weight(1f)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = order.total()
            )
        }
    }
}
package karika.distribucija.ba.ui.view.shop.profile.order

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Order
import karika.distribucija.ba.domain.model.OrdersResponse
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPickerSmall
import karika.distribucija.ba.ui.components.KarikaPickerSmall1
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.components.roundedWithBorder
import karika.distribucija.ba.ui.view.shop.profile.order.components.AttachBillModal
import karika.distribucija.ba.ui.view.shop.profile.order.components.CancelOrderModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import karikav2.composeapp.generated.resources.ic_gift
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrdersView(component: OrdersComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Moje narudžbe") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FilterView(component)
            Orders(component)
        }
    }
}

@Composable
private fun FilterView(component: OrdersComponent) {
    val statusSort = mutableStateOf("Sve").asState()
    val dateSort = mutableStateOf("Najnovije").asState()

    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaPickerSmall1(
            modifier = Modifier
                .weight(1f),
            value = statusSort,
            values = mutableStateOf(
                listOf(
                    Pair("Sve", KarikaColors.Gray2),
                    Pair("Na čekanju", KarikaColors.Blue),
                    Pair("Odobrena", KarikaColors.Green3),
                    Pair("Otkazana", KarikaColors.Gray2),
                    Pair("Odbijena", KarikaColors.Red),
                    Pair("Čekanje na uplatu", KarikaColors.Orange),
                    Pair("Uplaćena", KarikaColors.Orange)
                )
            ).asState()
        ) {
            component.status = when (statusSort.value) {
                "Odobrena" -> "approved"
                "Na čekanju" -> "pending"
                "Odbijena" -> "rejected"
                "Otkazana" -> "cancelled"
                "Čekanje na uplatu" -> "estimate-sent"
                "Uplaćena" -> "bill-sent"
                else -> ""
            }
            component.loadNextPage(reset = true)
        }
        KarikaPickerSmall(
            modifier = Modifier
                .weight(1f),
            value = dateSort,
            values = mutableStateOf(listOf("Najnovije", "Najstarije")).asState()
        ) {
            component.sortDirection = if (dateSort.value == "Najnovije") "DESC" else "ASC"
            component.loadNextPage(reset = true)
        }
    }
}

@Composable
private fun Orders(component: OrdersComponent) {
    val state = rememberLazyListState()
    val items by component.orders.collectAsState()
    val shouldScrollToTop by component.shouldScrollToTop.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth(),
        state = state,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = items, key = { it.orderId }) {
            OrderItem(it, component)
        }
        item { EmptyState(component) }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }

    LaunchedEffect(shouldScrollToTop) {
        if (component.shouldScrollToTop.value) {
            state.scrollToItem(0)
            component.scrollHandled()
        }
    }
}

@Composable
private fun OrderItem(order: OrdersResponse, component: OrdersComponent) {
    Column(
        modifier = Modifier
            .onClick { component.navigateDetails(order) }
            .roundedWithBorder(
                color = KarikaColors.Gray14,
                borderColor = KarikaColors.Border,
                shape = 4.dp
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .rounded(color = KarikaColors.White, shape = 4.dp)
                .fillMaxWidth()
        ) {
            YSpacer16()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = "#${order.incrementId}",
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = order.vpcString(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = order.date(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = "UKUPNO VPC",
                    color = KarikaColors.Gray13,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600,
                )
            }
            YSpacer16()
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = order.vpcPdvString(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = order.bonus(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                )
            }
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = "UKUPNO SA PDV",
                    color = KarikaColors.Gray13,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600
                )
                Row(
                    modifier = Modifier
                        .weight(1f)
                ) {
                    Icon(
                        modifier = Modifier
                            .size(12.dp),
                        imageVector = vectorResource(Res.drawable.ic_gift),
                        tint = KarikaColors.Green1,
                        contentDescription = ""
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "OSTVARENI BONUS",
                        color = KarikaColors.Gray13,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W600
                    )
                }
            }
            YSpacer16()
            HorizontalButtons(
                primaryTitle = "Naruči ponovo",
                secondaryTitle = "Vidi narudžbu"
            ) {
                if (it == "Vidi narudžbu") {
                    component.navigateDetails(order)
                    return@HorizontalButtons
                }

                component.orderAgain(order)
            }
            YSpacer16()
        }
        order.orders.forEach {
            VendorItem(it, component)
        }
        Spacer(modifier = Modifier.height(1.dp))
    }
}

@Composable
private fun VendorItem(order: Order, component: OrdersComponent) {
    val cancelModal = remember { mutableStateOf<Order?>(null) }
    val attachBillModal = remember { mutableStateOf<Order?>(null) }
    val showAdditionalOptions = mutableStateOf(false).asState()
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .roundedWithBorder(
                color = KarikaColors.White,
                borderColor = KarikaColors.Border,
                shape = 4.dp
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray13,
                        fontSize = 10.sp
                    )
                ) {
                    append("DOBAVLJAČ   ")
                }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "",
                        styles = TextLinkStyles(),
                        linkInteractionListener = {
                            component.showVendor(
                                Vendor(
                                    entityId = order.vendorId ?: 0,
                                    publicName = order.vendorName
                                )
                            )
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Blue,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(order.vendorName)
                    }
                }
            }
        )
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray13,
                        fontSize = 10.sp
                    )
                ) {
                    append("UKUPNO VPC   ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray2,
                        fontSize = 14.sp
                    )
                ) {
                    append(order.vpcString())
                }
            }
        )
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray13,
                        fontSize = 10.sp
                    )
                ) {
                    append("UKUPNO SA PDV   ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray2,
                        fontSize = 14.sp
                    )
                ) {
                    append(order.vpcPdvString())
                }
            }
        )
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
                    .rounded(color = order.statusColor(), shape = 6.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    text = order.status(),
                    fontWeight = FontWeight.W700,
                    color = order.statusTextColor(),
                    textSize = 12.sp
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Icon(
                modifier = Modifier
                    .onClick {
                        showAdditionalOptions.negate()
                    }
                    .padding(end = 16.dp),
                imageVector = vectorResource(
                    if (!showAdditionalOptions.value) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_up
                ),
                tint = KarikaColors.Black1,
                contentDescription = ""
            )
        }
        if (showAdditionalOptions.value) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .onClick {
                            if (!order.canceled()) {
                                cancelModal.value = order
                            }
                        }
                        .weight(1f),
                    text = "Otkaži narudžbu",
                    fontWeight = FontWeight.W600,
                    color = if (order.canceled()) KarikaColors.Divider else KarikaColors.Primary,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                KarikaText(
                    modifier = Modifier
                        .onClick {
                            if (order.commentsArchived()) {
                                component.showWarningMessage("Komentari narudžbe su arhivirani.")
                            } else {
                                component.navigateToComments(order)
                            }
                        }
                        .weight(1f),
                    text = "Komentari(${order.commentCount})",
                    fontWeight = FontWeight.W600,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        if (order.showAddBill()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .onClick {
                            attachBillModal.value = order
                        },
                    text = "Pošalji uplatnicu",
                    fontWeight = FontWeight.W600,
                    color = KarikaColors.Primary,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
        YSpacer8()
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
private fun EmptyState(component: OrdersComponent) {
    val vendors by component.orders.collectAsState()
    if (vendors.isNotEmpty()) {
        return
    }
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
            text = if (component.status.isEmpty()) "Nema narudžbi" else "Nema narudžbi za izabrani status."
        )
    }
}
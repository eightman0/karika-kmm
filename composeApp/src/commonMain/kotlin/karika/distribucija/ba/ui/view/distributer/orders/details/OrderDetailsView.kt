package karika.distribucija.ba.ui.view.distributer.orders.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.HttpClientProvider.imageUrl
import karika.distribucija.ba.domain.model.Comment
import karika.distribucija.ba.domain.model.VendorProduct
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.orders.details.component.ApproveOrderModal
import karika.distribucija.ba.ui.view.distributer.orders.details.component.AttachBillModal
import karika.distribucija.ba.ui.view.distributer.orders.details.component.EditOrderSheet
import karika.distribucija.ba.ui.view.distributer.orders.details.component.RejectOrderModal
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import karikav2.composeapp.generated.resources.ic_cancel
import karikav2.composeapp.generated.resources.ic_checked_circle
import karikav2.composeapp.generated.resources.ic_pdf
import karikav2.composeapp.generated.resources.ic_print
import karikav2.composeapp.generated.resources.ic_send_receipt
import karikav2.composeapp.generated.resources.img_ab_post
import karikav2.composeapp.generated.resources.img_express_post
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun OrderDetailsView(component: OrderDetailsComponent) {
    val scroll = rememberScrollState()

    Box(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
    )
    Column(
        modifier = Modifier
            .padding(16.dp)
            .hideKeyboard(true)
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
            .verticalScroll(scroll)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconTextItem(
            modifier = Modifier
                .onClick {
                    component.dashBack()
                },
            icon = vectorResource(Res.drawable.ic_arrow_back),
            iconColor = KarikaColors.Gray2,
            textColor = KarikaColors.Gray2,
            text = "Nazad na upravljanje narudžbama",
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            iconPosition = FabPosition.Start
        )
        OrderInfo(component)
        OrderTax(component)
        OrderShipping(component)
        Comments(component)
        YSpacer16()
    }
}

@Composable
private fun OrderInfo(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val dropdownState = remember { mutableStateOf(false) }
    val approveModal = mutableStateOf(false).asState()
    val rejectModal = mutableStateOf(false).asState()
    val attachBillModal = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .hideKeyboard(true)
            .background(color = KarikaColors.White)
            .border(width = 1.dp, color = KarikaColors.Gray21, shape = RoundedCornerShape(4.dp)),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = "Informacije o narudžbi",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            Box(modifier = Modifier) {
                Button(
                    modifier = Modifier
                        .height(40.dp),
                    shape = RoundedCornerShape(100.dp),
                    onClick = {
                        dropdownState.negate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KarikaColors.Blue,
                        disabledContentColor = KarikaColors.Secondary
                    ),
                    enabled = !order.locked() && !order.isApproved() && !order.isRejected() && !order.isCancelled(),
                    contentPadding = PaddingValues(horizontal = 12.dp)
                ) {
                    Row(
                        modifier = Modifier,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier,
                            text = "Akcije |",
                            color = KarikaColors.White,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W600
                        )
                        Icon(
                            imageVector = vectorResource(Res.drawable.ic_arrow_down),
                            tint = KarikaColors.White,
                            contentDescription = ""
                        )
                    }
                }
                if (dropdownState.value) {
                    DropdownMenu(
                        modifier = Modifier,
                        offset = DpOffset(x = 0.dp, y = (8).dp),
                        shadowElevation = 10.dp,
                        shape = RoundedCornerShape(8.dp),
                        containerColor = KarikaColors.White,
                        expanded = true,
                        onDismissRequest = {
                            dropdownState.negate()
                        }
                    ) {
                        if (!order.isApproved() && !order.isRejected() && !order.isCancelled()) {
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    approveModal.negate()
                                },
                                text = {
                                    IconTextItem(
                                        modifier = Modifier,
                                        icon = vectorResource(Res.drawable.ic_checked_circle),
                                        iconColor = KarikaColors.Green5,
                                        textColor = KarikaColors.Green5,
                                        text = "Odobri narudžbu"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    rejectModal.negate()
                                },
                                text = {
                                    IconTextItem(
                                        modifier = Modifier,
                                        icon = vectorResource(Res.drawable.ic_cancel),
                                        iconColor = KarikaColors.Red,
                                        textColor = KarikaColors.Red,
                                        text = "Odbij narudžbu"
                                    )
                                }
                            )
                        }
                        if (!order.isApproved()) {
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    component.createInvoice()
                                },
                                text = {
                                    IconTextItem(
                                        modifier = Modifier,
                                        icon = vectorResource(Res.drawable.ic_send_receipt),
                                        iconColor = KarikaColors.Gray2,
                                        textColor = KarikaColors.Gray2,
                                        text = "Generiši predračun"
                                    )
                                }
                            )
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    component.getBill()
                                },
                                text = {
                                    IconTextItem(
                                        modifier = Modifier,
                                        icon = vectorResource(Res.drawable.ic_print),
                                        iconColor = KarikaColors.Gray2,
                                        textColor = KarikaColors.Gray2,
                                        text = "Printaj narudžbu"
                                    )
                                }
                            )
                        }
                        if (order.isPending()) {
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    attachBillModal.negate()
                                },
                                text = {
                                    IconTextItem(
                                        modifier = Modifier,
                                        icon = vectorResource(Res.drawable.ic_send_receipt),
                                        iconColor = KarikaColors.Blue,
                                        textColor = KarikaColors.Blue,
                                        text = "Pošalji predračun"
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        HorizontalDivider(
            modifier = Modifier.padding(horizontal = 16.dp),
            color = KarikaColors.Divider
        )
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
                KarikaText(
                    modifier = Modifier,
                    text = "STATUS NARUDŽBE",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = order.orderId,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "BROJ NARUDŽBE",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = order.date(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "DATUM NARUDŽBE",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = order.totalAmount() + " KM",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "UKUPNO VPC",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.totalAmountWithPdv(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "UKUPNO SA PDV",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.totalCommission(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "KARIKA PROVIZIJA",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .blur(radius = if (order.locked()) 5.dp else 0.dp),
                text = order.b2bPravnoLice,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier,
                text = "NAZIV PRAVNOG LICA",
                color = KarikaColors.Gray13,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.pdvNumber ?: "-",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "PDV BROJ",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.idNumber ?: "-",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "ID BROJ",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.billingName ?: "-",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "IME KONTAKT OSOBE",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.email(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "KONTAKT EMAIL",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.telephone(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "KONTAKT TELEFON",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .blur(radius = if (order.locked()) 5.dp else 0.dp),
                    text = order.address(),
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier,
                    text = "ADRESA ZA ISPORUKU",
                    color = KarikaColors.Gray13,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
        }
        YSpacer16()
    }


    if (approveModal.value) {
        ApproveOrderModal(
            value = component.getDelivery(),
            onCancel = {
                approveModal.negate()
            },
            onSubmit = { type, typeId, message ->
                approveModal.negate()
                component.approve(message, type, typeId != 0)
            }
        )
    }

    if (rejectModal.value) {
        RejectOrderModal(
            onCancel = {
                rejectModal.negate()
            },
            onSubmit = { message ->
                rejectModal.negate()
                component.reject(message)
            }
        )
    }

    if (attachBillModal.value) {
        AttachBillModal(
            component = component,
            onSubmit = { message, file ->
                component.estimate(message, file.second, file.first)
                attachBillModal.value = false
            },
            onCancel = {
                attachBillModal.value = false
            }
        )
    }
}

@Composable
private fun OrderShipping(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val expand = mutableStateOf(false).asState()

    Column(
        modifier = Modifier
            .hideKeyboard(true)
            .fillMaxWidth()
            .background(color = KarikaColors.Yellow1)
            .border(
                width = 1.dp,
                color = KarikaColors.Yellow,
                shape = RoundedCornerShape(4.dp)
            ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier
                .onClick {
                    expand.negate()
                }
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = "Usluga dostave",
                color = KarikaColors.Yellow2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            Icon(
                imageVector = vectorResource(
                    if (!expand.value) Res.drawable.ic_arrow_down else Res.drawable.ic_arrow_up
                ),
                contentDescription = "",
                tint = KarikaColors.Yellow2
            )
        }
        if (expand.value) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Adresa za utovar",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Kontakt osoba*",
                    value = component.contactName.asState(),
                    placeholder = "Kontakt osoba",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Email adresa*",
                    value = component.contactEmail.asState(),
                    placeholder = "Email adresa",
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Telefon*",
                    value = component.contactPhone.asState(),
                    placeholder = "Telefon",
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Grad*",
                    value = component.contactCity.asState(),
                    placeholder = "Grad",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Adresa*",
                    value = component.contactAddress.asState(),
                    placeholder = "Adresa",
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Poštanski broj*",
                    value = component.contactPostal.asState(),
                    placeholder = "Poštanski broj",
                    keyboardType = KeyboardType.Number,
                    allowedChars = KarikaConstants.numbers,
                    imeAction = ImeAction.Next
                )
            }


            KarikaText(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp),
                text = "Kalkulator dostave",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Unesite dimenzije paketa i izračunajte cijene za brzu dostavu, ukoliko odobrite paket u tom koraku ćete moći izabrati da li i koju opciju dostave želite",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Ukupna širina*",
                    value = component.packageWidth.asState(),
                    placeholder = "Ukupna širina",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    trailingIcons = {
                        KarikaText(
                            modifier = Modifier,
                            text = "cm",
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Ukupna visina*",
                    value = component.packageHeight.asState(),
                    placeholder = "Ukupna visina",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    trailingIcons = {
                        KarikaText(
                            modifier = Modifier,
                            text = "cm",
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Ukupna dubina*",
                    value = component.packageDepth.asState(),
                    placeholder = "Ukupna dubina",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    trailingIcons = {
                        KarikaText(
                            modifier = Modifier,
                            text = "cm",
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Ukupna težina*",
                    value = component.packageWeight.asState(),
                    placeholder = "Ukupna težina",
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Next,
                    trailingIcons = {
                        KarikaText(
                            modifier = Modifier,
                            text = "kg",
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = KarikaColors.Divider
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .width(93.dp),
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(Res.drawable.img_ab_post),
                    contentDescription = ""
                )
                KarikaText(
                    modifier = Modifier,
                    text = "A2B Express",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
            }
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Cijena dostave sa PDV: ${component.a2b.asState().value}",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = KarikaColors.Divider
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    modifier = Modifier
                        .width(93.dp),
                    contentScale = ContentScale.FillWidth,
                    painter = painterResource(Res.drawable.img_express_post),
                    contentDescription = ""
                )
                KarikaText(
                    modifier = Modifier,
                    text = "EuroExpress",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
            }
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Cijena dostave sa PDV: ${component.express.asState().value}",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = KarikaColors.Divider
            )

            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Napomena za dostavu",
                color = KarikaColors.Gray4,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
            KarikaTextField2(
                modifier = Modifier.padding(horizontal = 16.dp)
                    .heightIn(min = 66.dp)
                    .fillMaxWidth(),
                value = component.deliveryNotes.asState(),
                placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next
            )

            PrimaryButtonFilled(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                    .height(48.dp)
                    .fillMaxWidth(),
                title = "Izračunaj cijenu",
                enabled = !order.locked() && order.shouldShowShipping()
            ) {
                component.calculateShipping()
            }
        }
    }
}

@Composable
private fun OrderTax(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .hideKeyboard(true)
                .fillMaxWidth()
                .blur(radius = if (order.locked()) 5.dp else 0.dp)
                .background(color = KarikaColors.White)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Gray21,
                    shape = RoundedCornerShape(4.dp)
                )
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f),
                    text = "Specifikacija narudžbe",
                    fontWeight = FontWeight.W700,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp
                )
            }
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState())
                    .fillMaxWidth()
            ) {
                Column {
                    TableHeaderRow()
                    order.products.forEach {
                        TableRow(
                            component = component,
                            item = it,
                            name = it.name ?: "",
                            originalRabat = it.originalRabat(),
                            rabat = it.rabat(),
                            priceVpc = it.priceVpc(),
                            originalQty = it.originalQty(),
                            qty = it.qty(),
                            totalVpc = it.totalVpc(),
                            totalWithPdv = it.totalWithPdv(),
                            percent = it.commissionPercent(),
                            tax = it.commission()
                        )
                    }
                }
            }
            YSpacer16()
        }
        if (order.locked()) {
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
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(color = KarikaColors.Gray16)
            .border(width = 0.5.dp, color = KarikaColors.Border)
    ) {
        Box(
            modifier = Modifier
                .width(200.dp)
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
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "RABAT %"
            )
        }
        Box(
            modifier = Modifier
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "CIJENA VPC"
            )
        }
        Box(
            modifier = Modifier
                .width(150.dp)
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
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "UKUPNO VPC"
            )
        }
        Box(
            modifier = Modifier
                .width(110.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "UKUPNO SA PDV"
            )
        }
        Box(
            modifier = Modifier
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "PROVIZIJA %"
            )
        }
        Box(
            modifier = Modifier
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "PROVIZIJA"
            )
        }
        Box(
            modifier = Modifier
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "IZMIJENI"
            )
        }
    }
}

@Composable
private fun TableRow(
    component: OrderDetailsComponent,
    item: VendorProduct,
    name: String,
    originalRabat: String,
    rabat: String,
    priceVpc: String,
    originalQty: String,
    qty: String,
    totalVpc: String,
    totalWithPdv: String,
    percent: String,
    tax: String
) {
    val order by component.order.collectAsState()
    val height = remember { mutableStateOf(0) }
    val edit = component.editOrderItem.asState()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(color = KarikaColors.White)
            .border(width = 0.5.dp, color = KarikaColors.Border)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    height.value = it.size.height
                }
                .width(200.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = name
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (originalRabat.isNotEmpty()) {
                    KarikaText(
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = KarikaColors.Gray1,
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2)
                            )
                        },
                        color = KarikaColors.Gray17,
                        fontWeight = FontWeight.W600,
                        textSize = 12.sp,
                        text = originalRabat
                    )
                }
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    color = KarikaColors.Gray17,
                    fontWeight = FontWeight.W600,
                    textSize = 12.sp,
                    text = rabat
                )
            }
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = priceVpc
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(150.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (originalQty.isNotEmpty()) {
                    KarikaText(
                        modifier = Modifier.drawBehind {
                            drawLine(
                                color = KarikaColors.Gray1,
                                strokeWidth = 1.dp.toPx(),
                                start = Offset(0f, size.height / 2),
                                end = Offset(size.width, size.height / 2)
                            )
                        },
                        color = KarikaColors.Gray17,
                        fontWeight = FontWeight.W600,
                        textSize = 12.sp,
                        text = originalQty
                    )
                }
                KarikaText(
                    modifier = Modifier
                        .padding(8.dp),
                    color = KarikaColors.Gray17,
                    fontWeight = FontWeight.W600,
                    textSize = 12.sp,
                    text = qty
                )
            }

        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = totalVpc
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(110.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = totalWithPdv
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = percent
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = tax
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .width(100.dp)
                .onClick(!order.locked() && !order.isCancelled() && !order.isRejected() && !order.isApproved()) {
                    edit.value = item
                }
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterEnd
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Blue,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = "Izmijeni"
            )
        }
    }
    if (edit.value != null) {
        EditOrderSheet(component)
    }
}

@Composable
private fun Comments(component: OrderDetailsComponent) {
    val comments by component.comments.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = KarikaColors.White)
            .border(width = 1.dp, color = KarikaColors.Gray21, shape = RoundedCornerShape(4.dp)),
    ) {
        comments.forEach {
            Box(
                modifier = Modifier
                    .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            ) {
                CommentItem(it, component)
            }
        }
        YSpacer16()
        EnterComment(component)
    }
}

@Composable
private fun EnterComment(component: OrderDetailsComponent) {
    val order by component.order.collectAsState()
    val comment = component.newComment.asState()
    val keyboardController = LocalSoftwareKeyboardController.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaTextField2(
            modifier = Modifier
                .weight(1f),
            value = comment,
            placeholder = "Napiši komentar",
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            trailingIcons = {
                // Icon(
                //     modifier = Modifier
                //         .onClick {
                //             component.pickFile()
                //         },
                //     imageVector = vectorResource(Res.drawable.ic_attachment),
                //     tint = KarikaColors.Gray2,
                //     contentDescription = ""
                // )
            }
        )
        SecondaryButtonFilled(
            modifier = Modifier
                .height(50.dp),
            title = "Pošalji",
            enabled = comment.value.isNotEmpty() && !order.locked() && !order.isCancelled() && !order.isRejected()
        ) {
            keyboardController?.hide()
            component.sendComment()
        }
    }
}

@Composable
fun CommentItem(comment: Comment, component: OrderDetailsComponent) {
    if (comment.isMine()) {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Column(
                modifier = Modifier
                    .padding(start = 32.dp)
                    .background(
                        color = KarikaColors.MineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.End
            ) {
                HtmlTextWithStyles(
                    modifier = Modifier
                        .padding(16.dp),
                    html = comment.message(),
                    textColor = KarikaColors.White
                )
                comment.files?.forEach {
                    if (it.type?.startsWith("image") == true) {
                        KarikaImage(
                            modifier = Modifier
                                .padding(16.dp)
                                .width(150.dp)
                                .onClick {
                                    component.showImagePreview(imageUrl(it.url ?: ""))
                                },
                            model = imageUrl(it.url ?: ""),
                            contentScale = ContentScale.Inside
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    component.downloadReceipt(it)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_pdf),
                                tint = KarikaColors.Primary,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            KarikaText(
                                text = it.name ?: "",
                                fontWeight = FontWeight.Bold,
                                textSize = 12.sp,
                                color = KarikaColors.Primary
                            )
                        }
                    }
                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.White,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxWidth(),
            contentAlignment = Alignment.CenterStart
        ) {
            Column(
                modifier = Modifier
                    .padding(end = 32.dp)
                    .background(
                        color = KarikaColors.NotMineMessage,
                        shape = RoundedCornerShape(
                            topStart = 8.dp,
                            topEnd = 8.dp,
                            bottomStart = 8.dp
                        )
                    ),
                horizontalAlignment = Alignment.Start
            ) {
                HtmlTextWithStyles(
                    modifier = Modifier
                        .padding(16.dp),
                    html = comment.message(),
                    textColor = KarikaColors.Gray2
                )
                comment.files?.forEach {
                    if (it.type?.startsWith("image") == true) {
                        KarikaImage(
                            modifier = Modifier
                                .padding(16.dp)
                                .width(150.dp)
                                .onClick {
                                    component.showImagePreview(imageUrl(it.url ?: ""))
                                },
                            model = imageUrl(it.url ?: ""),
                            contentScale = ContentScale.Inside
                        )
                    } else {
                        Row(
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    component.downloadReceipt(it)
                                },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_pdf),
                                tint = KarikaColors.Primary,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            KarikaText(
                                text = it.name ?: "",
                                fontWeight = FontWeight.Bold,
                                textSize = 12.sp,
                                color = KarikaColors.Primary
                            )
                        }
                    }
                }
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    text = comment.createdAt(),
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
                YSpacer16()
            }
        }
    }
}
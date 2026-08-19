package karika.distribucija.ba.ui.view.salesrep.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfCartResponseItem
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_arrow_up
import karikav2.composeapp.generated.resources.img_ab_post
import karikav2.composeapp.generated.resources.img_express_post
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOrderReviewView(component: SalesOrderReviewComponent) {
    val cart by component.cart.collectAsState()
    val shippingDefaults by component.shippingDefaults.collectAsState()
    val isPlacingOrder by component.isPlacingOrder.collectAsState()
    val items = cart?.items.orEmpty()
    val customer = component.customer

    // Per the cart response: grand_total is the pre-tax VPC total (post-discount), subtotal is
    // the pre-discount VPC total, and total_with_tax is the actual final total shown to the user.
    val vpcTotal = cart?.grandTotal ?: 0.0
    val subtotal = cart?.subtotal ?: 0.0
    val discountTotal = cart?.discountAmount ?: 0.0
    val pdvTotal = cart?.totalTax ?: 0.0
    val totalWithTax = cart?.totalWithTax ?: 0.0
    val karikaProvizija = cart?.fee ?: 0.0

    var deliveryExpanded by remember { mutableStateOf(false) }
    val contactName = remember { mutableStateOf("") }
    val contactEmail = remember { mutableStateOf("") }
    val contactPhone = remember { mutableStateOf("") }
    val city = remember { mutableStateOf("") }
    val address = remember { mutableStateOf("") }
    val postalCode = remember { mutableStateOf("") }
    val packageWidth = remember { mutableStateOf("") }
    val packageHeight = remember { mutableStateOf("") }
    val packageDepth = remember { mutableStateOf("") }
    val packageWeight = remember { mutableStateOf("") }
    val deliveryNote = remember { mutableStateOf("") }
    var selectedCarrierCode by remember { mutableStateOf("") }
    var shippingCost by remember { mutableStateOf<Pair<Double?, Double?>?>(null) }
    var defaultsApplied by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    var editingItem by remember { mutableStateOf<OnBehalfCartResponseItem?>(null) }

    LaunchedEffect(shippingDefaults) {
        val defaults = shippingDefaults
        if (defaults != null && !defaultsApplied) {
            contactName.value = defaults.contactName ?: ""
            contactEmail.value = defaults.email ?: ""
            contactPhone.value = defaults.telephone ?: ""
            city.value = defaults.city ?: ""
            address.value = defaults.street ?: ""
            postalCode.value = defaults.postcode ?: ""
            packageWidth.value = defaults.packageWidth ?: ""
            packageHeight.value = defaults.packageHeight ?: ""
            packageDepth.value = defaults.packageDepth ?: ""
            packageWeight.value = defaults.packageWeight ?: ""
            deliveryNote.value = defaults.note ?: ""
            selectedCarrierCode = defaults.shippingCompany ?: ""
            defaultsApplied = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ── Order info card ────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    KarikaText(
                        text = "Informacije o narudžbi",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )

                    HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoField(modifier = Modifier.weight(1f), label = "KUPAC", value = customer.company ?: "")
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "EMAIL",
                            value = customer.email?.takeIf { it.isNotBlank() } ?: "—"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = "PARTNERSTVO",
                                color = KarikaColors.Gray6,
                                textSize = 11.sp,
                                fontWeight = FontWeight.W500
                            )
                            Spacer(Modifier.height(4.dp))
                            PartnershipBadge(isActive = customer.isActive, status = customer.partnershipStatus)
                        }
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "STAVKI",
                            value = "${cart?.itemsCount ?: items.size}"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "UKUPNO VPC",
                            value = karikaPriceFormat(vpcTotal) + " KM"
                        )
                    }

                    HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "MEĐUZBIR",
                            value = karikaPriceFormat(subtotal) + " KM"
                        )
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "PDV (17%)",
                            value = karikaPriceFormat(pdvTotal) + " KM"
                        )
                    }

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "POPUST",
                            value = "-" + karikaPriceFormat(discountTotal) + " KM"
                        )
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "UKUPNO SA PDV",
                            value = karikaPriceFormat(totalWithTax) + " KM",
                            valueColor = KarikaColors.Blue,
                            valueSize = 18.sp
                        )
                    }

                    HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

                    Row(modifier = Modifier.fillMaxWidth()) {
                        InfoField(
                            modifier = Modifier.weight(1f),
                            label = "KARIKA PROVIZIJA",
                            value = karikaPriceFormat(karikaProvizija) + " KM"
                        )
                    }
                }
            }

            // ── Usluga dostave (foldable) ─────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.Yellow1)
                        .border(1.dp, KarikaColors.Yellow, RoundedCornerShape(16.dp)),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { deliveryExpanded = !deliveryExpanded }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            text = "Usluga dostave",
                            color = KarikaColors.Yellow2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                        Icon(
                            imageVector = vectorResource(
                                if (deliveryExpanded) Res.drawable.ic_arrow_up else Res.drawable.ic_arrow_down
                            ),
                            contentDescription = "",
                            tint = KarikaColors.Yellow2,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    if (deliveryExpanded) {
                        KarikaText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Adresa za utovar",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Kontakt osoba*",
                                value = contactName,
                                placeholder = "Kontakt osoba",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Email adresa*",
                                value = contactEmail,
                                placeholder = "Email adresa",
                                keyboardType = KeyboardType.Email,
                                imeAction = ImeAction.Next
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Telefon*",
                                value = contactPhone,
                                placeholder = "Telefon",
                                keyboardType = KeyboardType.Phone,
                                imeAction = ImeAction.Next
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Grad*",
                                value = city,
                                placeholder = "Grad",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Adresa*",
                                value = address,
                                placeholder = "Adresa",
                                keyboardType = KeyboardType.Text,
                                imeAction = ImeAction.Next
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Poštanski broj*",
                                value = postalCode,
                                placeholder = "Poštanski broj",
                                keyboardType = KeyboardType.Number,
                                allowedChars = KarikaConstants.numbers,
                                imeAction = ImeAction.Next
                            )
                        }

                        KarikaText(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            text = "Kalkulator dostave",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                        KarikaText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Unesite dimenzije paketa i izračunajte cijene za brzu dostavu, ukoliko odobrite paket u tom koraku ćete moći izabrati da li i koju opciju dostave želite",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )

                        Column(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Ukupna širina*",
                                value = packageWidth,
                                placeholder = "Ukupna širina",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                trailingIcons = {
                                    KarikaText(
                                        text = "cm",
                                        color = KarikaColors.Gray4,
                                        textSize = 16.sp,
                                        fontWeight = FontWeight.W400
                                    )
                                }
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Ukupna visina*",
                                value = packageHeight,
                                placeholder = "Ukupna visina",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                trailingIcons = {
                                    KarikaText(
                                        text = "cm",
                                        color = KarikaColors.Gray4,
                                        textSize = 16.sp,
                                        fontWeight = FontWeight.W400
                                    )
                                }
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Ukupna dubina*",
                                value = packageDepth,
                                placeholder = "Ukupna dubina",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                trailingIcons = {
                                    KarikaText(
                                        text = "cm",
                                        color = KarikaColors.Gray4,
                                        textSize = 16.sp,
                                        fontWeight = FontWeight.W400
                                    )
                                }
                            )
                            KarikaTextField1(
                                modifier = Modifier.fillMaxWidth(),
                                title = "Ukupna težina*",
                                value = packageWeight,
                                placeholder = "Ukupna težina",
                                keyboardType = KeyboardType.Number,
                                imeAction = ImeAction.Next,
                                trailingIcons = {
                                    KarikaText(
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

                        ShippingProviderRow(
                            image = Res.drawable.img_ab_post,
                            label = "A2B Express",
                            cost = shippingCost?.first,
                            selected = selectedCarrierCode == "A2B",
                            onSelect = { selectedCarrierCode = "A2B" }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = KarikaColors.Divider
                        )
                        ShippingProviderRow(
                            image = Res.drawable.img_express_post,
                            label = "EuroExpress",
                            cost = shippingCost?.second,
                            selected = selectedCarrierCode == "EURO_EXPRESS",
                            onSelect = { selectedCarrierCode = "EURO_EXPRESS" }
                        )
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = KarikaColors.Divider
                        )

                        KarikaText(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            text = "Napomena za dostavu",
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                        KarikaTextField2(
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .heightIn(min = 66.dp)
                                .fillMaxWidth(),
                            value = deliveryNote,
                            placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Next
                        )

                        PrimaryButtonFilled(
                            modifier = Modifier
                                .padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
                                .height(48.dp)
                                .fillMaxWidth(),
                            title = "Izračunaj cijenu"
                        ) {
                            shippingCost = component.calculateShipping(
                                packageWidth.value,
                                packageHeight.value,
                                packageDepth.value,
                                packageWeight.value
                            )
                        }
                    }
                }
            }

            // ── Specifikacija narudžbe ───────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            text = "Specifikacija narudžbe",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                    }

                    HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

                    SpecificationTable(
                        items = items,
                        onEditClick = { item -> editingItem = item }
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno VPC", value = karikaPriceFormat(vpcTotal) + " KM")
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno PDV 17%", value = karikaPriceFormat(pdvTotal) + " KM")
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        StatCard(modifier = Modifier.weight(1f), label = "Ukupno sa PDV", value = karikaPriceFormat(totalWithTax) + " KM")
                        StatCard(modifier = Modifier.weight(1f), label = "Karika provizija", value = karikaPriceFormat(karikaProvizija) + " KM")
                    }
                }
            }

            // ── Napomena ─────────────────────────────────────────────────────
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KarikaText(
                        text = "Napomena",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    LabeledTextField(
                        label = "",
                        value = note,
                        onValueChange = { note = it },
                        minLines = 3
                    )
                }
            }
        }

        // ── Sticky bottom actions ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(KarikaColors.White)
                .navigationBarsPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Potvrdi narudžbu
            val canConfirm = items.isNotEmpty() &&
                !isPlacingOrder &&
                customer.isActive &&
                customer.defaultShippingAddressId != null

            if (!customer.isActive) {
                KarikaText(
                    text = "Narudžbu možete kreirati samo za kupce sa aktivnim partnerstvom.",
                    color = KarikaColors.Red,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            } else if (customer.defaultShippingAddressId == null) {
                KarikaText(
                    text = "Kupac nema zadanu adresu dostave.",
                    color = KarikaColors.Red,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canConfirm) KarikaColors.Blue else KarikaColors.Gray9)
                    .clickable(
                        enabled = canConfirm,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) {
                        val shippingComplete = selectedCarrierCode.isNotBlank() &&
                            contactName.value.isNotBlank() &&
                            contactEmail.value.isNotBlank() &&
                            contactPhone.value.isNotBlank() &&
                            city.value.isNotBlank() &&
                            address.value.isNotBlank() &&
                            postalCode.value.isNotBlank() &&
                            packageWeight.value.isNotBlank() &&
                            packageWidth.value.isNotBlank() &&
                            packageHeight.value.isNotBlank() &&
                            packageDepth.value.isNotBlank()

                        val shippingForm = if (shippingComplete) {
                            VendorDeliveryServiceData(
                                name = contactName.value,
                                email = contactEmail.value,
                                telephone = contactPhone.value,
                                city = city.value,
                                street = address.value,
                                postcode = postalCode.value,
                                weight = packageWeight.value,
                                width = packageWidth.value,
                                height = packageHeight.value,
                                depth = packageDepth.value,
                                note = deliveryNote.value,
                                companyCode = selectedCarrierCode
                            )
                        } else null

                        component.confirmOrder(note, shippingForm)
                    },
                contentAlignment = Alignment.Center
            ) {
                if (isPlacingOrder) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        color = KarikaColors.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    KarikaText(
                        text = "Potvrdi narudžbu",
                        color = KarikaColors.White,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }

            // Nazad na korpu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                    .clickable(
                        enabled = !isPlacingOrder,
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() }
                    ) { component.goBack() },
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Nazad na korpu",
                    color = KarikaColors.Gray4,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
    }

    editingItem?.let { item ->
        EditCartItemModal(
            item = item,
            canDiscount = component.canCreateDiscountFor,
            onDismiss = { editingItem = null },
            onConfirm = { newQty, newDiscount ->
                component.updateItem(item, newQty, newDiscount)
                editingItem = null
            }
        )
    }
}

@Composable
private fun ShippingProviderRow(
    image: org.jetbrains.compose.resources.DrawableResource,
    label: String,
    cost: Double?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (selected) KarikaColors.Blue.copy(alpha = 0.08f) else androidx.compose.ui.graphics.Color.Transparent)
            .border(
                width = if (selected) 1.dp else 0.dp,
                color = if (selected) KarikaColors.Blue else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onSelect() }
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Image(
                modifier = Modifier.width(93.dp),
                contentScale = ContentScale.FillWidth,
                painter = painterResource(image),
                contentDescription = ""
            )
            KarikaText(
                text = label,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
        KarikaText(
            text = "Cijena dostave sa PDV: " + (cost?.let { karikaPriceFormat(it) + " KM" } ?: "—"),
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
private fun InfoField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    valueColor: androidx.compose.ui.graphics.Color = KarikaColors.Gray2,
    valueSize: androidx.compose.ui.unit.TextUnit = 14.sp
) {
    Column(modifier = modifier) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray6,
            textSize = 11.sp,
            fontWeight = FontWeight.W500
        )
        Spacer(Modifier.height(4.dp))
        KarikaText(
            text = value,
            color = valueColor,
            textSize = valueSize,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun PartnershipBadge(isActive: Boolean, status: String) {
    val badgeBg = if (isActive) KarikaColors.Green4 else KarikaColors.Blue3_10
    val badgeColor = if (isActive) KarikaColors.Green3 else KarikaColors.Blue
    val badgeLabel = when (status) {
        "active" -> "Aktivno"
        "pending" -> "Na čekanju"
        "revoked" -> "Opozvano"
        "rejected" -> "Odbijeno"
        else -> status
    }
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(badgeBg)
            .border(1.dp, badgeColor.copy(alpha = 0.25f), CircleShape)
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        KarikaText(
            text = badgeLabel,
            color = badgeColor,
            textSize = 11.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@Composable
private fun LabeledTextField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    Column(modifier = modifier) {
        if (label.isNotBlank()) {
            KarikaText(
                text = label,
                color = KarikaColors.Gray2,
                textSize = 13.sp,
                fontWeight = FontWeight.W600
            )
            Spacer(Modifier.height(6.dp))
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(KarikaColors.White)
                .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp)
        ) {
            if (value.isEmpty() && label.isNotBlank()) {
                KarikaText(
                    text = label.removeSuffix("*"),
                    color = KarikaColors.Gray7,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = karikaFonts(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500,
                    color = KarikaColors.Gray2
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = minLines == 1,
                minLines = minLines
            )
        }
    }
}

private val TABLE_COLUMN_WIDTHS = listOf(110.dp, 85.dp, 105.dp, 95.dp, 115.dp, 140.dp, 170.dp, 100.dp, 90.dp)

@Composable
private fun SpecificationTable(
    items: List<OnBehalfCartResponseItem>,
    onEditClick: (OnBehalfCartResponseItem) -> Unit
) {
    val headers = listOf(
        "ARTIKAL", "RABAT %", "CIJENA VPC", "KOLIČINA",
        "UKUPNO VPC", "UKUPNO SA PDV", "KARIKA PROVIZIJA %", "PROVIZIJA", "AKCIJE"
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(10.dp))
    ) {
        // ── Header row ───────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .height(IntrinsicSize.Min)
                .background(KarikaColors.Gray20)
        ) {
            headers.forEachIndexed { colIndex, header ->
                TableHeaderCell(header, width = TABLE_COLUMN_WIDTHS[colIndex])
                if (colIndex != headers.lastIndex) {
                    VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                }
            }
        }
        HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

        // ── Data rows ────────────────────────────────────────────────────
        items.forEachIndexed { rowIndex, item ->
            val discountMultiplier = 1.0 - (item.discountPercent ?: 0) / 100.0
            val discountedPrice = item.price * discountMultiplier
            val rowTotalVpc = discountedPrice * item.qty
            val rowTotalWithPdv = rowTotalVpc * 1.17
            val cells = listOf(
                item.name,
                "${item.discountPercent ?: 0}",
                karikaPriceFormat(discountedPrice) + " KM",
                "${item.qty} ${item.quantityUnit ?: "kom"}",
                karikaPriceFormat(rowTotalVpc) + " KM",
                karikaPriceFormat(rowTotalWithPdv) + " KM",
                "${item.commissionPercent.toInt()}",
                karikaPriceFormat(item.commission) + " KM"
            )

            Row(
                modifier = Modifier.height(IntrinsicSize.Min),
                verticalAlignment = Alignment.CenterVertically
            ) {
                cells.forEachIndexed { colIndex, text ->
                    TableCell(text, width = TABLE_COLUMN_WIDTHS[colIndex], bold = colIndex == 0)
                    VerticalDivider(color = KarikaColors.Gray9, thickness = 1.dp, modifier = Modifier.fillMaxHeight())
                }
                TableActionCell(width = TABLE_COLUMN_WIDTHS[cells.size]) { onEditClick(item) }
            }
            if (rowIndex != items.lastIndex) {
                HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)
            }
        }
    }
}

@Composable
private fun TableHeaderCell(text: String, width: Dp) {
    KarikaText(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 10.dp),
        color = KarikaColors.Gray6,
        textSize = 10.sp,
        fontWeight = FontWeight.W700,
        maxLines = 2
    )
}

@Composable
private fun TableCell(text: String, width: Dp, bold: Boolean = false) {
    KarikaText(
        text = text,
        modifier = Modifier.width(width).padding(horizontal = 8.dp, vertical = 10.dp),
        color = KarikaColors.Gray2,
        textSize = 13.sp,
        fontWeight = if (bold) FontWeight.W700 else FontWeight.W500,
        maxLines = 2
    )
}

@Composable
private fun TableActionCell(width: Dp, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .width(width)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 10.dp)
    ) {
        KarikaText(
            text = "Izmijeni",
            color = KarikaColors.Blue,
            textSize = 13.sp,
            fontWeight = FontWeight.W700
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditCartItemModal(
    item: OnBehalfCartResponseItem,
    canDiscount: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (newQty: Int, newDiscount: Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var discountText by remember {
        mutableStateOf(item.discountPercent?.takeIf { it > 0 }?.toString() ?: "")
    }
    var qtyText by remember { mutableStateOf("${item.qty}") }
    val qtyValid = (qtyText.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KarikaColors.Gray2, width = 60.dp)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                text = "Izmijeni stavku",
                modifier = Modifier.fillMaxWidth(),
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
            HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

            KarikaText(
                text = item.name,
                color = KarikaColors.Gray2,
                textSize = 15.sp,
                fontWeight = FontWeight.W700
            )

            if (canDiscount) {
                LabeledTextField(
                    label = "Rabat (%)",
                    value = discountText,
                    onValueChange = { v ->
                        val digits = v.filter { it.isDigit() }
                        discountText = when {
                            digits.isEmpty() -> ""
                            (digits.toIntOrNull() ?: 0) > 100 -> "100"
                            else -> digits
                        }
                    },
                    keyboardType = KeyboardType.Number
                )
            } else if ((item.discountPercent ?: 0) > 0) {
                KarikaText(
                    text = "Rabat: ${item.discountPercent}%",
                    color = KarikaColors.Gray6,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W500
                )
            }

            LabeledTextField(
                label = "Količina",
                value = qtyText,
                onValueChange = { v -> qtyText = v.filter { it.isDigit() } },
                keyboardType = KeyboardType.Number
            )

            HorizontalDivider(color = KarikaColors.Gray9, thickness = 1.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Odustani",
                        color = KarikaColors.Blue,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (qtyValid) KarikaColors.Blue else KarikaColors.Gray9)
                        .clickable(
                            enabled = qtyValid,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) {
                            onConfirm(qtyText.toIntOrNull() ?: item.qty, discountText.toIntOrNull() ?: 0)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Izmijeni",
                        color = KarikaColors.White,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun StatCard(modifier: Modifier = Modifier, label: String, value: String) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray20)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray6,
            textSize = 12.sp,
            fontWeight = FontWeight.W600
        )
        KarikaText(
            text = value,
            color = KarikaColors.Gray2,
            textSize = 15.sp,
            fontWeight = FontWeight.W700
        )
    }
}

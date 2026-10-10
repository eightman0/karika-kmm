package karika.distribucija.ba.ui.view.salesrep.cart

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.OnBehalfCartResponseItem
import karika.distribucija.ba.domain.model.VendorDeliveryServiceData
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KKeyValueRow
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderField
import karika.distribucija.ba.ui.view.distributer.orders.details.component.OrderModalButtons
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_truck
import karikav2.composeapp.generated.resources.img_ab_post
import karikav2.composeapp.generated.resources.img_express_post
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

// Amber of the "Usluga dostave" card, as on the supplier's order details
private val AmberCard = Color(0xFFFFFBEB)
private val AmberBorder = Color(0xFFFCD34D)
private val AmberLine = Color(0xFFFDE68A)
private val AmberTitle = Color(0xFF92400E)

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
    val note = remember { mutableStateOf("") }
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
            .background(KarikaUiColors.Page)
    ) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 20.dp)
        ) {
            // ── Order info card ────────────────────────────────────────────────
            item { SectionTitle("Informacije o narudžbi") }
            item {
                KCard(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KInitials(
                            name = customer.company ?: customer.fullName,
                            size = 40.dp,
                            shape = RoundedCornerShape(10.dp),
                            background = VendorAccentSoft,
                            color = VendorAccent,
                            textSize = 13.sp
                        )
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = customer.company?.takeIf { it.isNotBlank() } ?: customer.fullName,
                                color = KarikaUiColors.Ink,
                                textSize = 15.sp,
                                lineHeight = 19.sp,
                                fontWeight = FontWeight.W700,
                                maxLines = 2
                            )
                            KarikaText(
                                modifier = Modifier.padding(top = 2.dp),
                                text = customer.email?.takeIf { it.isNotBlank() } ?: "—",
                                color = KarikaUiColors.Muted,
                                textSize = 12.5.sp,
                                lineHeight = 16.sp,
                                maxLines = 1
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        PartnershipBadge(isActive = customer.isActive, status = customer.partnershipStatus)
                    }
                    KDivider()
                    KKeyValueRow(label = "Stavki", value = "${cart?.itemsCount ?: items.size}")
                    KDivider()
                    KKeyValueRow(label = "Ukupno VPC", value = karikaPriceFormat(vpcTotal) + " KM")
                }
            }

            // ── Specifikacija narudžbe ───────────────────────────────────────────
            item { SectionTitle("Specifikacija narudžbe") }
            itemsIndexed(items, key = { _, item -> item.itemId }) { index, item ->
                SpecificationItem(
                    modifier = Modifier.padding(top = if (index > 0) 10.dp else 0.dp),
                    item = item,
                    onEditClick = { editingItem = item }
                )
            }

            // ── Summary ─────────────────────────────────────────────────────────
            item { SectionTitle("Pregled") }
            item {
                KCard(modifier = Modifier.fillMaxWidth()) {
                    KKeyValueRow(label = "Međuzbir", value = karikaPriceFormat(subtotal) + " KM")
                    KDivider()
                    KKeyValueRow(
                        label = "Popust",
                        value = "-" + karikaPriceFormat(discountTotal) + " KM",
                        valueColor = KarikaUiColors.Red
                    )
                    KDivider()
                    KKeyValueRow(label = "Ukupno VPC", value = karikaPriceFormat(vpcTotal) + " KM")
                    KDivider()
                    KKeyValueRow(label = "PDV (17%)", value = karikaPriceFormat(pdvTotal) + " KM")
                    KDivider()
                    KKeyValueRow(label = "Karika provizija", value = karikaPriceFormat(karikaProvizija) + " KM")
                    KDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(VendorAccentSoft.copy(alpha = 0.5f))
                            .padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier.weight(1f),
                            text = "Ukupno sa PDV",
                            color = KarikaUiColors.Ink,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W700
                        )
                        KarikaText(
                            text = karikaPriceFormat(totalWithTax) + " KM",
                            color = VendorAccent,
                            textSize = 18.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                    }
                }
            }

            // ── Usluga dostave (foldable) ─────────────────────────────────────
            item {
                val shape = RoundedCornerShape(16.dp)
                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth()
                        .clip(shape)
                        .background(AmberCard)
                        .border(1.dp, AmberBorder, shape)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { deliveryExpanded = !deliveryExpanded }
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KarikaUiColors.AmberSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            KIcon(icon = vectorResource(Res.drawable.ic_k_truck), tint = KarikaUiColors.Amber, size = 20.dp)
                        }
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            KarikaText(
                                text = "Usluga dostave",
                                color = AmberTitle,
                                textSize = 14.5.sp,
                                fontWeight = FontWeight.W700
                            )
                            KarikaText(
                                modifier = Modifier.padding(top = 2.dp),
                                text = if (deliveryExpanded) "Unesite adresu za utovar robe" else "Karika preuzima i dostavlja robu kupcu",
                                color = KarikaUiColors.Amber,
                                textSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                        KIcon(
                            modifier = Modifier.rotate(if (deliveryExpanded) 180f else 0f),
                            icon = vectorResource(Res.drawable.ic_k_chevron_down),
                            tint = KarikaUiColors.Amber,
                            size = 18.dp
                        )
                    }

                    if (deliveryExpanded) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(KarikaColors.White)
                        ) {
                            Box(Modifier.fillMaxWidth().height(1.dp).background(AmberLine))
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 16.dp),
                                verticalArrangement = Arrangement.spacedBy(14.dp)
                            ) {
                                ShippingHeading("Adresa za utovar")
                                OrderField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = contactName,
                                    label = "Kontakt osoba",
                                    required = true,
                                    placeholder = "Kontakt osoba"
                                )
                                OrderField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = contactEmail,
                                    label = "Email adresa",
                                    required = true,
                                    placeholder = "Email adresa",
                                    keyboardType = KeyboardType.Email
                                )
                                OrderField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = contactPhone,
                                    label = "Telefon",
                                    required = true,
                                    placeholder = "Telefon",
                                    keyboardType = KeyboardType.Phone
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = city,
                                        label = "Grad",
                                        required = true,
                                        placeholder = "Grad"
                                    )
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = postalCode,
                                        label = "Poštanski broj",
                                        required = true,
                                        placeholder = "Poštanski broj",
                                        keyboardType = KeyboardType.Number,
                                        allowedChars = KarikaConstants.numbers
                                    )
                                }
                                OrderField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = address,
                                    label = "Adresa",
                                    required = true,
                                    placeholder = "Adresa"
                                )

                                Column(modifier = Modifier.padding(top = 6.dp)) {
                                    ShippingHeading("Kalkulator dostave")
                                    KarikaText(
                                        modifier = Modifier.padding(top = 4.dp),
                                        text = "Unesite dimenzije paketa i izračunajte cijene za brzu dostavu, ukoliko odobrite paket u tom koraku ćete moći izabrati da li i koju opciju dostave želite",
                                        color = KarikaUiColors.Muted,
                                        textSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = packageWidth,
                                        label = "Ukupna širina",
                                        required = true,
                                        placeholder = "Ukupna širina",
                                        keyboardType = KeyboardType.Number,
                                        trailingText = "cm"
                                    )
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = packageHeight,
                                        label = "Ukupna visina",
                                        required = true,
                                        placeholder = "Ukupna visina",
                                        keyboardType = KeyboardType.Number,
                                        trailingText = "cm"
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = packageDepth,
                                        label = "Ukupna dubina",
                                        required = true,
                                        placeholder = "Ukupna dubina",
                                        keyboardType = KeyboardType.Number,
                                        trailingText = "cm"
                                    )
                                    OrderField(
                                        modifier = Modifier.weight(1f),
                                        value = packageWeight,
                                        label = "Ukupna težina",
                                        required = true,
                                        placeholder = "Ukupna težina",
                                        keyboardType = KeyboardType.Number,
                                        trailingText = "kg"
                                    )
                                }

                                ShippingProviderRow(
                                    image = Res.drawable.img_ab_post,
                                    label = "A2B Express",
                                    cost = shippingCost?.first,
                                    selected = selectedCarrierCode == "A2B",
                                    onSelect = { selectedCarrierCode = "A2B" }
                                )
                                ShippingProviderRow(
                                    image = Res.drawable.img_express_post,
                                    label = "EuroExpress",
                                    cost = shippingCost?.second,
                                    selected = selectedCarrierCode == "EURO_EXPRESS",
                                    onSelect = { selectedCarrierCode = "EURO_EXPRESS" }
                                )

                                OrderField(
                                    modifier = Modifier.fillMaxWidth(),
                                    value = deliveryNote,
                                    label = "Napomena za dostavu",
                                    placeholder = "Napiši svoju napomenu za dostavu ovdje...",
                                    singleLine = false,
                                    minHeight = 66.dp
                                )

                                KPrimaryButton(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = "Izračunaj cijenu",
                                    background = VendorAccent,
                                    height = 50.dp
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
                }
            }

            // ── Napomena ─────────────────────────────────────────────────────
            item { SectionTitle("Napomena") }
            item {
                OrderField(
                    modifier = Modifier.fillMaxWidth(),
                    value = note,
                    placeholder = "Napomena (opciono)",
                    singleLine = false,
                    minHeight = 88.dp
                )
            }
        }

        // ── Sticky bottom actions ───────────────────────────────────────────────
        KBottomPanel(modifier = Modifier.navigationBarsPadding()) {
            val canConfirm = items.isNotEmpty() &&
                !isPlacingOrder &&
                customer.isActive &&
                customer.defaultShippingAddressId != null

            if (!customer.isActive) {
                WarningText("Narudžbu možete kreirati samo za kupce sa aktivnim partnerstvom.")
            } else if (customer.defaultShippingAddressId == null) {
                WarningText("Kupac nema zadanu adresu dostave.")
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = "Ukupno sa PDV",
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W500
                )
                KarikaText(
                    text = karikaPriceFormat(totalWithTax) + " KM",
                    color = KarikaUiColors.Ink,
                    textSize = 22.sp,
                    lineHeight = 26.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }

            // Potvrdi narudžbu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (canConfirm || isPlacingOrder) VendorAccent else VendorAccent.copy(alpha = 0.4f))
                    .clickable(enabled = canConfirm) {
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

                        component.confirmOrder(note.value, shippingForm)
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
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        maxLines = 1
                    )
                }
            }

            // Nazad na korpu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(enabled = !isPlacingOrder) { component.goBack() },
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = "Nazad na korpu",
                    color = VendorAccent,
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
private fun SectionTitle(title: String) {
    KSectionTitle(
        modifier = Modifier.padding(top = 18.dp, bottom = 12.dp),
        title = title
    )
}

@Composable
private fun ShippingHeading(text: String) {
    KarikaText(
        text = text,
        color = KarikaUiColors.Ink,
        textSize = 14.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun WarningText(text: String) {
    KarikaText(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(KarikaUiColors.RedSoft)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        text = text,
        color = KarikaUiColors.Red,
        textSize = 12.5.sp,
        lineHeight = 17.sp,
        fontWeight = FontWeight.W500
    )
}

/** Courier option: blue border and a blue check when selected. */
@Composable
private fun ShippingProviderRow(
    image: DrawableResource,
    label: String,
    cost: Double?,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(KarikaColors.White)
            .border(
                width = if (selected) 1.5.dp else 1.dp,
                color = if (selected) VendorAccent else KarikaUiColors.Line,
                shape = shape
            )
            .clickable { onSelect() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(CircleShape)
                .background(if (selected) VendorAccent else KarikaColors.White)
                .border(
                    width = 1.5.dp,
                    color = if (selected) VendorAccent else KarikaUiColors.Border,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_check),
                    tint = KarikaColors.White,
                    size = 14.dp
                )
            }
        }
        Spacer(Modifier.width(12.dp))
        Image(
            modifier = Modifier.width(64.dp),
            contentScale = ContentScale.FillWidth,
            painter = painterResource(image),
            contentDescription = ""
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = label,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            KarikaText(
                modifier = Modifier.padding(top = 2.dp),
                text = "Cijena dostave sa PDV: " + (cost?.let { karikaPriceFormat(it) + " KM" } ?: "—"),
                color = KarikaUiColors.Muted,
                textSize = 12.5.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun PartnershipBadge(isActive: Boolean, status: String) {
    val badgeLabel = when (status) {
        "active" -> "Aktivno"
        "pending" -> "Na čekanju"
        "revoked" -> "Opozvano"
        "rejected" -> "Odbijeno"
        else -> status
    }
    val (background, color) = when {
        isActive -> KarikaUiColors.GreenSoft to KarikaUiColors.Green
        status == "pending" -> KarikaUiColors.AmberSoft to KarikaUiColors.Amber
        status == "rejected" -> KarikaUiColors.RedSoft to KarikaUiColors.Red
        status == "revoked" -> KarikaUiColors.Field to KarikaUiColors.Muted
        else -> VendorAccentSoft to VendorAccent
    }
    KPill(text = badgeLabel, background = background, color = color, textSize = 11.sp)
}

/**
 * One line of the order specification as a card: name, rabat and commission labels, the prices
 * and quantity as key/value rows, and "Izmijeni".
 */
@Composable
private fun SpecificationItem(
    modifier: Modifier = Modifier,
    item: OnBehalfCartResponseItem,
    onEditClick: () -> Unit
) {
    val discountMultiplier = 1.0 - (item.discountPercent ?: 0) / 100.0
    val discountedPrice = item.price * discountMultiplier
    val rowTotalVpc = discountedPrice * item.qty
    val rowTotalWithPdv = rowTotalVpc * 1.17

    KCard(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 8.dp, top = 12.dp, bottom = 10.dp),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = item.name,
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    lineHeight = 18.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 2
                )
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    KPill(
                        text = "Rabat ${item.discountPercent ?: 0}%",
                        background = VendorAccentSoft,
                        color = VendorAccent,
                        textSize = 11.sp
                    )
                    KPill(
                        text = "Provizija ${item.commissionPercent.toInt()}%",
                        background = KarikaUiColors.Field,
                        color = KarikaUiColors.Muted,
                        textSize = 11.sp
                    )
                }
            }
            KarikaText(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onEditClick() }
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                text = "Izmijeni",
                color = VendorAccent,
                textSize = 13.sp,
                fontWeight = FontWeight.W700
            )
        }
        KDivider()
        KKeyValueRow(label = "Cijena VPC", value = karikaPriceFormat(discountedPrice) + " KM")
        KKeyValueRow(label = "Količina", value = "${item.qty} ${item.quantityUnit ?: "kom"}")
        KKeyValueRow(label = "Ukupno VPC", value = karikaPriceFormat(rowTotalVpc) + " KM")
        KKeyValueRow(label = "Ukupno sa PDV", value = karikaPriceFormat(rowTotalWithPdv) + " KM")
        KKeyValueRow(label = "Provizija", value = karikaPriceFormat(item.commission) + " KM")
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
    val discountText = remember {
        mutableStateOf(item.discountPercent?.takeIf { it > 0 }?.toString() ?: "")
    }
    val qtyText = remember { mutableStateOf("${item.qty}") }
    val qtyValid = (qtyText.value.toIntOrNull() ?: 0) > 0

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = KarikaUiColors.Border, width = 40.dp)
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 16.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                KarikaText(
                    text = "Izmijeni stavku",
                    color = KarikaUiColors.Ink,
                    textSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    text = item.name,
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    lineHeight = 19.sp
                )
            }

            if (canDiscount) {
                OrderField(
                    modifier = Modifier.fillMaxWidth(),
                    value = discountText,
                    label = "Rabat (%)",
                    placeholder = "Rabat (%)",
                    keyboardType = KeyboardType.Number,
                    trailingText = "%",
                    allowedChars = KarikaConstants.numbers,
                    onValueChange = { v ->
                        if ((v.toIntOrNull() ?: 0) > 100) discountText.value = "100"
                    }
                )
            } else if ((item.discountPercent ?: 0) > 0) {
                KPill(
                    text = "Rabat: ${item.discountPercent}%",
                    background = VendorAccentSoft,
                    color = VendorAccent
                )
            }

            OrderField(
                modifier = Modifier.fillMaxWidth(),
                value = qtyText,
                label = "Količina",
                placeholder = "Količina",
                keyboardType = KeyboardType.Number,
                allowedChars = KarikaConstants.numbers
            )

            OrderModalButtons(
                modifier = Modifier.padding(top = 6.dp),
                primaryText = "Izmijeni",
                primaryEnabled = qtyValid,
                onPrimary = {
                    onConfirm(qtyText.value.toIntOrNull() ?: item.qty, discountText.value.toIntOrNull() ?: 0)
                },
                onSecondary = onDismiss
            )
        }
    }
}

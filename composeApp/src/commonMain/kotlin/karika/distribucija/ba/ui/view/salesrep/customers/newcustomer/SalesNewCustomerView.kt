package karika.distribucija.ba.ui.view.salesrep.customers.newcustomer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_check_circle_filled
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_info
import karikav2.composeapp.generated.resources.ic_person
import karikav2.composeapp.generated.resources.ic_phone
import karikav2.composeapp.generated.resources.ic_storefront
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesNewCustomerView(component: SalesNewCustomerComponent) {
    val company by component.company.collectAsState()
    val idNumber by component.idNumber.collectAsState()
    val vatNumber by component.vatNumber.collectAsState()
    val street by component.street.collectAsState()
    val postcode by component.postcode.collectAsState()
    val entity by component.entity.collectAsState()
    val cantonOptions by component.cantonOptions.collectAsState()
    val canton by component.canton.collectAsState()
    val cityOptions by component.cityOptions.collectAsState()
    val city by component.city.collectAsState()
    val storeSize by component.storeSize.collectAsState()
    val storeType by component.storeType.collectAsState()
    val employeeCount by component.employeeCount.collectAsState()
    val firstname by component.firstname.collectAsState()
    val lastname by component.lastname.collectAsState()
    val phone by component.phone.collectAsState()
    val email by component.email.collectAsState()
    val isSaving by component.isSaving.collectAsState()
    val showInviteDialog by component.showInviteDialog.collectAsState()

    if (showInviteDialog) {
        AlertDialog(
            onDismissRequest = { component.dismissInviteDialog() },
            containerColor = KarikaColors.White,
            title = {
                KarikaText(
                    text = "Kupac već postoji",
                    color = KarikaColors.Gray2,
                    fontWeight = FontWeight.W700,
                    textSize = 18.sp
                )
            },
            text = {
                KarikaText(
                    text = "Kupac sa ovim email-om već postoji. Želiš li ga pozvati kao partnera?",
                    color = KarikaColors.Gray2,
                    textSize = 14.sp
                )
            },
            dismissButton = {
                TextButton(onClick = { component.dismissInviteDialog() }) {
                    KarikaText(text = "Odustani", color = KarikaColors.Blue, textSize = 14.sp)
                }
            },
            confirmButton = {
                TextButton(onClick = { component.openInviteCustomer() }) {
                    KarikaText(
                        text = "Pozovi",
                        color = KarikaColors.Blue,
                        fontWeight = FontWeight.W700,
                        textSize = 14.sp
                    )
                }
            }
        )
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    var activeSheet by remember { mutableStateOf<String?>(null) }

    fun closeSheet() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { activeSheet = null }
    }

    // Derived flags
    val isFBiH = entity == "Federacija"
    val isRS = entity == "Republika Srpska"
    val isBrcko = entity == "Distrikt Brčko"
    val showCantonPicker = (isFBiH || isRS) && cantonOptions.isNotEmpty()
    val showCityPicker = isFBiH && canton != null && cityOptions.isNotEmpty()
    // Brčko: city is pre-filled "Brčko Grad", shown as read-only chip
    val brckoCity = if (isBrcko) city else null

    KarikaScaffold(
        modifier = Modifier.fillMaxSize(),
        component = component,
        containerColor = KarikaColors.Gray20,
        bottomBar = {
            // ── Footer ─────────────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, KarikaColors.Blue, RoundedCornerShape(18.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.goBack() },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Odustani",
                        color = KarikaColors.Blue,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isSaving) KarikaColors.Gray9 else KarikaColors.Blue)
                        .clickable(
                            enabled = !isSaving,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.save() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = KarikaColors.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        KarikaText(
                            text = "Sačuvaj kupca",
                            color = KarikaColors.White,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .windowInsetsPadding(
                    WindowInsets.ime
                        .union(WindowInsets.navigationBars)
                        .only(WindowInsetsSides.Bottom)
                )
                .fillMaxSize()
        ) {
            // ── Info banner ────────────────────────────────────────────────────
            item {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(KarikaColors.Blue.copy(alpha = 0.08f))
                        .border(
                            1.dp,
                            KarikaColors.Blue.copy(alpha = 0.2f),
                            RoundedCornerShape(16.dp)
                        )
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_info),
                        contentDescription = "",
                        tint = KarikaColors.Blue,
                        modifier = Modifier.size(20.dp)
                    )
                    KarikaText(
                        text = "Kupac dobija email za postavljanje lozinke i može se samostalno prijaviti na Kariku. Partnerstvo i dodjela kreiraju se automatski, a kupac se odmah pojavljuje na vašoj listi.",
                        color = KarikaColors.Gray2,
                        textSize = 13.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            }

            // ── Section 1: Informacije o pravnom licu ──────────────────────────
            item {
                FormSection(
                    icon = Res.drawable.ic_storefront,
                    title = "Informacije o pravnom licu"
                ) {

                    // Naziv pravnog lica (full width)
                    FormField(label = "Naziv pravnog lica*") {
                        FormTextField(
                            value = company,
                            placeholder = "Naziv pravnog lica",
                            onValueChange = { component.setCompany(it) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // ID broj + PDV broj
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "ID broj*") {
                                FormTextField(
                                    value = idNumber,
                                    placeholder = "ID broj",
                                    keyboardType = KeyboardType.Number,
                                    onValueChange = { component.setIdNumber(it) }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "PDV broj") {
                                FormTextField(
                                    value = vatNumber,
                                    placeholder = "PDV broj",
                                    keyboardType = KeyboardType.Number,
                                    onValueChange = { component.setVatNumber(it) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Adresa (full width)
                    FormField(label = "Adresa i broj ulice*") {
                        FormTextField(
                            value = street,
                            placeholder = "Adresa i broj ulice",
                            onValueChange = { component.setStreet(it) }
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    // Poštanski broj + Entitet
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Poštanski broj*") {
                                FormTextField(
                                    value = postcode,
                                    placeholder = "Poštanski broj",
                                    keyboardType = KeyboardType.Number,
                                    onValueChange = { component.setPostcode(it) }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Entitet*") {
                                FormPicker(
                                    value = entity,
                                    placeholder = "Odaberite entitet",
                                    onClick = { activeSheet = "entity" }
                                )
                            }
                        }
                    }

                    // Kanton (FBiH) or Općina (RS) — appears after entity is selected
                    if (showCantonPicker) {
                        Spacer(Modifier.height(16.dp))
                        FormField(label = if (isFBiH) "Kanton*" else "Općina*") {
                            FormPicker(
                                value = canton,
                                placeholder = if (isFBiH) "Odaberite kanton" else "Odaberite općinu",
                                onClick = { activeSheet = "canton" }
                            )
                        }
                    }

                    // Grad — appears after kanton is selected (FBiH only)
                    if (showCityPicker) {
                        Spacer(Modifier.height(16.dp))
                        FormField(label = "Grad*") {
                            FormPicker(
                                value = city,
                                placeholder = "Odaberite grad",
                                onClick = { activeSheet = "city" }
                            )
                        }
                    }

                    // Brčko: show pre-filled city as read-only
                    if (isBrcko && brckoCity != null) {
                        Spacer(Modifier.height(16.dp))
                        FormField(label = "Grad") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaColors.Gray20)
                                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KarikaText(
                                    text = brckoCity,
                                    color = KarikaColors.Gray2,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W500
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Veličina + Tip objekta
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Veličina objekta*") {
                                FormPicker(
                                    value = storeSize,
                                    placeholder = "Veličina objekta",
                                    onClick = { activeSheet = "storeSize" }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Tip objekta*") {
                                FormPicker(
                                    value = storeType,
                                    placeholder = "Tip objekta",
                                    onClick = { activeSheet = "storeType" }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Broj zaposlenih (full width)
                    FormField(label = "Broj zaposlenih") {
                        FormTextField(
                            value = employeeCount,
                            placeholder = "Broj zaposlenih",
                            keyboardType = KeyboardType.Number,
                            onValueChange = { component.setEmployeeCount(it) }
                        )
                    }
                }
            }

            // ── Section 2: Kontakt osoba ───────────────────────────────────────
            item {
                FormSection(icon = Res.drawable.ic_person, title = "Kontakt osoba") {

                    // Ime + Prezime
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Ime*") {
                                FormTextField(
                                    value = firstname,
                                    placeholder = "Ime",
                                    onValueChange = { component.setFirstname(it) }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Prezime*") {
                                FormTextField(
                                    value = lastname,
                                    placeholder = "Prezime",
                                    onValueChange = { component.setLastname(it) }
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Telefon + Email
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Broj telefona*") {
                                FormTextField(
                                    value = phone,
                                    placeholder = "Broj telefona",
                                    keyboardType = KeyboardType.Phone,
                                    leadingIcon = Res.drawable.ic_phone,
                                    onValueChange = { component.setPhone(it) }
                                )
                            }
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            FormField(label = "Email adresa*") {
                                FormTextField(
                                    value = email,
                                    placeholder = "Email adresa",
                                    keyboardType = KeyboardType.Email,
                                    leadingIcon = Res.drawable.ic_email,
                                    onValueChange = { component.setEmail(it) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Bottom sheet pickers ───────────────────────────────────────────────────
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = KarikaColors.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(bottom = 16.dp)
            ) {
                when (activeSheet) {
                    "entity" -> SimplePickerSheet(
                        title = "Entitet",
                        options = component.entityOptions,
                        selected = entity,
                        onSelect = {
                            component.setEntity(it)
                            closeSheet()
                        }
                    )

                    "canton" -> SimplePickerSheet(
                        title = if (isFBiH) "Kanton" else "Općina",
                        options = cantonOptions,
                        selected = canton,
                        onSelect = {
                            component.setCanton(it)
                            closeSheet()
                        }
                    )

                    "city" -> SimplePickerSheet(
                        title = "Grad",
                        options = cityOptions,
                        selected = city,
                        onSelect = {
                            component.setCity(it)
                            closeSheet()
                        }
                    )

                    "storeSize" -> SimplePickerSheet(
                        title = "Veličina objekta",
                        options = component.storeSizeOptions,
                        selected = storeSize,
                        onSelect = {
                            component.setStoreSize(it)
                            closeSheet()
                        }
                    )

                    "storeType" -> SimplePickerSheet(
                        title = "Tip objekta",
                        options = component.storeTypeOptions,
                        selected = storeType,
                        onSelect = {
                            component.setStoreType(it)
                            closeSheet()
                        }
                    )
                }
            }
        }
    }
}

// ── Section wrapper ────────────────────────────────────────────────────────────

@Composable
private fun FormSection(
    icon: DrawableResource,
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(KarikaColors.White)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KarikaColors.Blue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(icon),
                    contentDescription = "",
                    tint = KarikaColors.Blue,
                    modifier = Modifier.size(20.dp)
                )
            }
            KarikaText(
                text = title,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
        }
        Spacer(Modifier.height(20.dp))
        content()
    }
}

// ── Form field label wrapper ───────────────────────────────────────────────────

@Composable
private fun FormField(label: String, content: @Composable () -> Unit) {
    Column {
        KarikaText(
            text = label,
            color = KarikaColors.Gray6,
            textSize = 12.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(5.dp))
        content()
    }
}

// ── Text input ─────────────────────────────────────────────────────────────────

@Composable
private fun FormTextField(
    value: String,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    leadingIcon: DrawableResource? = null,
    onValueChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray20)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = vectorResource(leadingIcon),
                contentDescription = "",
                tint = KarikaColors.Gray6,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                KarikaText(
                    text = placeholder,
                    color = KarikaColors.Gray7,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = TextStyle(
                    color = KarikaColors.Gray2,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.W500
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

// ── Picker trigger ─────────────────────────────────────────────────────────────

@Composable
private fun FormPicker(value: String?, placeholder: String, onClick: () -> Unit) {
    val focusManager = LocalFocusManager.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaColors.Gray20)
            .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(12.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {
                    focusManager.clearFocus()
                    onClick()
                }
            )
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KarikaText(
            text = value ?: placeholder,
            color = if (value != null) KarikaColors.Gray2 else KarikaColors.Gray7,
            textSize = 14.sp,
            fontWeight = if (value != null) FontWeight.W500 else FontWeight.W400,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = vectorResource(Res.drawable.ic_arrow_down),
            contentDescription = "",
            tint = KarikaColors.Gray6,
            modifier = Modifier.size(18.dp)
        )
    }
}

// ── Bottom sheet list ──────────────────────────────────────────────────────────

@Composable
private fun SimplePickerSheet(
    title: String,
    options: List<String>,
    selected: String?,
    onSelect: (String) -> Unit
) {
    KarikaText(
        text = title,
        color = KarikaColors.Gray2,
        textSize = 16.sp,
        fontWeight = FontWeight.W700,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
    )
    HorizontalDivider(color = KarikaColors.Gray9)
    options.forEachIndexed { index, option ->
        val isSelected = selected == option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) { onSelect(option) }
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                text = option,
                color = if (isSelected) KarikaColors.Blue else KarikaColors.Gray2,
                textSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500
            )
            if (isSelected) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_check_circle_filled),
                    contentDescription = "",
                    tint = KarikaColors.Blue,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        if (index < options.lastIndex) {
            HorizontalDivider(
                color = KarikaColors.Gray9,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
        }
    }
    YSpacer8()
}

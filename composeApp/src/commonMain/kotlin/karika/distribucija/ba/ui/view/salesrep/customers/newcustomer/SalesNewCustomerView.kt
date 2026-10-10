package karika.distribucija.ba.ui.view.salesrep.customers.newcustomer

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KConfirmDialog
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_info
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_phone
import kotlinx.coroutines.launch
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
        KConfirmDialog(
            title = "Kupac već postoji",
            message = "Kupac sa ovim email-om već postoji. Želiš li ga pozvati kao partnera?",
            icon = vectorResource(Res.drawable.ic_k_mail),
            confirmText = "Pozovi",
            dismissText = "Odustani",
            onConfirm = { component.openInviteCustomer() },
            onDismiss = { component.dismissInviteDialog() },
            iconBackground = VendorAccentSoft,
            iconTint = VendorAccent,
            confirmColor = VendorAccent
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
    // Brčko: city is pre-filled "Brčko Grad", shown read-only
    val brckoCity = if (isBrcko) city else null

    KarikaScaffold(
        modifier = Modifier.fillMaxSize(),
        component = component,
        containerColor = KarikaUiColors.Page,
        bottomBar = {
            KBottomPanel {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KSecondaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Odustani",
                        onClick = { component.goBack() }
                    )
                    KPrimaryButton(
                        modifier = Modifier.weight(1.6f),
                        text = "Sačuvaj kupca",
                        background = VendorAccent,
                        enabled = !isSaving,
                        onClick = { component.save() }
                    )
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
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp)
        ) {
            // ── Info banner ────────────────────────────────────────────────────
            item {
                InfoNote(
                    "Kupac dobija email za postavljanje lozinke i može se samostalno prijaviti na Kariku. " +
                        "Partnerstvo i dodjela kreiraju se automatski, a kupac se odmah pojavljuje na vašoj listi."
                )
            }

            // ── Section 1: Informacije o pravnom licu ──────────────────────────
            item {
                Section("Informacije o pravnom licu") {
                    FormTextField(
                        label = "Naziv pravnog lica",
                        value = company,
                        placeholder = "Naziv pravnog lica",
                        onValueChange = { component.setCompany(it) }
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            label = "ID broj",
                            value = idNumber,
                            placeholder = "ID broj",
                            keyboardType = KeyboardType.Number,
                            onValueChange = { component.setIdNumber(it) }
                        )
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            label = "PDV broj",
                            value = vatNumber,
                            placeholder = "PDV broj",
                            required = false,
                            keyboardType = KeyboardType.Number,
                            onValueChange = { component.setVatNumber(it) }
                        )
                    }
                    FormTextField(
                        label = "Adresa i broj ulice",
                        value = street,
                        placeholder = "Adresa i broj ulice",
                        onValueChange = { component.setStreet(it) }
                    )
                    FormTextField(
                        label = "Poštanski broj",
                        value = postcode,
                        placeholder = "Poštanski broj",
                        keyboardType = KeyboardType.Number,
                        onValueChange = { component.setPostcode(it) }
                    )
                    DropdownField(
                        label = "Entitet",
                        value = entity,
                        placeholder = "Odaberite entitet",
                        onClick = { activeSheet = "entity" }
                    )

                    // Kanton (FBiH) or Općina (RS) — appears after entity is selected
                    if (showCantonPicker) {
                        DropdownField(
                            label = if (isFBiH) "Kanton" else "Općina",
                            value = canton,
                            placeholder = if (isFBiH) "Odaberite kanton" else "Odaberite općinu",
                            onClick = { activeSheet = "canton" }
                        )
                    }

                    // Grad — appears after kanton is selected (FBiH only)
                    if (showCityPicker) {
                        DropdownField(
                            label = "Grad",
                            value = city,
                            placeholder = "Odaberite grad",
                            onClick = { activeSheet = "city" }
                        )
                    }

                    // Brčko: the pre-filled city, read-only
                    if (isBrcko && brckoCity != null) {
                        Column {
                            KFieldLabel(text = "Grad")
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(KarikaUiColors.Field)
                                    .padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KarikaText(
                                    modifier = Modifier.weight(1f),
                                    text = brckoCity,
                                    color = KarikaUiColors.Ink,
                                    textSize = 15.sp,
                                    maxLines = 1
                                )
                                KIcon(
                                    icon = vectorResource(Res.drawable.ic_k_lock),
                                    tint = KarikaUiColors.Subtle,
                                    size = 16.dp
                                )
                            }
                        }
                    }

                    DropdownField(
                        label = "Veličina objekta",
                        value = storeSize,
                        placeholder = "Odaberite veličinu objekta",
                        onClick = { activeSheet = "storeSize" }
                    )
                    DropdownField(
                        label = "Tip objekta",
                        value = storeType,
                        placeholder = "Odaberite tip objekta",
                        onClick = { activeSheet = "storeType" }
                    )
                    FormTextField(
                        label = "Broj zaposlenih",
                        value = employeeCount,
                        placeholder = "Broj zaposlenih",
                        required = false,
                        keyboardType = KeyboardType.Number,
                        onValueChange = { component.setEmployeeCount(it) }
                    )
                }
            }

            // ── Section 2: Kontakt osoba ───────────────────────────────────────
            item {
                Section("Kontakt osoba") {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Ime",
                            value = firstname,
                            placeholder = "Ime",
                            onValueChange = { component.setFirstname(it) }
                        )
                        FormTextField(
                            modifier = Modifier.weight(1f),
                            label = "Prezime",
                            value = lastname,
                            placeholder = "Prezime",
                            onValueChange = { component.setLastname(it) }
                        )
                    }
                    FormTextField(
                        label = "Broj telefona",
                        value = phone,
                        placeholder = "Broj telefona",
                        keyboardType = KeyboardType.Phone,
                        leadingIcon = vectorResource(Res.drawable.ic_k_phone),
                        onValueChange = { component.setPhone(it) }
                    )
                    FormTextField(
                        label = "Email adresa",
                        value = email,
                        placeholder = "Email adresa",
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done,
                        leadingIcon = vectorResource(Res.drawable.ic_k_mail),
                        onValueChange = { component.setEmail(it) }
                    )
                }
            }
        }
    }

    // ── Bottom sheet pickers ───────────────────────────────────────────────────
    if (activeSheet != null) {
        ModalBottomSheet(
            onDismissRequest = { activeSheet = null },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
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

// ── Section ────────────────────────────────────────────────────────────────────

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    KSectionTitle(
        modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        title = title
    )
    KCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(VendorAccentSoft)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_info), tint = VendorAccent, size = 18.dp)
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Ink,
            textSize = 13.sp,
            lineHeight = 19.sp
        )
    }
}

// ── Text input ─────────────────────────────────────────────────────────────────

/** Test tag of the form's text field with [placeholder], for the end-to-end tests. */
fun newCustomerFieldTag(placeholder: String) = "new_customer_$placeholder"

/**
 * Label and white bordered field. The placeholder is drawn inside the text field, so tests can
 * find a field by it (and by [newCustomerFieldTag]).
 */
@Composable
private fun FormTextField(
    label: String,
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier.fillMaxWidth(),
    required: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    leadingIcon: ImageVector? = null,
    onValueChange: (String) -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier) {
        KFieldLabel(text = label, required = required, requiredColor = VendorAccent)
        BasicTextField(
            modifier = Modifier
                .fillMaxWidth()
                .testTag(newCustomerFieldTag(placeholder)),
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = TextStyle(
                color = KarikaUiColors.Ink,
                fontSize = 15.sp,
                fontFamily = karikaFonts()
            ),
            cursorBrush = SolidColor(VendorAccent),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(shape)
                        .background(KarikaColors.White)
                        .border(1.dp, KarikaUiColors.Border, shape)
                        .padding(horizontal = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null) {
                        KIcon(icon = leadingIcon, tint = KarikaUiColors.Muted, size = 18.dp)
                        Spacer(Modifier.width(10.dp))
                    }
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
                        }
                        innerTextField()
                    }
                }
            }
        )
    }
}

// ── Picker ─────────────────────────────────────────────────────────────────────

/**
 * Picker drawn as a bordered field with a chevron; the label and the field both open its sheet.
 * Until something is chosen it shows [placeholder].
 */
@Composable
private fun DropdownField(label: String, value: String?, placeholder: String, onClick: () -> Unit) {
    val focusManager = LocalFocusManager.current
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                focusManager.clearFocus()
                onClick()
            }
    ) {
        KFieldLabel(text = label, required = true, requiredColor = VendorAccent)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
                .clip(shape)
                .background(KarikaColors.White)
                .border(1.dp, KarikaUiColors.Border, shape)
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = value ?: placeholder,
                color = if (value != null) KarikaUiColors.Ink else KarikaUiColors.Subtle,
                textSize = 15.sp,
                maxLines = 1
            )
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_chevron_down),
                tint = KarikaUiColors.Muted,
                size = 18.dp
            )
        }
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
        color = KarikaUiColors.Ink,
        textSize = 18.sp,
        lineHeight = 24.sp,
        fontWeight = FontWeight.W700,
        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp)
    )
    KDivider()
    options.forEachIndexed { index, option ->
        val isSelected = selected == option
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(if (isSelected) VendorAccentSoft else Color.Transparent)
                .clickable { onSelect(option) }
                .padding(horizontal = 20.dp, vertical = 15.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier.weight(1f),
                text = option,
                color = if (isSelected) VendorAccent else KarikaUiColors.Ink,
                textSize = 15.sp,
                fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500
            )
            if (isSelected) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = VendorAccent, size = 18.dp)
            }
        }
        if (index < options.lastIndex) {
            KDivider(modifier = Modifier.padding(horizontal = 20.dp))
        }
    }
}

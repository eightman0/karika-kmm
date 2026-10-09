package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KKeyValueRow
import karika.distribucija.ba.ui.components.KMenuRow
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KTextField
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.isTabletLandscape
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_trash
import org.jetbrains.compose.resources.vectorResource

private const val COUNTRY = "Bosna i Hercegovina"

@Composable
fun AccountView(component: AccountComponent) {
    var editAddress by component.editAddress.asState()
    var editContact by component.editContact.asState()
    val showState = component.changePassSheet.asState()
    val editing = editAddress != null || editContact

    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                if (editing) {
                    KBackHeader(
                        title = if (editContact) "Informacije profila" else editAddress?.second.orEmpty(),
                        overline = "Moj nalog · uređivanje",
                        onBack = {
                            // Back from a form returns to the overview, as "Odustani" does
                            editAddress = null
                            editContact = false
                        }
                    )
                } else {
                    KBackHeader(title = "Moj nalog", onBack = { component.appBack() })
                }
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(KarikaUiColors.Page)
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                if (editAddress != null) {
                    UpdateAddress(component)
                } else if (editContact) {
                    UpdateContactInfo(component)
                } else {
                    if (isTabletLandscape()) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ContactInfo(modifier = Modifier.weight(1f), component = component)
                            BillingAddress(modifier = Modifier.weight(1f), component = component)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            ShippingAddress(modifier = Modifier.weight(1f), component = component)
                            AllShippingAddress(modifier = Modifier.weight(1f), component = component)
                        }
                    } else {
                        ContactInfo(component = component)
                        BillingAddress(component = component)
                        ShippingAddress(component = component)
                        AllShippingAddress(component = component)
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime.union(WindowInsets.navigationBars)))
                }
            }
            if (editing) {
                KBottomPanel {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        KSecondaryButton(
                            modifier = Modifier.weight(1f),
                            text = "Odustani"
                        ) {
                            if (editAddress != null) {
                                editAddress = null
                            } else {
                                editContact = false
                            }
                        }
                        KPrimaryButton(
                            modifier = Modifier.weight(1f),
                            text = "Sačuvaj izmjene"
                        ) {
                            if (editAddress != null) {
                                component.updateAddress()
                            } else {
                                component.updateContact()
                            }
                        }
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.ime.union(WindowInsets.navigationBars)))
                }
            }
        }
        if (showState.value) {
            ChangePasswordSheet(
                onCancel = {
                    showState.negate()
                },
                onChange = { old, new ->
                    showState.negate()
                    component.changePass(old, new)
                }
            )
        }
        DeleteAccountConfirmation(component)
    }
}

/** A value from the profile, with the profile's "-" for a missing one shown as the design's dash. */
private fun String?.shown(): String? = this?.takeIf { it.isNotBlank() && it != "-" }

private fun Address?.fullName(): String? =
    listOfNotNull(this?.firstname, this?.lastname).filter { it.isNotBlank() }.joinToString(" ").shown()

private fun Address?.cityAndPostcode(): String? =
    listOfNotNull(this?.city, this?.postcode).filter { it.isNotBlank() }.joinToString(", ").shown()

private fun Address?.rows(): List<Pair<String, String?>> = listOf(
    "Ime i prezime" to fullName(),
    "Telefon" to this?.telephone,
    "Adresa" to this?.street?.firstOrNull(),
    "Grad" to cityAndPostcode(),
    "Država" to COUNTRY
)

@Composable
private fun KeyValueRows(rows: List<Pair<String, String?>>) {
    rows.forEachIndexed { index, (label, value) ->
        if (index > 0) KDivider()
        KKeyValueRow(label = label, value = value)
    }
}

@Composable
private fun ContactInfo(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KSectionTitle(
            title = "Informacije profila",
            actionText = "Uredi",
            onAction = { component.edit(null, "Informacije profila") }
        )
        KCard(modifier = Modifier.fillMaxWidth()) {
            KeyValueRows(
                listOf(
                    "Pravno lice" to profile.companyName().shown(),
                    "ID broj" to profile.idNumber().shown(),
                    "PDV broj" to profile.pdv().shown(),
                    "Email" to profile.email,
                    "Veličina objekta" to profile.objectSize().shown(),
                    "Tip objekta" to profile.objectType().shown(),
                    "Broj zaposlenih" to profile.employeeCount().shown(),
                    "Viber broj" to profile.viberPhoneNumber().shown()
                )
            )
        }
        KCard(modifier = Modifier.fillMaxWidth()) {
            KMenuRow(
                text = "Promijeni lozinku",
                icon = vectorResource(Res.drawable.ic_k_lock),
                onClick = { component.showChangePass() }
            )
            if (!isKiosk()) {
                KDivider()
                KMenuRow(
                    text = "Obriši nalog",
                    icon = vectorResource(Res.drawable.ic_k_trash),
                    tileBackground = KarikaUiColors.RedSoft,
                    iconTint = KarikaUiColors.Red,
                    textColor = KarikaUiColors.Red,
                    onClick = { component.deleteAccount.negate() }
                )
            }
        }
    }
}

@Composable
private fun BillingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KSectionTitle(
            title = "Informacije za naplatu",
            actionText = "Uredi",
            onAction = {
                component.edit(
                    profile.billingAddress(),
                    "Informacije za naplatu",
                    false
                )
            }
        )
        KCard(modifier = Modifier.fillMaxWidth()) {
            KeyValueRows(profile.billingAddress().rows())
        }
    }
}

@Composable
private fun ShippingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KSectionTitle(
            title = "Zadana adresa za dostavu",
            actionText = "Uredi",
            onAction = { component.edit(profile.shippingAddress(), "Adresa za dostavu") }
        )
        KCard(modifier = Modifier.fillMaxWidth()) {
            KeyValueRows(profile.shippingAddress().rows())
        }
    }
}

@Composable
private fun AllShippingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    val deleteAddressConfirmation = mutableStateOf<Address?>(null).asState()
    if (profile.addresses.none { it.defaultShipping == null && it.defaultBilling == null }) {
        return
    }

    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KarikaText(
            text = "Spisak svih unesenih adresa za dostavu",
            color = KarikaUiColors.Ink,
            textSize = 18.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W700
        )
        profile.addresses
            .filter { it.defaultShipping == null && it.defaultBilling == null }
            .forEach { shippingAddress ->
                KCard(modifier = Modifier.fillMaxWidth()) {
                    KeyValueRows(shippingAddress.rows())
                    KDivider()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KSecondaryButton(
                            modifier = Modifier.weight(1f),
                            text = "Obriši",
                            height = 44.dp,
                            textColor = KarikaUiColors.Red
                        ) {
                            deleteAddressConfirmation.value = shippingAddress
                        }
                        KTonalButton(
                            modifier = Modifier.weight(1f),
                            text = "Uredi"
                        ) {
                            component.edit(shippingAddress, "Adresa za dostavu")
                        }
                    }
                }
            }
    }

    if (deleteAddressConfirmation.value != null) {
        AccountDialog(
            title = "Obriši adresu za dostavu",
            message = "Jeste li sigurni da želite obrisati ovu adresu za dostavu?",
            primaryButtonText = "Obriši",
            secondaryButtonText = "Odustani",
            onPrimaryClick = {
                component.deleteShippingAddress(deleteAddressConfirmation.value)
                deleteAddressConfirmation.value = null
            },
            onSecondaryClick = {
                deleteAddressConfirmation.value = null
            }
        )
    }
}

/** White form card of the edit screens. */
@Composable
private fun FormCard(content: @Composable () -> Unit) {
    KCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            content()
        }
    }
}

/** Label and white bordered text field; [allowedChars] filters typing as the old field did. */
@Composable
private fun FormField(
    label: String,
    value: MutableState<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "",
    required: Boolean = true,
    allowedChars: List<String> = emptyList(),
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
) {
    Column(modifier = modifier) {
        KFieldLabel(text = label, required = required)
        KTextField(
            value = value.value,
            onValueChange = { new ->
                if (new.startsWith(" ")) return@KTextField
                if (allowedChars.isNotEmpty() && new.any { c -> !allowedChars.contains(c.toString()) }) {
                    return@KTextField
                }
                value.value = new
            },
            placeholder = placeholder,
            keyboardType = keyboardType,
            imeAction = imeAction
        )
    }
}

/** The existing dropdown pickers, drawn as a bordered field with a chevron. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: MutableState<String>,
    values: List<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "",
) {
    if (values.isEmpty()) {
        return
    }
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Box(modifier = modifier) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Column(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            ) {
                KFieldLabel(text = label, required = true)
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
                        text = value.value.ifEmpty { placeholder },
                        color = if (value.value.isEmpty()) KarikaUiColors.Subtle else KarikaUiColors.Ink,
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
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = KarikaColors.White
            ) {
                values.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        onClick = {
                            value.value = option
                            expanded = false
                        },
                        text = {
                            KarikaText(
                                text = option,
                                fontWeight = if (option == value.value) FontWeight.W700 else FontWeight.W400,
                                textSize = 15.sp,
                                color = if (option == value.value) KarikaUiColors.Pink else KarikaUiColors.Ink
                            )
                        }
                    )
                    if (index < values.size - 1) {
                        KDivider()
                    }
                }
            }
        }
    }
}

@Composable
private fun UpdateAddress(component: AccountComponent) {
    FormCard {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField(
                modifier = Modifier.weight(1f),
                label = "Ime",
                value = component.firstname.asState(),
                placeholder = "Ime",
                allowedChars = KarikaConstants.lettersSpace
            )
            FormField(
                modifier = Modifier.weight(1f),
                label = "Prezime",
                value = component.lastname.asState(),
                placeholder = "Prezime",
                allowedChars = KarikaConstants.lettersSpace
            )
        }
        FormField(
            label = "Broj telefona",
            value = component.telephone.asState(),
            placeholder = "Broj telefona",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        )
        FormField(
            label = "Adresa i broj ulice",
            value = component.address.asState(),
            placeholder = "Adresa i broj ulice",
            allowedChars = KarikaConstants.numbersAndLettersSpace
        )
        Column {
            KFieldLabel(text = "Država", required = true)
            KTextField(
                value = COUNTRY,
                onValueChange = {},
                enabled = false,
                trailing = {
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_lock),
                        tint = KarikaUiColors.Subtle,
                        size = 16.dp
                    )
                }
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            DropdownField(
                modifier = Modifier.weight(1f),
                label = "Grad",
                placeholder = "Grad",
                value = component.city.asState(),
                values = KarikaConstants.cities()
            )
            FormField(
                modifier = Modifier.weight(1f),
                label = "Poštanski broj",
                value = component.postal.asState(),
                placeholder = "Poštanski broj",
                allowedChars = KarikaConstants.numbers,
                keyboardType = KeyboardType.Number
            )
        }
    }
}

@Composable
private fun UpdateContactInfo(component: AccountComponent) {
    FormCard {
        DropdownField(
            label = "Veličina objekta",
            placeholder = "Veličina objekta",
            value = component.objectSize.asState(),
            values = KarikaConstants.companySizes
        )
        DropdownField(
            label = "Tip objekta",
            placeholder = "Tip objekta",
            value = component.objectType.asState(),
            values = KarikaConstants.companyTypes
        )
        FormField(
            label = "Broj zaposlenih",
            value = component.employeeCount.asState(),
            placeholder = "Broj zaposlenih",
            keyboardType = KeyboardType.Number
        )
        FormField(
            label = "Viber broj telefona",
            value = component.viberPhoneNumber.asState(),
            placeholder = "Broj telefona",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        )
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        KSectionTitle(title = "Obavijesti")
        KCard(modifier = Modifier.fillMaxWidth()) {
            NotificationToggleRow(
                icon = vectorResource(Res.drawable.ic_k_mail),
                title = "Email obavijesti",
                checked = component.emailNotifications.asState().value,
                onCheckedChange = { component.emailNotifications.value = it }
            )
            KDivider()
            NotificationToggleRow(
                icon = vectorResource(Res.drawable.ic_k_chat),
                title = "Viber obavijesti",
                checked = component.viberNotifications.asState().value,
                onCheckedChange = { component.viberNotifications.value = it }
            )
            KDivider()
            NotificationToggleRow(
                icon = vectorResource(Res.drawable.ic_k_bell),
                title = "Push obavijesti",
                checked = component.pushNotifications.asState().value,
                onCheckedChange = { component.pushNotifications.value = it }
            )
        }
    }
}

/** Icon tile, title and the design's pink switch; the whole row toggles. */
@Composable
private fun NotificationToggleRow(
    icon: ImageVector,
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(value = checked, role = Role.Switch, onValueChange = onCheckedChange)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KarikaUiColors.Field),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = icon, tint = KarikaUiColors.Ink, size = 18.dp)
        }
        Spacer(Modifier.width(14.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 15.sp,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
        Box(
            modifier = Modifier
                .width(46.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(50))
                .background(if (checked) KarikaUiColors.Pink else KarikaUiColors.Border)
                .padding(3.dp),
            contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(KarikaColors.White)
            )
        }
    }
}

@Composable
private fun DeleteAccountConfirmation(component: AccountComponent) {
    val state = component.deleteAccount.asState()

    if (state.value) {
        AccountDialog(
            title = "Obriši nalog",
            message = "Jeste li sigurni da želite obrisati nalog?",
            primaryButtonText = "Obriši nalog",
            secondaryButtonText = "Odustani",
            onPrimaryClick = { component.deleteAccount() },
            onSecondaryClick = { state.negate() }
        )
    }
}

/** Confirmation dialog of this screen in the new style: white card, red icon, two buttons. */
@Composable
private fun AccountDialog(
    title: String,
    message: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(KarikaColors.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.RedSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_trash), tint = KarikaUiColors.Red, size = 24.dp)
            }
            Spacer(Modifier.height(16.dp))
            KarikaText(
                text = title,
                color = KarikaUiColors.Ink,
                textSize = 20.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W700
            )
            Spacer(Modifier.height(8.dp))
            KarikaText(
                text = message,
                color = KarikaUiColors.Muted,
                textSize = 15.sp,
                lineHeight = 21.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KSecondaryButton(
                    modifier = Modifier.weight(1f),
                    text = secondaryButtonText,
                    onClick = onSecondaryClick
                )
                KPrimaryButton(
                    modifier = Modifier.weight(1f),
                    text = primaryButtonText,
                    background = KarikaUiColors.Red,
                    onClick = onPrimaryClick
                )
            }
        }
    }
}


@Composable
fun ConfirmationModal(
    title: String,
    message: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    type: Int = 0, // 0 primary, 1 secondary
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .rounded(shape = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = title,
                    color = KarikaColors.Gray2,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    text = message,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W400
                )
                if (type == 0) {
                    HorizontalButtons(
                        modifier = Modifier,
                        primaryTitle = primaryButtonText,
                        secondaryTitle = secondaryButtonText
                    ) {
                        if (it == secondaryButtonText) {
                            onSecondaryClick()
                            return@HorizontalButtons
                        }
                        onPrimaryClick()
                    }
                } else {
                    HorizontalSecondaryButtons(
                        modifier = Modifier,
                        primaryTitle = primaryButtonText,
                        secondaryTitle = secondaryButtonText
                    ) {
                        if (it == secondaryButtonText) {
                            onSecondaryClick()
                            return@HorizontalSecondaryButtons
                        }
                        onPrimaryClick()
                    }
                }
            }
        }
    }
}

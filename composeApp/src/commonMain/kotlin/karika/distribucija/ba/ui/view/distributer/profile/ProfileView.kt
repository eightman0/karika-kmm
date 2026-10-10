package karika.distribucija.ba.ui.view.distributer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KConfirmDialog
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KMenuRow
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KTextField
import karika.distribucija.ba.ui.components.KToggle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.distributer.products.details.dashedBorder
import karika.distribucija.ba.ui.view.shop.profile.account.ChangePasswordSheet
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_image
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_trash
import org.jetbrains.compose.resources.vectorResource

private val ProfileTabs = listOf("Opšte", "Ciljanje", "Postavke")

@Composable
fun ProfileView(component: ProfileComponent) {
    var tab by rememberSaveable { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .hideKeyboard()
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 20.dp)
        ) {
            ProfileHeader(component = component, tab = tab, onTab = { tab = it })
            when (tab) {
                0 -> GeneralTab(component)
                1 -> TargetingTab(component)
                else -> SettingsTab(component)
            }
        }
        KBottomPanel {
            KPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Spasi izmjene",
                background = VendorAccent
            ) {
                component.updateProfile()
            }
        }
    }

    ChangePassword(component)
    DeleteAccountConfirmation(component)

    LaunchedEffect(Unit) {
        component.stateHolder.vendorSpecificHandler.getVendorDetails()
    }
}

/** White block continuing the shell header: logo, name, email and the segmented tabs. */
@Composable
private fun ProfileHeader(component: ProfileComponent, tab: Int, onTab: (Int) -> Unit) {
    val companyLogo = component.companyLogo.asState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KarikaUiColors.Field)
                    .border(1.dp, KarikaUiColors.Line, RoundedCornerShape(14.dp))
            ) {
                if (companyLogo.value.third != null) {
                    KarikaImage(modifier = Modifier.fillMaxSize(), model = companyLogo.value.third)
                }
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = component.companyName.asState().value,
                    color = KarikaUiColors.Ink,
                    textSize = 16.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = component.email.asState().value,
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KarikaUiColors.Field)
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ProfileTabs.forEachIndexed { index, title ->
                val selected = index == tab
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(36.dp)
                        .then(if (selected) Modifier.shadow(2.dp, RoundedCornerShape(9.dp)) else Modifier)
                        .clip(RoundedCornerShape(9.dp))
                        .background(if (selected) KarikaColors.White else KarikaUiColors.Field)
                        .clickable { onTab(index) },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = title,
                        color = if (selected) KarikaUiColors.Ink else KarikaUiColors.Muted,
                        textSize = 13.sp,
                        fontWeight = if (selected) FontWeight.W600 else FontWeight.W500,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

// Opšte

@Composable
private fun GeneralTab(component: ProfileComponent) {
    val companyCity = component.companyCity.asState()
    val companyCanton = component.companyCanton.asState()

    SectionTitle("Kompanija")
    SectionCard {
        ProfileField(
            label = "Naziv pravnog lica",
            value = component.companyName.asState(),
            placeholder = "Naziv pravnog lica",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            enabled = false
        )
        ProfileField(
            label = "O nama",
            value = component.aboutUs.asState(),
            placeholder = "Kratko predstavite kompaniju kupcima…",
            required = false,
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            singleLine = false
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileField(
                modifier = Modifier.weight(1f),
                label = "ID broj",
                value = component.companyId.asState(),
                placeholder = "ID broj",
                allowedChars = KarikaConstants.numbers,
                keyboardType = KeyboardType.Number,
                enabled = false
            )
            ProfileField(
                modifier = Modifier.weight(1f),
                label = "PDV broj",
                value = component.companyPdv.asState(),
                placeholder = "PDV broj",
                required = false,
                allowedChars = KarikaConstants.numbers,
                keyboardType = KeyboardType.Number,
                enabled = false
            )
        }
    }

    SectionTitle("Kontakt")
    SectionCard {
        ProfileField(
            label = "Ime kontakta",
            value = component.contactName.asState(),
            placeholder = "Ime kontakta",
            allowedChars = KarikaConstants.numbersAndLettersSpace
        )
        ProfileField(
            label = "Email adresa",
            value = component.email.asState(),
            placeholder = "Email adresa",
            allowedChars = KarikaConstants.numbersAndLettersSpace,
            keyboardType = KeyboardType.Email,
            enabled = false
        )
        ProfileField(
            label = "Telefon",
            value = component.companyPhone.asState(),
            placeholder = "Broj telefona",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone
        )
        ProfileField(
            label = "Viber",
            value = component.companyViberPhone.asState(),
            placeholder = "Viber broj telefona",
            required = false,
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone
        )
    }

    SectionTitle("Lokacija")
    SectionCard {
        ProfileField(
            label = "Entitet",
            value = component.companyEntity.asState(),
            placeholder = "Entitet",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            enabled = false
        )
        if (companyCanton.value.isNotEmpty() || companyCity.value.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (companyCanton.value.isNotEmpty()) {
                    ProfileField(
                        modifier = Modifier.weight(1f),
                        label = "Kanton",
                        value = companyCanton,
                        placeholder = "Kanton",
                        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                        enabled = false
                    )
                }
                if (companyCity.value.isNotEmpty()) {
                    ProfileField(
                        modifier = Modifier.weight(1f),
                        label = "Grad",
                        value = companyCity,
                        placeholder = "Grad",
                        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                        enabled = false
                    )
                }
            }
        }
    }

    SectionTitle("Poslovanje")
    SectionCard {
        ProfileField(
            label = "Minimalna vrijednost narudžbe",
            value = component.minOrderAmount.asState(),
            placeholder = "Minimalna vrijednost narudžbe",
            required = false,
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            trailing = {
                KarikaText(text = "KM", color = KarikaUiColors.Muted, textSize = 15.sp, fontWeight = FontWeight.W600)
            }
        )
        ProfileField(
            label = "Broj računa",
            value = component.bankAccountNumber.asState(),
            placeholder = "Broj računa",
            required = false,
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone
        )
    }
}

// Ciljanje

@Composable
private fun TargetingTab(component: ProfileComponent) {
    val config = component.stateHolder.commonHandler.config.value
    TargetSection(
        title = "Ciljana grupa kupaca",
        options = config.customerGroupList,
        selected = component.customerGroups.asState()
    )
    TargetSection(
        title = "Ciljani region kupaca",
        options = config.customerRegionList,
        selected = component.customerRegions.asState()
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TargetSection(
    title: String,
    options: List<KarikaUnit>,
    selected: MutableState<List<KarikaUnit>>,
) {
    fun isSelected(option: KarikaUnit) = selected.value.any { c -> c.unit == option.unit() }
    val selectedCount = options.count { isSelected(it) }
    val allSelected = options.isNotEmpty() && selectedCount == options.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier.weight(1f),
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 17.sp,
            lineHeight = 22.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        if (options.isNotEmpty()) {
            KarikaText(
                text = "$selectedCount od ${options.size} · ",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
            KarikaText(
                modifier = Modifier.clickable {
                    selected.value = if (allSelected) emptyList() else options
                },
                text = "Sve",
                color = VendorAccent,
                textSize = 12.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
    SectionCard(spacing = 8.dp) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { option ->
                val on = isSelected(option)
                KChip(
                    text = option.label(),
                    selected = on,
                    selectedColor = VendorAccent,
                    trailingIcon = if (on) vectorResource(Res.drawable.ic_k_check) else null
                ) {
                    selected.value = if (isSelected(option)) {
                        selected.value.filterNot { c -> c.unit == option.unit() }
                    } else {
                        selected.value + option
                    }
                }
            }
        }
    }
}

// Postavke

@Composable
private fun SettingsTab(component: ProfileComponent) {
    val emailNotifications = component.emailNotifications.asState()
    val viberNotifications = component.viberNotifications.asState()
    val pushNotifications = component.pushNotifications.asState()
    val viber = component.companyViberPhone.asState().value

    SectionTitle("Obavijesti")
    KCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        NotificationRow(
            icon = vectorResource(Res.drawable.ic_k_mail),
            title = "Email",
            subtitle = "Nove narudžbe i poruke",
            checked = emailNotifications.value
        ) { emailNotifications.value = it }
        KDivider()
        NotificationRow(
            icon = vectorResource(Res.drawable.ic_k_chat),
            title = "Viber",
            subtitle = if (viber.isNotBlank()) "Na $viber" else "Na Viber broj kompanije",
            checked = viberNotifications.value
        ) { viberNotifications.value = it }
        KDivider()
        NotificationRow(
            icon = vectorResource(Res.drawable.ic_k_bell),
            title = "Push",
            subtitle = "Na ovom uređaju",
            checked = pushNotifications.value
        ) { pushNotifications.value = it }
    }

    SectionTitle("Brending")
    SectionCard(spacing = 14.dp) {
        LogoRow(component)
        BannerBlock(component)
    }

    SectionTitle("Sigurnost")
    KCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
        KMenuRow(
            text = "Promijeni lozinku",
            icon = vectorResource(Res.drawable.ic_k_lock),
            tileBackground = VendorAccentSoft,
            iconTint = VendorAccent
        ) {
            component.changePassSheet.negate()
        }
        KDivider()
        KMenuRow(
            text = "Obriši nalog",
            icon = vectorResource(Res.drawable.ic_k_trash),
            tileBackground = KarikaUiColors.RedSoft,
            iconTint = KarikaUiColors.Red,
            textColor = KarikaUiColors.Red
        ) {
            component.deleteAccount.negate()
        }
    }
}

@Composable
private fun NotificationRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTile(icon = icon)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(text = title, color = KarikaUiColors.Ink, textSize = 14.sp, fontWeight = FontWeight.W600, maxLines = 1)
            Spacer(Modifier.height(1.dp))
            KarikaText(text = subtitle, color = KarikaUiColors.Muted, textSize = 12.sp, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        KToggle(checked = checked, color = VendorAccent, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun IconTile(icon: ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(VendorAccentSoft),
        contentAlignment = Alignment.Center
    ) {
        KIcon(icon = icon, tint = VendorAccent, size = 18.dp)
    }
}

@Composable
private fun LogoRow(component: ProfileComponent) {
    val companyLogo = component.companyLogo.asState()
    val hasLogo = companyLogo.value.third != null

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (hasLogo) KarikaUiColors.Field else VendorAccentSoft)
                .then(
                    if (hasLogo) Modifier.border(1.dp, KarikaUiColors.Line, RoundedCornerShape(14.dp))
                    else Modifier.dashedBorder(color = VendorAccent, strokeWidth = 1.5.dp, cornerRadius = 14.dp)
                )
                .clickable {
                    if (hasLogo) {
                        component.showImagePreview(companyLogo.value.third?.toString() ?: "")
                    } else {
                        component.pickImage(1)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            if (hasLogo) {
                KarikaImage(modifier = Modifier.fillMaxSize(), model = companyLogo.value.third)
            } else {
                KIcon(icon = vectorResource(Res.drawable.ic_k_image), tint = VendorAccent, size = 22.dp)
            }
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(text = "Logo kompanije", color = KarikaUiColors.Ink, textSize = 14.sp, fontWeight = FontWeight.W600)
            Spacer(Modifier.height(2.dp))
            KarikaText(
                text = if (hasLogo) "PNG ili JPG, kvadrat" else "Dodaj sliku · PNG ili JPG, kvadrat",
                color = KarikaUiColors.Muted,
                textSize = 12.sp
            )
        }
        if (hasLogo) {
            DeleteImageButton { companyLogo.value = Triple("", "", null) }
        }
    }
}

@Composable
private fun BannerBlock(component: ProfileComponent) {
    val companyBanner = component.companyBanner.asState()
    val shape = RoundedCornerShape(12.dp)

    Column {
        KarikaText(
            modifier = Modifier.padding(bottom = 8.dp),
            text = "Baner kompanije",
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
        if (companyBanner.value.third == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(shape)
                    .background(VendorAccentSoft.copy(alpha = 0.5f))
                    .dashedBorder(color = VendorAccent, strokeWidth = 1.5.dp, cornerRadius = 12.dp)
                    .clickable { component.pickImage(2) },
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_image), tint = VendorAccent, size = 18.dp)
                Spacer(Modifier.width(8.dp))
                KarikaText(text = "Dodaj sliku · 1200×400", color = VendorAccent, textSize = 13.sp, fontWeight = FontWeight.W600)
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(96.dp)
                    .clip(shape)
                    .background(KarikaUiColors.Field)
                    .border(1.dp, KarikaUiColors.Line, shape)
            ) {
                KarikaImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable {
                            component.showImagePreview(companyBanner.value.third?.toString() ?: "")
                        },
                    model = companyBanner.value.third
                )
                DeleteImageButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                ) { companyBanner.value = Triple("", "", null) }
            }
        }
    }
}

@Composable
private fun DeleteImageButton(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(KarikaUiColors.RedSoft)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_trash), tint = KarikaUiColors.Red, size = 18.dp)
    }
}

// Shared pieces

@Composable
private fun SectionTitle(title: String) {
    KarikaText(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
        text = title,
        color = KarikaUiColors.Ink,
        textSize = 17.sp,
        lineHeight = 22.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun SectionCard(
    spacing: androidx.compose.ui.unit.Dp = 14.dp,
    content: @Composable () -> Unit,
) {
    KCard(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(spacing)
        ) {
            content()
        }
    }
}

/** Label and bordered field; [allowedChars] filters typing as the old fields did. */
@Composable
private fun ProfileField(
    label: String,
    value: MutableState<String>,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    required: Boolean = true,
    enabled: Boolean = true,
    allowedChars: List<String> = emptyList(),
    keyboardType: KeyboardType = KeyboardType.Text,
    singleLine: Boolean = true,
    trailing: @Composable (androidx.compose.foundation.layout.RowScope.() -> Unit)? = null,
) {
    Column(modifier = modifier) {
        KFieldLabel(text = label, required = required, requiredColor = VendorAccent)
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
            enabled = enabled,
            keyboardType = keyboardType,
            imeAction = ImeAction.Next,
            singleLine = singleLine,
            minHeight = if (singleLine) 50.dp else 84.dp,
            trailing = trailing
        )
    }
}

@Composable
private fun ChangePassword(component: ProfileComponent) {
    val showState = component.changePassSheet.asState()
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
}

@Composable
private fun DeleteAccountConfirmation(component: ProfileComponent) {
    val state = component.deleteAccount.asState()

    if (state.value) {
        KConfirmDialog(
            title = "Obriši nalog",
            message = "Jeste li sigurni da želite obrisati nalog?",
            icon = vectorResource(Res.drawable.ic_k_trash),
            confirmText = "Obriši nalog",
            dismissText = "Odustani",
            onConfirm = { component.deleteAccount() },
            onDismiss = { state.negate() }
        )
    }
}

package karika.distribucija.ba.ui.view.prelogin.registration

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
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.ui.common.getEnvPrefix
import karika.distribucija.ba.ui.common.openPdf
import karika.distribucija.ba.ui.components.KBackButton
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KChip
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KLogo
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_eye
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import org.jetbrains.compose.resources.vectorResource

/** Pink for customers, the suppliers' blue for suppliers, as on the login screen. */
private fun RegistrationComponent.accent(): Color =
    if (userType.isShop()) KarikaUiColors.Pink else KarikaUiColors.Blue

/** Accent of the fields (cursor, focus, required mark), provided by [RegistrationView]. */
private val LocalAccent = staticCompositionLocalOf { KarikaUiColors.Pink }

private fun RegistrationComponent.backToLogin() {
    stateHolder.preLoginNavigation.replaceAll(PreLoginConfig.Login(userType))
}

@Composable
fun RegistrationView(component: RegistrationComponent) {
    CompositionLocalProvider(LocalAccent provides component.accent()) {
        RegistrationContent(component)
    }
}

@Composable
private fun RegistrationContent(component: RegistrationComponent) {
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        component = component
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(
                    WindowInsets.ime
                        .exclude(WindowInsets.navigationBars)
                        .only(WindowInsetsSides.Bottom)
                )
                .verticalScroll(rememberScrollState())
        ) {
            Header(component)
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                CompanyInfo(component)
                ContactInfo(component)
                LoginInfo(component)
                Spacer(Modifier.height(20.dp))
                KPrimaryButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Registruj se",
                    background = component.accent()
                ) {
                    component.register()
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp, bottom = 20.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    KarikaText(
                        text = "Već imate račun? ",
                        color = KarikaUiColors.Muted,
                        textSize = 14.sp
                    )
                    KarikaText(
                        modifier = Modifier.clickable { component.backToLogin() },
                        text = "Prijavite se",
                        color = component.accent(),
                        textSize = 14.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
            }
        }
    }
}

@Composable
private fun Header(component: RegistrationComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(KarikaHeaderShape)
            .background(KarikaColors.White)
            .windowInsetsPadding(WindowInsets.statusBars)
            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
    ) {
        KBackButton(modifier = Modifier.offset(x = (-4).dp)) {
            component.backToLogin()
        }
        Spacer(Modifier.height(22.dp))
        KLogo(width = 52.dp)
        Spacer(Modifier.height(20.dp))
        KarikaText(
            text = component.title,
            color = KarikaUiColors.Ink,
            fontWeight = FontWeight.W800,
            textSize = 26.sp,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(8.dp))
        if (component.userType.isShop()) {
            KPill(text = "Kupac", background = KarikaUiColors.PinkSoft, color = KarikaUiColors.Pink)
        } else {
            KPill(text = "Dobavljač", background = KarikaUiColors.BlueSoft, color = KarikaUiColors.Blue)
        }
        Spacer(Modifier.height(10.dp))
        KarikaText(
            text = "Popunite podatke o firmi. Nakon provjere Karika tim aktivira vaš račun.",
            color = KarikaUiColors.Muted,
            textSize = 14.sp,
            lineHeight = 20.sp
        )
    }
}

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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CompanyInfo(component: RegistrationComponent) {
    val customerGroups = remember { component.customerGroups }
    val customerRegions = remember { component.customerRegions }

    Section("Informacije o pravnom licu") {
        FormField(
            label = "Naziv pravnog lica",
            value = component.companyName,
            placeholder = "Naziv pravnog lica",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus(".")
        )
        FormField(
            label = "ID broj",
            value = component.companyId,
            placeholder = "ID broj",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number
        )
        FormField(
            label = "PDV broj",
            value = component.companyPdv,
            placeholder = "PDV broj",
            required = false,
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number
        )
        CompanyAddress(component)
        if (component.userType.isShop()) {
            DropdownField(
                label = "Veličina objekta",
                placeholder = "Odaberite veličinu objekta",
                value = component.companySize,
                values = KarikaConstants.companySizes
            )
            DropdownField(
                label = "Tip objekta",
                placeholder = "Odaberite tip objekta",
                value = component.companyType,
                values = KarikaConstants.companyTypes
            )
            FormField(
                label = "Broj zaposlenih",
                value = component.companyEmployees,
                placeholder = "Broj zaposlenih",
                required = false,
                allowedChars = KarikaConstants.numbers,
                keyboardType = KeyboardType.Number
            )
        }
    }

    if (!component.userType.isShop()) {
        val groups = component.stateHolder.commonHandler.config.value.customerGroupList
        val regions = component.stateHolder.commonHandler.config.value.customerRegionList
        if (groups.isNotEmpty() || regions.isNotEmpty()) {
            Section("Ciljani kupci") {
                if (groups.isNotEmpty()) {
                    GroupTitle("Ciljana grupa kupaca")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groups.forEach { group ->
                            KChip(
                                selectedColor = component.accent(),
                                text = group.label(),
                                selected = customerGroups.value.contains(group)
                            ) {
                                if (customerGroups.value.contains(group)) {
                                    customerGroups.value -= group
                                } else {
                                    customerGroups.value += group
                                }
                            }
                        }
                    }
                }
                if (groups.isNotEmpty() && regions.isNotEmpty()) {
                    KDivider()
                }
                if (regions.isNotEmpty()) {
                    GroupTitle("Ciljana regija kupaca")
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        regions.forEach { region ->
                            KChip(
                                selectedColor = component.accent(),
                                text = region.label(),
                                selected = customerRegions.value.contains(region)
                            ) {
                                if (customerRegions.value.contains(region)) {
                                    customerRegions.value -= region
                                } else {
                                    customerRegions.value += region
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Title above a group of chips, in the field labels' font with the accent color. */
@Composable
private fun GroupTitle(text: String) {
    KarikaText(
        modifier = Modifier.padding(bottom = 2.dp),
        text = text,
        color = LocalAccent.current,
        textSize = 14.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun CompanyAddress(component: RegistrationComponent) {
    val entity = component.companyEntity
    val canton = component.companyCanton

    DropdownField(
        label = "Entitet",
        placeholder = "Odaberite entitet",
        value = entity,
        values = component.entities.value
    ) {
        when (entity.value) {
            "Federacija" -> {
                component.canton.value = KarikaConstants.cantons("Federacija")
                component.city.value = emptyList()
            }

            else -> {
                component.canton.value = emptyList()
                component.city.value = KarikaConstants.cantons(entity.value)
                component.companyCanton.value = ""
                component.companyCity.value = ""
            }
        }
    }
    DropdownField(
        label = "Kanton",
        placeholder = "Odaberite kanton",
        value = canton,
        values = component.canton.value
    ) {
        component.companyCity.value = ""
        component.city.value = KarikaConstants.cities(canton.value)
    }
    DropdownField(
        label = "Grad",
        placeholder = "Odaberite grad",
        value = component.companyCity,
        values = component.city.value
    )
}

@Composable
private fun ContactInfo(component: RegistrationComponent) {
    Section("Kontakt osoba") {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            FormField(
                modifier = Modifier.weight(1f),
                label = "Ime",
                value = component.contactFirstname,
                placeholder = "Ime",
                allowedChars = KarikaConstants.lettersSpace
            )
            FormField(
                modifier = Modifier.weight(1f),
                label = "Prezime",
                value = component.contactLastname,
                placeholder = "Prezime",
                allowedChars = KarikaConstants.lettersSpace
            )
        }
        if (component.userType.isShop()) {
            FormField(
                label = "Adresa i broj ulice",
                value = component.contactAddress,
                placeholder = "Adresa i broj ulice",
                allowedChars = KarikaConstants.numbersAndLettersSpace
            )
            FormField(
                label = "Poštanski broj",
                value = component.contactPostal,
                placeholder = "Poštanski broj",
                allowedChars = KarikaConstants.numbers,
                keyboardType = KeyboardType.Number
            )
        }
        FormField(
            label = "Broj telefona",
            value = component.contactPhone,
            placeholder = "Broj telefona",
            allowedChars = KarikaConstants.numbers.plus("+"),
            keyboardType = KeyboardType.Phone,
            maxLength = 14
        )
    }
}

@Composable
private fun LoginInfo(component: RegistrationComponent) {
    val passwordVisible = remember { mutableStateOf(false) }
    val confirmVisible = remember { mutableStateOf(false) }

    Section("Informacije za prijavu") {
        FormField(
            label = "Email adresa",
            value = component.email,
            placeholder = "Email adresa",
            leadingIcon = vectorResource(Res.drawable.ic_k_mail),
            allowedChars = KarikaConstants.numbersAndLetters.plus("@").plus(".").plus("_"),
            keyboardType = KeyboardType.Email
        )
        FormField(
            label = "Šifra",
            value = component.password,
            placeholder = "Šifra",
            leadingIcon = vectorResource(Res.drawable.ic_k_lock),
            keyboardType = KeyboardType.Password,
            password = !passwordVisible.value,
            onTogglePassword = { passwordVisible.value = !passwordVisible.value }
        )
        FormField(
            label = "Potvrdi šifru",
            value = component.confirmPassword,
            placeholder = "Potvrdi šifru",
            leadingIcon = vectorResource(Res.drawable.ic_k_lock),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            password = !confirmVisible.value,
            onTogglePassword = { confirmVisible.value = !confirmVisible.value }
        )
        KarikaText(
            text = "Najmanje 8 karaktera, jedno veliko slovo i jedan broj.",
            color = KarikaUiColors.Muted,
            textSize = 12.sp,
            lineHeight = 16.sp
        )
    }

    Spacer(Modifier.height(14.dp))
    TermsCheckbox(component)
}

/** "Slažem se sa uslovima korištenja i politikom privatnosti", with both links opening the documents. */
@Composable
private fun TermsCheckbox(component: RegistrationComponent) {
    val agree = component.agree
    val accent = component.accent()
    val text = buildAnnotatedString {
        withStyle(SpanStyle(color = KarikaUiColors.Ink)) {
            append("Slažem se sa ")
        }
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms",
                styles = TextLinkStyles(SpanStyle(color = accent, fontWeight = FontWeight.W700)),
                linkInteractionListener = {
                    openPdf("https://${getEnvPrefix()}karika.ba/cms/odredbe-i-uvjeti")
                }
            )
        ) {
            append("uslovima korištenja")
        }
        withStyle(SpanStyle(color = KarikaUiColors.Ink)) {
            append(" i ")
        }
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacy",
                styles = TextLinkStyles(SpanStyle(color = accent, fontWeight = FontWeight.W700)),
                linkInteractionListener = {
                    openPdf("https://${getEnvPrefix()}karika.ba/cms/politika-privatnosti")
                }
            )
        ) {
            append("politikom privatnosti")
        }
        withStyle(SpanStyle(color = KarikaUiColors.Ink)) {
            append(" i prihvatam da Karika sačuva moje lične podatke.")
        }
    }

    KCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = agree.value,
                    role = Role.Checkbox,
                    onValueChange = { agree.value = it }
                )
                .padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            val shape = RoundedCornerShape(6.dp)
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(shape)
                    .background(if (agree.value) accent else KarikaColors.White)
                    .border(1.5.dp, if (agree.value) accent else KarikaUiColors.Border, shape),
                contentAlignment = Alignment.Center
            ) {
                if (agree.value) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = KarikaColors.White, size = 15.dp)
                }
            }
            Spacer(Modifier.width(12.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                atext = text,
                color = KarikaUiColors.Ink,
                textSize = 13.5.sp,
                lineHeight = 19.sp
            )
        }
    }
}

/**
 * Label and white bordered field. The placeholder is drawn inside the text field, so tests can
 * find a field by it. [allowedChars] and [maxLength] filter typing as the old field did.
 */
@Composable
private fun FormField(
    label: String,
    value: MutableState<String>,
    modifier: Modifier = Modifier.fillMaxWidth(),
    placeholder: String = "",
    required: Boolean = true,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    allowedChars: List<String> = emptyList(),
    maxLength: Int = Int.MAX_VALUE,
    keyboardType: KeyboardType = KeyboardType.Text,
    imeAction: ImeAction = ImeAction.Next,
    password: Boolean = false,
    onTogglePassword: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = modifier) {
        KFieldLabel(text = label, required = required, requiredColor = LocalAccent.current)
        BasicTextField(
            modifier = Modifier.fillMaxWidth(),
            value = value.value,
            onValueChange = { new ->
                if (new.startsWith(" ")) return@BasicTextField
                if (new.length > maxLength) return@BasicTextField
                if (allowedChars.isNotEmpty() && new.any { c -> !allowedChars.contains(c.toString()) }) {
                    return@BasicTextField
                }
                value.value = new
            },
            singleLine = true,
            textStyle = TextStyle(
                color = KarikaUiColors.Ink,
                fontSize = 15.sp,
                fontFamily = karikaFonts()
            ),
            cursorBrush = SolidColor(LocalAccent.current),
            visualTransformation = if (password) PasswordVisualTransformation() else VisualTransformation.None,
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
                        if (value.value.isEmpty()) {
                            KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
                        }
                        innerTextField()
                    }
                    if (onTogglePassword != null) {
                        Spacer(Modifier.width(8.dp))
                        KIcon(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .clickable(onClick = onTogglePassword),
                            icon = vectorResource(Res.drawable.ic_k_eye),
                            tint = if (password) KarikaUiColors.Muted else LocalAccent.current,
                            size = 20.dp
                        )
                    }
                }
            }
        )
    }
}

/** Dropdown drawn as a bordered field with a chevron; tapping the label also opens it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    value: MutableState<String>,
    values: List<String>,
    placeholder: String = "",
    onChange: (String) -> Unit = {},
) {
    if (values.isEmpty()) {
        return
    }
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    val accent = LocalAccent.current
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        Column(
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        ) {
            KFieldLabel(text = label, required = true, requiredColor = accent)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(shape)
                    .background(KarikaColors.White)
                    .border(1.dp, if (expanded) LocalAccent.current else KarikaUiColors.Border, shape)
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
                        onChange(option)
                    },
                    text = {
                        KarikaText(
                            text = option,
                            fontWeight = if (option == value.value) FontWeight.W700 else FontWeight.W400,
                            textSize = 15.sp,
                            color = if (option == value.value) accent else KarikaUiColors.Ink
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

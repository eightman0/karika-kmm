package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.ui.common.getEnvPrefix
import karika.distribucija.ba.ui.common.openPdf
import karika.distribucija.ba.ui.components.KarikaBox
import karika.distribucija.ba.ui.components.KarikaCheckbox
import karika.distribucija.ba.ui.components.KarikaCheckboxSecondary
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaPicker
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import karika.distribucija.ba.util.KarikaConstants


@Composable
fun RegistrationView(component: RegistrationComponent) {
    KarikaBox {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
                    .background(color = component.getColor())
            )
        }
        KarikaScaffold(
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                TopBarWithBack(component.title, color = component.getColor()) {
                    component.stateHolder.preLoginNavigation.replaceAll(
                        PreLoginConfig.Login(component.userType)
                    )
                }
            },
            component = component
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .verticalScroll(rememberScrollState())
                        .padding(it),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompanyInfo(component)
                    YSpacer16()
                    ContactInfo(component)
                    YSpacer16()
                    LoginInfo(component)
                    YSpacer16()
                    if (component.userType.isShop()) {
                        PrimaryButtonFilled(
                            title = "Prijavi se",
                        ) {
                            component.register()
                        }
                    } else {
                        SecondaryButtonFilled(
                            title = "Prijavi se",
                        ) {
                            component.register()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyInfo(component: RegistrationComponent) {
    val customerGroups = component.customerGroups.asState()
    val customerRegions = component.customerRegions.asState()
    val gridColumnCount = gridColumnCount()

    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = "Informacije o pravnom licu",
        color = KarikaColors.Black,
        fontWeight = FontWeight.W600,
        textSize = 18.sp
    )
    YSpacer8()
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Naziv pravnog lica*",
        value = component.companyName.asState(),
        placeholder = "Naziv pravnog lica",
        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "ID broj*",
        value = component.companyId.asState(),
        placeholder = "ID broj",
        keyboardType = KeyboardType.Number,
        allowedChars = KarikaConstants.numbers,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "PDV broj",
        value = component.companyPdv.asState(),
        placeholder = "PDV broj",
        allowedChars = KarikaConstants.numbers,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    )
    CompanyAddress(component)
    if (component.userType.isShop()) {
        KarikaPicker(
            title = "Veličina objekta*",
            placeholder = "Veličina objekta*",
            value = component.companySize.asState(),
            values = mutableStateOf(KarikaConstants.companySizes).asState()
        )
        KarikaPicker(
            title = "Tip objekta*",
            placeholder = "Tip objekta",
            value = component.companyType.asState(),
            values = mutableStateOf(KarikaConstants.companyTypes).asState()
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj zaposlenih",
            value = component.companyEmployees.asState(),
            placeholder = "Broj zaposlenih",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    } else {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Ciljana grupa kupaca",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        component.stateHolder.commonHandler.config.value.customerGroupList
            .chunked(gridColumnCount)
            .forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    it.forEach { it1 ->
                        if (component.userType.isShop()) {
                            KarikaCheckbox(
                                modifier = Modifier.weight(1f),
                                title = it1.label(),
                                value = customerGroups.value.contains(it1)
                            ) {
                                if (customerGroups.value.contains(it1)) {
                                    customerGroups.value -= it1
                                } else {
                                    customerGroups.value += it1
                                }
                            }
                        } else {
                            KarikaCheckboxSecondary(
                                modifier = Modifier.weight(1f),
                                title = it1.label(),
                                value = customerGroups.value.contains(it1)
                            ) {
                                if (customerGroups.value.contains(it1)) {
                                    customerGroups.value -= it1
                                } else {
                                    customerGroups.value += it1
                                }
                            }
                        }
                    }
                }
            }
        YSpacer8()
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Ciljana region kupaca",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        component.stateHolder.commonHandler.config.value.customerRegionList.chunked(gridColumnCount)
            .forEach {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    it.forEach { it1 ->
                        if (component.userType.isShop()) {
                            KarikaCheckbox(
                                modifier = Modifier.weight(1f),
                                title = it1.label(),
                                value = customerRegions.value.contains(it1)
                            ) {
                                if (customerRegions.value.contains(it1)) {
                                    customerRegions.value -= it1
                                } else {
                                    customerRegions.value += it1
                                }
                            }
                        } else {
                            KarikaCheckboxSecondary(
                                modifier = Modifier.weight(1f),
                                title = it1.label(),
                                value = customerRegions.value.contains(it1)
                            ) {
                                if (customerRegions.value.contains(it1)) {
                                    customerRegions.value -= it1
                                } else {
                                    customerRegions.value += it1
                                }
                            }
                        }
                    }
                }
            }
    }
}

@Composable
private fun ContactInfo(component: RegistrationComponent) {
    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = "Kontakt osoba",
        color = KarikaColors.Black,
        fontWeight = FontWeight.W600,
        textSize = 18.sp
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Ime*",
        value = component.contactFirstname.asState(),
        placeholder = "Ime",
        allowedChars = KarikaConstants.lettersSpace,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Prezime*",
        value = component.contactLastname.asState(),
        placeholder = "Prezime",
        allowedChars = KarikaConstants.lettersSpace,
        imeAction = ImeAction.Next
    )
    if (component.userType.isShop()) {
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Adresa i broj ulice*",
            value = component.contactAddress.asState(),
            placeholder = "Adresa i broj ulice",
            allowedChars = KarikaConstants.numbersAndLettersSpace,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Poštanski broj*",
            value = component.contactPostal.asState(),
            placeholder = "Poštanski broj",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
    }
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Broj telefona*",
        value = component.contactPhone.asState(),
        placeholder = "Broj telefona",
        allowedChars = KarikaConstants.numbers,
        keyboardType = KeyboardType.Phone,
        imeAction = ImeAction.Next
    )
}

@Composable
private fun LoginInfo(viewModel: RegistrationComponent) {
    val agree = viewModel.agree.asState()
    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = "Informacije za prijavu",
        color = KarikaColors.Black,
        fontWeight = FontWeight.W600,
        textSize = 18.sp
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Email adresa*",
        value = viewModel.email.asState(),
        placeholder = "Email adresa",
        keyboardType = KeyboardType.Email,
        imeAction = ImeAction.Next
    )
    KarikaPasswordTextField(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Šifra*",
        value = viewModel.password.asState(),
        placeholder = "Šifra",
        imeAction = ImeAction.Next
    )
    KarikaPasswordTextField(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Potvrdi šifru*",
        value = viewModel.confirmPassword.asState(),
        placeholder = "Potvrdi šifru",
        imeAction = ImeAction.Done
    )
    if (viewModel.userType.isShop()) {
        KarikaCheckbox(
            atitle = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append("Slažem se sa")
                }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "",
                        styles = TextLinkStyles(),
                        linkInteractionListener = {
                            openPdf("https://${getEnvPrefix()}karika.ba/cms/odredbe-i-uvjeti")
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray4,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(" uslovima korištenja ")
                    }
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append("i")
                }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "",
                        styles = TextLinkStyles(),
                        linkInteractionListener = {
                            openPdf("https://${getEnvPrefix()}karika.ba/cms/politika-privatnosti")
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray4,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(" politikom privatnosti")
                    }
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append(" i prihvatam da Karika sačuva moje lične podatke.")
                }
            }
        ) {
            agree.value = it
        }
    } else {
        KarikaCheckboxSecondary(
            atitle = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append("Slažem se sa")
                }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "",
                        styles = TextLinkStyles(),
                        linkInteractionListener = {
                            openPdf("https://${getEnvPrefix()}karika.ba/cms/odredbe-i-uvjeti")
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray4,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(" uslovima korištenja ")
                    }
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append("i")
                }
                withLink(
                    LinkAnnotation.Clickable(
                        tag = "",
                        styles = TextLinkStyles(),
                        linkInteractionListener = {
                            openPdf("https://${getEnvPrefix()}karika.ba/cms/politika-privatnosti")
                        }
                    )
                ) {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaColors.Gray4,
                            fontSize = 14.sp,
                            textDecoration = TextDecoration.Underline
                        )
                    ) {
                        append(" politikom privatnosti")
                    }
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray4,
                        fontSize = 14.sp
                    )
                ) {
                    append(" i prihvatam da Karika sačuva moje lične podatke.")
                }
            }
        ) {
            agree.value = it
        }
    }
}

@Composable
private fun CompanyAddress(viewModel: RegistrationComponent) {
    val entity = viewModel.companyEntity.asState()
    val canton = viewModel.companyCanton.asState()
    val city = viewModel.companyCity.asState()
    val municipality = viewModel.companyMunicipality.asState()

    KarikaPicker(
        title = "Entitet*",
        placeholder = "Entitet",
        values = viewModel.entities.asState(),
        value = entity
    )
    KarikaPicker(
        title = "Kanton*",
        placeholder = "Kanton",
        values = viewModel.canton.asState(),
        value = canton
    )
    KarikaPicker(
        title = "Grad*",
        placeholder = "Grad",
        values = viewModel.city.asState(),
        value = city
    )
    KarikaPicker(
        title = "Opčina*",
        placeholder = "Općina",
        values = viewModel.municipality.asState(),
        value = municipality
    )

    LaunchedEffect(entity.value) {
        when (entity.value) {
            "Federacija" -> {
                viewModel.canton.value = KarikaConstants.cantons("Federacija")
                viewModel.city.value = emptyList()
                viewModel.municipality.value = emptyList()
                viewModel.companyMunicipality.value = ""
            }

            "Republika Srpska" -> {
                municipality.value = ""
                viewModel.municipality.value = KarikaConstants.cantons("Republika Srpska")
                viewModel.canton.value = emptyList()
                viewModel.city.value = emptyList()
                viewModel.companyCanton.value = ""
                viewModel.companyCity.value = ""
            }

            "Distrikt Brčko" -> {
                municipality.value = ""
                viewModel.municipality.value = KarikaConstants.cantons("Distrikt Brčko")
                viewModel.canton.value = emptyList()
                viewModel.city.value = emptyList()
                viewModel.companyCanton.value = ""
                viewModel.companyCity.value = ""
            }
        }
    }
    LaunchedEffect(canton.value) {
        viewModel.companyCity.value = ""
        viewModel.city.value = KarikaConstants.cities(canton.value)
    }
}
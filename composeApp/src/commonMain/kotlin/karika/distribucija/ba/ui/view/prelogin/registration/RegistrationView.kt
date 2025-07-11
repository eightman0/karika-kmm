package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaBox
import karika.distribucija.ba.ui.components.KarikaCheckbox
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaPicker
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
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
                    .background(color = KarikaColors.Primary)
            )
        }
        KarikaScaffold(
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = {
                TopBarWithBack("Registracija kupca") {
                    component.back()
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
                    PrimaryButtonFilled(
                        title = "Prijavi se",
                    ) {
                        component.register()
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyInfo(viewModel: RegistrationComponent) {
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
        value = viewModel.companyName.asState(),
        placeholder = "Naziv pravnog lica",
        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "ID broj*",
        value = viewModel.companyId.asState(),
        placeholder = "ID broj",
        keyboardType = KeyboardType.Number,
        allowedChars = KarikaConstants.numbers,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "PDV broj",
        value = viewModel.companyPdv.asState(),
        placeholder = "PDV broj",
        allowedChars = KarikaConstants.numbers,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    )
    CompanyAddress(viewModel)
    KarikaPicker(
        title = "Veličina objekta*",
        placeholder = "Veličina objekta*",
        value = viewModel.companySize.asState(),
        values = mutableStateOf(KarikaConstants.companySizes).asState()
    )
    KarikaPicker(
        title = "Tip objekta*",
        placeholder = "Tip objekta",
        value = viewModel.companyType.asState(),
        values = mutableStateOf(KarikaConstants.companyTypes).asState()
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Broj zaposlenih",
        value = viewModel.companyEmployees.asState(),
        placeholder = "Broj zaposlenih",
        allowedChars = KarikaConstants.numbers,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    )
}

@Composable
private fun ContactInfo(viewModel: RegistrationComponent) {
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
        value = viewModel.contactFirstname.asState(),
        placeholder = "Ime",
        allowedChars = KarikaConstants.lettersSpace,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Prezime*",
        value = viewModel.contactLastname.asState(),
        placeholder = "Prezime",
        allowedChars = KarikaConstants.lettersSpace,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Adresa i broj ulice*",
        value = viewModel.contactAddress.asState(),
        placeholder = "Adresa i broj ulice",
        allowedChars = KarikaConstants.numbersAndLettersSpace,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Poštanski broj*",
        value = viewModel.contactPostal.asState(),
        placeholder = "Poštanski broj",
        allowedChars = KarikaConstants.numbers,
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Broj telefona*",
        value = viewModel.contactPhone.asState(),
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
            withStyle(
                style = SpanStyle(
                    fontWeight = FontWeight.W400,
                    color = KarikaColors.Gray4,
                    fontSize = 14.sp
                )
            ) {
                append("i")
            }
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
            "Izaberite entitet" -> {
                viewModel.canton.value = emptyList()
                viewModel.city.value = emptyList()
                viewModel.municipality.value = emptyList()
            }

            "Federacija" -> {
                viewModel.canton.value = KarikaConstants.cantons("Federacija")
                viewModel.city.value = emptyList()
                viewModel.municipality.value = emptyList()
            }

            "Republika Srpska" -> {
                municipality.value = ""
                viewModel.municipality.value = KarikaConstants.cantons("Republika Srpska")
                viewModel.canton.value = emptyList()
                viewModel.city.value = emptyList()
            }

            "Distrikt Brčko" -> {
                municipality.value = ""
                viewModel.municipality.value = KarikaConstants.cantons("Distrikt Brčko")
                viewModel.canton.value = emptyList()
                viewModel.city.value = emptyList()
            }
        }
    }
    LaunchedEffect(canton.value) {
        when (entity.value) {
            "Izaberite Kanton" -> {
                viewModel.city.value = emptyList()
            }

            else -> {
                city.value = ""
                viewModel.city.value = KarikaConstants.cities(canton.value)
            }
        }
    }
}
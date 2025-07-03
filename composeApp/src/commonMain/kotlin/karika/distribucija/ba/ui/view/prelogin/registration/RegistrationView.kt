package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaBox
import karika.distribucija.ba.ui.components.KarikaCheckbox
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState


@Composable
fun RegistrationView(viewModel: RegistrationViewModel) {
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
            hostState = viewModel.snackbarHostState,
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            topBar = { TopBarWithBack(viewModel) },
            viewModel = viewModel
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
                        .verticalScroll(rememberScrollState())
                        .padding(it),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    CompanyInfo(viewModel)
                    YSpacer16()
                    ContactInfo(viewModel)
                    YSpacer16()
                    LoginInfo(viewModel)
                    YSpacer16()
                    PrimaryButtonFilled(
                        title = "Prijavi se",
                    ) {
                        viewModel.register()
                    }
                }
            }
        }
    }
}

@Composable
private fun CompanyInfo(viewModel: RegistrationViewModel) {
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
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "ID broj*",
        value = viewModel.companyId.asState(),
        placeholder = "ID broj"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "PDV broj",
        value = viewModel.companyPdv.asState(),
        placeholder = "PDV broj"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Entitet*",
        value = viewModel.companyEntity.asState(),
        placeholder = "Entitet"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Veličina objekta*",
        value = viewModel.companySize.asState(),
        placeholder = "Veličina objekta"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Tip objekta*",
        value = viewModel.companyType.asState(),
        placeholder = "Tip objekta"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Broj zaposlenih",
        value = viewModel.companyEmployees.asState(),
        placeholder = "Broj zaposlenih"
    )
}

@Composable
private fun ContactInfo(viewModel: RegistrationViewModel) {
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
        placeholder = "Ime"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Prezime*",
        value = viewModel.contactLastname.asState(),
        placeholder = "Prezime"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Adresa i broj ulice*",
        value = viewModel.contactAddress.asState(),
        placeholder = "Adresa i broj ulice"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Poštanski broj*",
        value = viewModel.contactPostal.asState(),
        placeholder = "Poštanski broj"
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Broj telefona*",
        value = viewModel.contactPhone.asState(),
        placeholder = "Broj telefona"
    )
}

@Composable
private fun LoginInfo(viewModel: RegistrationViewModel) {
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
        placeholder = "Email adresa"
    )
    KarikaPasswordTextField(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Šifra*",
        value = viewModel.password.asState(),
        placeholder = "Šifra"
    )
    KarikaPasswordTextField(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Potvrdi šifru*",
        value = viewModel.confirmPassword.asState(),
        placeholder = "Potvrdi šifru"
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
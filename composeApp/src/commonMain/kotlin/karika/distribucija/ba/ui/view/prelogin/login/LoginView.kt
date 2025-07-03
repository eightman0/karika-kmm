package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.Screen
import karika.distribucija.ba.ui.components.KarikaBox
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLogo
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaSwitch
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.onClick


@Composable
fun LoginView(viewModel: LoginViewModel) {
    KarikaBox {
        KarikaScaffold(
            hostState = viewModel.snackbarHostState,
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
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
                        .padding(it),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaLogo()
                    KarikaText(
                        text = "Prijava kupac",
                        color = KarikaColors.Black,
                        fontWeight = FontWeight.W700,
                        textSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    KarikaTextField1(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "Email Adresa",
                        value = viewModel.email.asState(),
                        placeholder = "Email Adresa"
                    )
                    KarikaPasswordTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "Šifra",
                        value = viewModel.pass.asState(),
                        placeholder = "Šifra"
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaSwitch(
                            title = "Zapamti me"
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        KarikaText(
                            modifier = Modifier
                                .onClick {
                                    viewModel.forgotPassword()
                                },
                            text = "Zaboravili ste šifru?",
                            color = KarikaColors.Gray4,
                            fontWeight = FontWeight.W400,
                            textSize = 14.sp,
                            decoration = TextDecoration.Underline
                        )
                    }
                    YSpacer16()
                    PrimaryButtonFilled(
                        title = "Prijavi se"
                    ) {
                        viewModel.login()
                    }
                    KarikaText(
                        modifier = Modifier
                            .onClick {
                                viewModel.navigate(Screen.Registration)
                            }
                            .fillMaxWidth(),
                        atext = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W400,
                                    color = KarikaColors.Gray4,
                                    fontSize = 14.sp
                                )
                            ) {
                                append("Nemate kreiran račun? ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W400,
                                    color = KarikaColors.Blue,
                                    fontSize = 16.sp,
                                    textDecoration = TextDecoration.Underline
                                )
                            ) {
                                append("Registrujte se ovdje.")
                            }
                        }
                    )
                    YSpacer16()
                }
            }
        }
    }
}

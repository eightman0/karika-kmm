package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent
import karika.distribucija.ba.ui.view.prelogin.login.component.ForgotPasswordSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun GuestUserInfoDialog(
    component: CommonComponent
) {
    val showState = component.stateHolder.commonHandler.showLoginRequired.asState()

    if (showState.value != null) {
        Dialog(
            onDismissRequest = {
                showState.value = null
            },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .rounded(color = KarikaColors.Transparent)
                    .clip(RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                InternalLoginView(
                    LoginComponent(
                        componentContext = component,
                        stateHolder = component.stateHolder,
                        userType = KarikaType.SHOP
                    ),
                    showState
                )
            }
        }
    }
}

@Composable
private fun InternalLoginView(component: LoginComponent, showState: MutableState<String?>) {
    val emailValid = remember { mutableStateOf("") }
    val formValid = component.formValid.asState()
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White, shape = RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(
                        WindowInsets.ime
                            .union(WindowInsets.navigationBars)
                            .only(WindowInsetsSides.Bottom)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        modifier = Modifier
                            .onClick {
                                showState.value = null
                                component.logout()
                            },
                        imageVector = vectorResource(Res.drawable.ic_arrow_back),
                        tint = KarikaColors.Primary,
                        contentDescription = ""
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        modifier = Modifier
                            .size(48.dp)
                            .onClick {
                                showState.value = null
                            },
                        imageVector = vectorResource(Res.drawable.ic_tertiary),
                        tint = KarikaColors.Gray2,
                        contentDescription = ""
                    )
                }
                KarikaLogo {
                    component.exitKiosk()
                }
                KarikaText(
                    text = component.title(),
                    color = KarikaColors.Black,
                    fontWeight = FontWeight.W700,
                    textSize = 24.sp,
                    textAlign = TextAlign.Center
                )
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Email Adresa",
                    value = component.email.asState(),
                    placeholder = "Email Adresa",
                    error = emailValid,
                    onValueChange = {
                        emailValid.value = if (component.email.value.isEmailFormat()) {
                            formValid.value = component.pass.value.isNotEmpty()
                            ""
                        } else {
                            "Unesite valjanu email adresu (npr. johndoe@domain.com)."
                        }
                    },
                    imeAction = ImeAction.Next
                )
                KarikaPasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Šifra",
                    value = component.pass.asState(),
                    placeholder = "Šifra",
                    onValueChange = {
                        formValid.value =
                            component.pass.value.isNotEmpty() && component.email.value.isEmailFormat()
                    },
                    imeAction = ImeAction.Done,
                    doneAction = {}
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaSwitch(
                        title = "Zapamti me",
                        checked = component.rememberMe.asState()
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    KarikaText(
                        modifier = Modifier
                            .onClick {
                                component.forgotPassword()
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
                    title = "Prijavi se",
                    enabled = formValid.value
                ) {
                    component.login {
                        showState.value = null
                        component.stateHolder.customerSpecificHandler.getUserDetails()
                        component.stateHolder.cartHandler.reloadCart()
                        component.stateHolder.customerNotificationHandler.notificationReceived()
                    }
                }
                KarikaText(
                    modifier = Modifier
                        .onClick {
                            showState.value = null
                            component.stateHolder.appNavigation.replaceAll(
                                AppConfig.PreLogin(PreLoginConfig.Registration(KarikaType.SHOP))
                            )
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
                KarikaText(
                    modifier = Modifier,
                    text = showState.value,
                    color = KarikaColors.Gray2,
                    fontWeight = FontWeight.W700,
                    textSize = 16.sp,
                )
                YSpacer16()
            }
            ForgotPasswordSheet(component)
        }
    }
}
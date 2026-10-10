package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import karika.distribucija.ba.ui.view.prelogin.login.LoginField
import karika.distribucija.ba.ui.view.prelogin.login.component.ForgotPasswordSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_eye
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import org.jetbrains.compose.resources.vectorResource

/** Test tags of the guest login sheet's fields, for the end-to-end tests. */
const val GUEST_LOGIN_EMAIL_FIELD_TAG = "guest_login_email"
const val GUEST_LOGIN_PASSWORD_FIELD_TAG = "guest_login_password"

/** Login sheet shown when a guest tries something that needs an account. */
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
                    .fillMaxSize()
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        showState.value = null
                    },
                contentAlignment = Alignment.BottomCenter
            ) {
                val loginComponent = remember {
                    LoginComponent(
                        componentContext = component,
                        stateHolder = component.stateHolder,
                        userType = KarikaType.SHOP
                    )
                }
                InternalLoginView(loginComponent, showState)
            }
        }
    }
}

@Composable
private fun InternalLoginView(component: LoginComponent, showState: MutableState<String?>) {
    val emailValid = remember { mutableStateOf("") }
    val formValid = component.formValid.asState()
    val passwordVisible = remember { mutableStateOf(false) }
    // Reasons come as "*Potrebna registracija za ..."; the asterisk was the old form's marker
    val reason = showState.value.orEmpty().removePrefix("*").trim()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(KarikaColors.White)
            // Swallow taps, so only the dimmed area around the sheet closes it
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {}
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
            .verticalScroll(rememberScrollState())
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(50))
                .background(KarikaUiColors.Border)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KLogo(width = 40.dp)
            Spacer(Modifier.weight(1f))
            KCircleButton(
                icon = vectorResource(Res.drawable.ic_k_close),
                size = 38.dp,
                iconSize = 18.dp
            ) {
                showState.value = null
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            KarikaText(
                text = "Prijava",
                color = KarikaUiColors.Ink,
                fontWeight = FontWeight.W800,
                textSize = 24.sp,
                lineHeight = 30.sp,
                maxLines = 1
            )
            Spacer(Modifier.width(8.dp))
            KPill(text = "Kupac", background = KarikaUiColors.PinkSoft, color = KarikaUiColors.Pink)
        }
        Spacer(Modifier.height(10.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(KarikaUiColors.PinkSoft)
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_lock), tint = KarikaUiColors.Pink, size = 18.dp)
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = reason.ifEmpty { "Prijavite se da biste nastavili." },
                color = KarikaUiColors.Ink,
                textSize = 13.5.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W500
            )
        }
        Spacer(Modifier.height(18.dp))

        KFieldLabel(text = "Email adresa")
        LoginField(
            tag = GUEST_LOGIN_EMAIL_FIELD_TAG,
            value = component.email.value,
            onValueChange = { value ->
                component.email.value = value
                emailValid.value = if (value.isEmailFormat()) {
                    formValid.value = component.pass.value.isNotEmpty()
                    ""
                } else {
                    "Unesite valjanu email adresu (npr. johndoe@domain.com)."
                }
            },
            placeholder = "Email adresa",
            leadingIcon = vectorResource(Res.drawable.ic_k_mail),
            isError = emailValid.value.isNotEmpty(),
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        if (emailValid.value.isNotEmpty()) {
            Spacer(Modifier.height(6.dp))
            KarikaText(
                text = emailValid.value,
                color = KarikaUiColors.Red,
                textSize = 12.sp,
                lineHeight = 16.sp
            )
        }
        Spacer(Modifier.height(14.dp))
        KFieldLabel(text = "Šifra")
        LoginField(
            tag = GUEST_LOGIN_PASSWORD_FIELD_TAG,
            value = component.pass.value,
            onValueChange = { value ->
                component.pass.value = value
                formValid.value = value.isNotEmpty() && component.email.value.isEmailFormat()
            },
            placeholder = "Šifra",
            leadingIcon = vectorResource(Res.drawable.ic_k_lock),
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
            visualTransformation = if (passwordVisible.value) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            trailing = {
                KIcon(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable { passwordVisible.value = !passwordVisible.value },
                    icon = vectorResource(Res.drawable.ic_k_eye),
                    tint = if (passwordVisible.value) KarikaUiColors.Pink else KarikaUiColors.Muted,
                    size = 20.dp
                )
            }
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KToggle(checked = component.rememberMe.value) {
                component.rememberMe.value = it
            }
            Spacer(Modifier.width(10.dp))
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Zapamti me",
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1
            )
            KarikaText(
                modifier = Modifier.clickable { component.forgotPassword() },
                text = "Zaboravili ste šifru?",
                color = KarikaUiColors.Pink,
                textSize = 13.sp,
                fontWeight = FontWeight.W600,
                maxLines = 1
            )
        }
        Spacer(Modifier.height(20.dp))
        KPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Prijavi se",
            enabled = formValid.value
        ) {
            component.login {
                showState.value = null
                component.stateHolder.customerSpecificHandler.getUserDetails()
                component.stateHolder.cartHandler.reloadCart()
                component.stateHolder.customerNotificationHandler.notificationReceived()
            }
        }
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            KarikaText(
                text = "Nemate račun? ",
                color = KarikaUiColors.Muted,
                textSize = 14.sp
            )
            KarikaText(
                modifier = Modifier.clickable {
                    showState.value = null
                    component.stateHolder.appNavigation.replaceAll(
                        AppConfig.PreLogin(PreLoginConfig.Registration(KarikaType.SHOP))
                    )
                },
                text = "Registrujte se",
                color = KarikaUiColors.Pink,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        Spacer(Modifier.height(10.dp))
        KarikaText(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    showState.value = null
                    component.logout()
                }
                .padding(vertical = 6.dp),
            text = "Izađi iz režima gosta",
            color = KarikaUiColors.Muted,
            textSize = 13.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center
        )
    }
    ForgotPasswordSheet(component)
}

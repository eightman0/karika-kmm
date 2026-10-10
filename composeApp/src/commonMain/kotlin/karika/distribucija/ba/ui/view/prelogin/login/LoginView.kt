package karika.distribucija.ba.ui.view.prelogin.login

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.text
import androidx.compose.ui.semantics.toggleableState
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.components.KBackButton
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KToggle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KLogo
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.isEmailFormat
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.view.prelogin.login.component.ForgotPasswordSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_eye
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_network_manage
import org.jetbrains.compose.resources.vectorResource

/** Test tag of the login's email field, for the end-to-end tests. */
const val LOGIN_EMAIL_FIELD_TAG = "login_email"

/** Test tag of the login's password field, for the end-to-end tests. */
const val LOGIN_PASSWORD_FIELD_TAG = "login_password"

@Composable
fun LoginView(component: LoginComponent) {
    val emailValid = remember { mutableStateOf("") }
    val formValid = component.formValid.asState()
    val passwordVisible = remember { mutableStateOf(false) }
    val accent = if (component.isShop()) KarikaUiColors.Pink else KarikaUiColors.Navy

    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        component = component
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(
                        WindowInsets.ime
                            .exclude(WindowInsets.navigationBars)
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
                    // White header: back (or Wi-Fi on a kiosk), logo, title with the account type
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(KarikaHeaderShape)
                            .background(KarikaColors.White)
                            .windowInsetsPadding(WindowInsets.statusBars)
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 24.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isKiosk()) {
                                KBackButton(modifier = Modifier.offset(x = (-4).dp)) {
                                    component.navigateLanding()
                                }
                            }
                            Spacer(Modifier.weight(1f))
                            if (isKiosk()) {
                                KCircleButton(
                                    icon = vectorResource(Res.drawable.ic_network_manage),
                                    tint = KarikaUiColors.Pink
                                ) {
                                    component.wifi()
                                }
                            }
                        }
                        Spacer(Modifier.height(22.dp))
                        KLogo(width = 52.dp)
                        Spacer(Modifier.height(20.dp))
                        // Read as one text ("Prijava kupac" / "Prijava dobavljač") by accessibility and tests
                        Row(
                            modifier = Modifier.clearAndSetSemantics {
                                text = AnnotatedString(component.title())
                                heading()
                            },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KarikaText(
                                text = "Prijava",
                                color = KarikaUiColors.Ink,
                                fontWeight = FontWeight.W800,
                                textSize = 28.sp,
                                lineHeight = 34.sp,
                                maxLines = 1
                            )
                            Spacer(Modifier.width(8.dp))
                            if (component.isShop()) {
                                KPill(
                                    text = "Kupac",
                                    background = KarikaUiColors.PinkSoft,
                                    color = KarikaUiColors.Pink
                                )
                            } else {
                                KPill(
                                    text = "Dobavljač",
                                    background = KarikaUiColors.Field,
                                    color = KarikaUiColors.Navy
                                )
                            }
                        }
                        Spacer(Modifier.height(6.dp))
                        KarikaText(
                            text = "Dobrodošli nazad. Prijavite se na svoj račun.",
                            color = KarikaUiColors.Muted,
                            textSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    KCard(
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Column {
                                KFieldLabel(text = "Email adresa")
                                LoginField(
                                    tag = LOGIN_EMAIL_FIELD_TAG,
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
                            }
                            Column {
                                KFieldLabel(text = "Šifra")
                                LoginField(
                                    tag = LOGIN_PASSWORD_FIELD_TAG,
                                    value = component.pass.value,
                                    onValueChange = { value ->
                                        component.pass.value = value
                                        formValid.value =
                                            value.isNotEmpty() && component.email.value.isEmailFormat()
                                    },
                                    placeholder = "Šifra",
                                    leadingIcon = vectorResource(Res.drawable.ic_k_lock),
                                    keyboardType = KeyboardType.Password,
                                    imeAction = ImeAction.Done,
                                    onDone = { component.login() },
                                    visualTransformation = if (passwordVisible.value) {
                                        VisualTransformation.None
                                    } else {
                                        PasswordVisualTransformation()
                                    },
                                    trailing = {
                                        KIcon(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .clickable {
                                                    passwordVisible.value = !passwordVisible.value
                                                },
                                            icon = vectorResource(Res.drawable.ic_k_eye),
                                            tint = if (passwordVisible.value) KarikaUiColors.Pink else KarikaUiColors.Muted,
                                            size = 20.dp
                                        )
                                    }
                                )
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                KToggle(
                                    modifier = Modifier.semantics {
                                        toggleableState = ToggleableState(component.rememberMe.value)
                                        role = Role.Switch
                                    },
                                    checked = component.rememberMe.value
                                ) {
                                    component.rememberMe.value = it
                                }
                                Spacer(Modifier.width(10.dp))
                                KarikaText(
                                    modifier = Modifier.clickable {
                                        component.rememberMe.value = !component.rememberMe.value
                                    },
                                    text = "Zapamti me",
                                    color = KarikaUiColors.Ink,
                                    fontWeight = FontWeight.W500,
                                    textSize = 14.sp,
                                    maxLines = 1
                                )
                                Spacer(Modifier.weight(1f))
                                KarikaText(
                                    modifier = Modifier
                                        .clickable {
                                            component.forgotPassword()
                                        }
                                        .padding(vertical = 4.dp),
                                    text = "Zaboravili ste šifru?",
                                    color = KarikaUiColors.Pink,
                                    fontWeight = FontWeight.W600,
                                    textSize = 13.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                KBottomPanel {
                    KPrimaryButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = "Prijavi se",
                        enabled = formValid.value,
                        background = accent
                    ) {
                        component.login()
                    }
                    if (!isKiosk()) {
                        Spacer(Modifier.height(14.dp))
                        KarikaText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    component.navigateRegistration()
                                },
                            atext = buildAnnotatedString {
                                append("Nemate račun? ")
                                withStyle(
                                    style = SpanStyle(
                                        fontWeight = FontWeight.W700,
                                        color = KarikaUiColors.Pink
                                    )
                                ) {
                                    append("Registrujte se")
                                }
                            },
                            color = KarikaUiColors.Muted,
                            textSize = 14.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }

            ForgotPasswordSheet(component)
        }
    }
}

/** Gray, borderless login field with a leading icon, as in the design's login form. */
@Composable
internal fun LoginField(
    tag: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    leadingIcon: ImageVector,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    isError: Boolean = false,
    onDone: () -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    trailing: (@Composable () -> Unit)? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(if (isError) KarikaUiColors.RedSoft else KarikaUiColors.Field)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = leadingIcon, tint = KarikaUiColors.Muted, size = 18.dp)
        Spacer(Modifier.width(10.dp))
        Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
            if (value.isEmpty()) {
                KarikaText(text = placeholder, color = KarikaUiColors.Subtle, textSize = 15.sp, maxLines = 1)
            }
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(tag),
                value = value,
                onValueChange = onValueChange,
                singleLine = true,
                textStyle = TextStyle(
                    color = KarikaUiColors.Ink,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.W500,
                    fontFamily = karikaFonts()
                ),
                cursorBrush = SolidColor(KarikaUiColors.Pink),
                visualTransformation = visualTransformation,
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = imeAction),
                keyboardActions = KeyboardActions(onDone = { onDone() })
            )
        }
        if (trailing != null) {
            Spacer(Modifier.width(8.dp))
            trailing()
        }
    }
}

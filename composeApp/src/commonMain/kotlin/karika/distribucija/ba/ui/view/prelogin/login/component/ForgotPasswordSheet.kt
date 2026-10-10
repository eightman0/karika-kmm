package karika.distribucija.ba.ui.view.prelogin.login.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.isEmailFormat
import karika.distribucija.ba.ui.components.karikaFonts
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_lock
import karikav2.composeapp.generated.resources.ic_k_mail
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordSheet(
    component: LoginComponent
) {
    val showState = component.forgotPassSheet.asState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val email = remember { mutableStateOf("") }
    val keyboard = LocalSoftwareKeyboardController.current
    // Pink for customers, the suppliers' blue for suppliers, as on the login screen
    val accent = if (component.isShop()) KarikaUiColors.Pink else KarikaUiColors.Blue
    val accentSoft = if (component.isShop()) KarikaUiColors.PinkSoft else KarikaUiColors.BlueSoft
    val emailValid = email.value.isEmailFormat()

    if (showState.value) {
        ModalBottomSheet(
            onDismissRequest = {
                showState.negate()
            },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            dragHandle = {
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp, bottom = 6.dp)
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(50))
                        .background(KarikaUiColors.Border)
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .hideKeyboard()
                    .navigationBarsPadding()
                    .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_lock), tint = accent, size = 24.dp)
                }
                Spacer(Modifier.height(16.dp))
                KarikaText(
                    text = "Zaboravili ste šifru?",
                    color = KarikaUiColors.Ink,
                    textSize = 22.sp,
                    lineHeight = 28.sp,
                    fontWeight = FontWeight.W800
                )
                Spacer(Modifier.height(6.dp))
                KarikaText(
                    text = "Unesite vašu email adresu da resetujete vašu lozinku.",
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    lineHeight = 20.sp
                )
                Spacer(Modifier.height(20.dp))
                KFieldLabel(text = "Email adresa")
                BasicTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = email.value,
                    onValueChange = { email.value = it.trim() },
                    singleLine = true,
                    textStyle = TextStyle(
                        color = KarikaUiColors.Ink,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.W500,
                        fontFamily = karikaFonts()
                    ),
                    cursorBrush = SolidColor(accent),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(onDone = { keyboard?.hide() }),
                    // The placeholder sits inside the text field, so tests find the field by it
                    decorationBox = { innerTextField ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(KarikaUiColors.Field)
                                .padding(horizontal = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            KIcon(
                                icon = vectorResource(Res.drawable.ic_k_mail),
                                tint = KarikaUiColors.Muted,
                                size = 18.dp
                            )
                            Spacer(Modifier.width(10.dp))
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                                if (email.value.isEmpty()) {
                                    KarikaText(
                                        text = "Unesite email",
                                        color = KarikaUiColors.Subtle,
                                        textSize = 15.sp,
                                        maxLines = 1
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
                if (email.value.isNotEmpty() && !emailValid) {
                    Spacer(Modifier.height(6.dp))
                    KarikaText(
                        text = "Unesite valjanu email adresu (npr. johndoe@domain.com).",
                        color = KarikaUiColors.Red,
                        textSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KSecondaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Zatvori"
                    ) {
                        keyboard?.hide()
                        showState.negate()
                    }
                    KPrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Potvrdi",
                        background = accent,
                        enabled = emailValid
                    ) {
                        keyboard?.hide()
                        showState.negate()
                        component.forgotPassword(email.value)
                    }
                }
            }
        }
    }
}

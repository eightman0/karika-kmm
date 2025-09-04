package karika.distribucija.ba.ui.view.prelogin.login.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordSheet(
    component: LoginComponent
) {
    val showState = component.forgotPassSheet.asState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val email = mutableStateOf("").asState()
    val keyboard = LocalSoftwareKeyboardController.current

    if (showState.value) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 100.dp),
            onDismissRequest = {
                showState.negate()
            },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = KarikaColors.Gray2,
                    width = 60.dp
                )
            }
        ) {
            Column(
                modifier = Modifier
                    .hideKeyboard()
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = "Zaboravili ste šifru?",
                    color = KarikaColors.Gray2,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center
                )
                YSpacer16()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = KarikaColors.Divider
                )
                YSpacer32()
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = "Unesite vašu email adresu da resetujete vašu lozinku.",
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
                YSpacer8()
                KarikaTextField2(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    value = email,
                    placeholder = "Unesite email",
                    imeAction = ImeAction.Done,
                    keyboardType = KeyboardType.Email
                )
                YSpacer32()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = KarikaColors.Divider
                )
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryButton(
                        modifier = Modifier
                            .weight(1f),
                        title = "Zatvori",
                        textSize = 16.sp
                    ) {
                        keyboard?.hide()
                        showState.negate()
                    }
                    PrimaryButtonFilled(
                        modifier = Modifier
                            .weight(1f),
                        title = "Potvrdi"
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
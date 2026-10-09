package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(onCancel: () -> Unit, onChange: (String, String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pass = mutableStateOf("").asState()
    val newPass = mutableStateOf("").asState()
    val keyboard = LocalSoftwareKeyboardController.current
    val error = mutableStateOf("").asState()
    val enabled = mutableStateOf(false).asState()
    ModalBottomSheet(
        modifier = Modifier
            .padding(top = 100.dp),
        onDismissRequest = {
            onCancel()
        },
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = KarikaUiColors.Border,
                width = 40.dp,
                height = 4.dp
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
                text = "Promijeni lozinku",
                color = KarikaUiColors.Ink,
                textSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Start
            )
            YSpacer16()
            KDivider()
            YSpacer16()
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Unesite staru lozinku",
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600
                )
                YSpacer8()
                KarikaPasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = pass,
                    placeholder = "Stara lozinku",
                    imeAction = ImeAction.Next,
                    onValueChange = {
                        enabled.value = pass.value.isNotEmpty() &&
                                newPass.value.length >= 8 &&
                                newPass.value.isPassComplex()
                    }
                )
                YSpacer16()
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Unesite novu lozinku",
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W600
                )
                YSpacer8()
                KarikaPasswordTextField(
                    modifier = Modifier
                        .fillMaxWidth(),
                    value = newPass,
                    placeholder = "Nova lozinku",
                    imeAction = ImeAction.Done,
                    error = error,
                    onValueChange = {
                        enabled.value = false
                        if (newPass.value.length < 8) {
                            error.value = "Lozinka mora imati najmanje 8 karaktera."
                            return@KarikaPasswordTextField
                        }
                        if (!newPass.value.isPassComplex()) {
                            error.value =
                                "Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj."
                            return@KarikaPasswordTextField
                        }
                        error.value = ""
                        enabled.value = pass.value.isNotEmpty() &&
                                newPass.value.length >= 8 &&
                                newPass.value.isPassComplex()
                    }
                )
            }
            YSpacer16()
            KDivider()
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KSecondaryButton(
                    modifier = Modifier
                        .weight(1f),
                    text = "Zatvori"
                ) {
                    keyboard?.hide()
                    onCancel()
                }
                KPrimaryButton(
                    modifier = Modifier
                        .weight(1f),
                    text = "Potvrdi",
                    enabled = enabled.value
                ) {
                    keyboard?.hide()
                    onChange(pass.value, newPass.value)
                }
            }
        }
    }
}

fun String.isPassComplex(): Boolean {
    val passwordRegex = "^(?=.*[A-Z])(?=.*[0-9]).+\$".toRegex()
    return this.matches(passwordRegex)
}
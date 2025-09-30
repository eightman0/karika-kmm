package karika.distribucija.ba.ui.view.distributer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KarikaCheckboxSecondary
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaPasswordTextField
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer24
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.products.details.dashedBorder
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_delete
import karikav2.composeapp.generated.resources.ic_photo
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProfileView(component: ProfileComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
    )
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CompanyInfo(component)
        Images(component)
        ButtonsBox(component)
    }
}

@Composable
private fun ButtonsBox(component: ProfileComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth()
    ) {
        HorizontalSecondaryButtons(
            modifier = Modifier
                .padding(16.dp),
            primaryTitle = "Spasi izmjene",
            secondaryTitle = "Nazad"
        ) {
            if (it == "Nazad") {
                component.dashBack()
                return@HorizontalSecondaryButtons
            }

            component.updateProfile()
        }
    }
}

@Composable
private fun CompanyInfo(component: ProfileComponent) {
    val customerGroups = component.customerGroups.asState()
    val customerRegions = component.customerRegions.asState()
    val companyCity = component.companyCity.asState()
    val companyCanton = component.companyCanton.asState()
    val companyMunicipality = component.companyMunicipality.asState()
    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        KarikaText(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            text = "Opste informacije",
            color = KarikaColors.Black,
            fontWeight = FontWeight.W600,
            textSize = 18.sp
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Naziv pravnog lica*",
            value = component.companyName.asState(),
            placeholder = "Naziv pravnog lica",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            enabled = false
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Entitet*",
            value = component.companyEntity.asState(),
            placeholder = "Entitet",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Next,
            enabled = false
        )
        if (companyCity.value.isNotEmpty()) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Grad*",
                value = component.companyCity.asState(),
                placeholder = "Grad",
                allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                enabled = false
            )
        }
        if (companyCanton.value.isNotEmpty()) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Kanton*",
                value = component.companyCanton.asState(),
                placeholder = "Kanton",
                allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                enabled = false
            )
        }
        if (companyMunicipality.value.isNotEmpty()) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Opština*",
                value = component.companyMunicipality.asState(),
                placeholder = "Opština",
                allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                enabled = false
            )
        }
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Ciljana grupa kupaca",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        component.stateHolder.commonHandler.config.value.customerGroupList.chunked(2).forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                it.forEach { it1 ->
                    KarikaCheckboxSecondary(
                        modifier = Modifier.weight(1f),
                        title = it1.label(),
                        value = customerGroups.value.any { c -> c.unit == it1.unit() }
                    ) {
                        if (customerGroups.value.any { c -> c.unit == it1.unit() }) {
                            customerGroups.value -= it1
                        } else {
                            customerGroups.value += it1
                        }
                    }
                }
            }
        }
        YSpacer8()
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Ciljani region kupaca",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        component.stateHolder.commonHandler.config.value.customerRegionList.chunked(2).forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                it.forEach { it1 ->
                    KarikaCheckboxSecondary(
                        modifier = Modifier.weight(1f),
                        title = it1.label(),
                        value = customerRegions.value.any { c -> c.unit == it1.unit() }
                    ) {
                        if (customerRegions.value.any { c -> c.unit == it1.unit() }) {
                            customerRegions.value -= it1
                        } else {
                            customerRegions.value += it1
                        }
                    }
                }
            }
        }
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "PDV broj",
            value = component.companyPdv.asState(),
            placeholder = "PDV broj",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            enabled = false
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "ID broj*",
            value = component.companyId.asState(),
            placeholder = "ID broj",
            keyboardType = KeyboardType.Number,
            allowedChars = KarikaConstants.numbers,
            imeAction = ImeAction.Next,
            enabled = false
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj telefona*",
            value = component.companyPhone.asState(),
            placeholder = "Broj telefona",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Minimalna vrijednost narudžbe",
            value = component.minOrderAmount.asState(),
            placeholder = "Minimalna vrijednost narudžbe",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj računa",
            value = component.bankAccountNumber.asState(),
            placeholder = "Broj računa",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Ime kontakta*",
            value = component.contactName.asState(),
            placeholder = "Ime kontakta",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    component.changePassSheet.negate()
                },
            text = "Promijeni lozinku",
            color = KarikaColors.Blue,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
    ChangePasswordSheet(component)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordSheet(
    component: ProfileComponent
) {
    val showState = component.changePassSheet.asState()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pass = mutableStateOf("").asState()
    val newPass = mutableStateOf("").asState()
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
                    text = "Promijeni lozinku",
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
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
                    KarikaText(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "Unesite staru lozinku",
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                    YSpacer8()
                    KarikaPasswordTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = pass,
                        placeholder = "Stara lozinku",
                        imeAction = ImeAction.Next
                    )
                    YSpacer32()
                    KarikaText(
                        modifier = Modifier
                            .fillMaxWidth(),
                        text = "Unesite novu lozinku",
                        color = KarikaColors.Gray2,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                    YSpacer8()
                    KarikaPasswordTextField(
                        modifier = Modifier
                            .fillMaxWidth(),
                        value = newPass,
                        placeholder = "Nova lozinku",
                        imeAction = ImeAction.Done,
                    )
                }
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
                        title = "Potvrdi",
                        enabled = newPass.value.isNotEmpty() && pass.value.isNotEmpty()
                    ) {
                        keyboard?.hide()
                        showState.negate()
                        component.changePass(pass.value, newPass.value)
                    }
                }
            }
        }
    }
}

@Composable
private fun Images(component: ProfileComponent) {
    val companyLogo = component.companyLogo.asState()
    val companyBanner = component.companyBanner.asState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        KarikaText(
            modifier = Modifier,
            text = "Logo kompanije",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        YSpacer8()
        Row(
            modifier = Modifier
                .background(color = KarikaColors.White)
                .border(width = 1.dp, color = KarikaColors.Gray21, shape = RoundedCornerShape(4.dp))
                .fillMaxWidth()
        ) {
            if (companyLogo.value.third == null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(120.dp)
                        .onClick {
                            component.pickImage(1)
                        }
                        .dashedBorder(color = KarikaColors.Blue2, cornerRadius = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(vertical = 16.dp),
                            imageVector = vectorResource(Res.drawable.ic_photo),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                        KarikaText(
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            text = "Dodaj sliku",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    KarikaImage(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = KarikaColors.Gray21,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(120.dp)
                            .onClick {
                                component.showImagePreview(
                                    companyLogo.value.third?.toString() ?: ""
                                )
                            },
                        model = companyLogo.value.third
                    )
                }
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .onClick {
                            companyLogo.value = Triple("", "", null)
                        },
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    tint = KarikaColors.Red,
                    contentDescription = ""
                )
            }
        }
        YSpacer24()
        KarikaText(
            modifier = Modifier,
            text = "Baner kompanije",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        YSpacer8()
        Row(
            modifier = Modifier
                .background(color = KarikaColors.White)
                .border(width = 1.dp, color = KarikaColors.Gray21, shape = RoundedCornerShape(4.dp))
                .fillMaxWidth()
        ) {
            if (companyBanner.value.third == null) {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .size(120.dp)
                        .onClick {
                            component.pickImage(2)
                        }
                        .dashedBorder(color = KarikaColors.Blue2, cornerRadius = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(vertical = 16.dp),
                            imageVector = vectorResource(Res.drawable.ic_photo),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                        KarikaText(
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            text = "Dodaj sliku",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .padding(8.dp)
                ) {
                    KarikaImage(
                        modifier = Modifier
                            .border(
                                width = 1.dp,
                                color = KarikaColors.Gray21,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .size(120.dp)
                            .onClick {
                                component.showImagePreview(
                                    companyBanner.value.third?.toString() ?: ""
                                )
                            }
                            .fillMaxSize(),
                        model = companyBanner.value.third
                    )
                }
                Icon(
                    modifier = Modifier
                        .padding(8.dp)
                        .onClick {
                            companyBanner.value = Triple("", "", null)
                        },
                    imageVector = vectorResource(Res.drawable.ic_delete),
                    tint = KarikaColors.Red,
                    contentDescription = ""
                )
            }
        }
    }
}
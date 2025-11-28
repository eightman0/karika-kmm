package karika.distribucija.ba.ui.view.distributer.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KarikaCheckboxSecondary
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.YSpacer24
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.distributer.products.details.dashedBorder
import karika.distribucija.ba.ui.view.main.profile.account.ChangePasswordSheet
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
            .hideKeyboard()
            .windowInsetsPadding(
                WindowInsets.ime
                    .union(WindowInsets.navigationBars)
                    .only(WindowInsetsSides.Bottom)
            )
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        CompanyInfo(component)
        Images(component)
        ButtonsBox(component)
    }
    DeleteAccountConfirmation(component)
    DeleteAccountConfirmation(component)
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
                component.dashNavigate(DashConfig.ControlBoard)
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
    val showState = component.changePassSheet.asState()
    val gridColumnCount = gridColumnCount()

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {

        KarikaText(
            modifier = Modifier
                .padding(top = 16.dp)
                .fillMaxWidth(),
            text = "Opšte informacije",
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
            enabled = false,
            disabledTextColor = KarikaColors.Gray2,
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
            enabled = false,
            disabledTextColor = KarikaColors.Gray2,
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
                enabled = false,
                disabledTextColor = KarikaColors.Gray2,
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
                enabled = false,
                disabledTextColor = KarikaColors.Gray2,
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
                enabled = false,
                disabledTextColor = KarikaColors.Gray2,
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
        component.stateHolder.commonHandler.config.value.customerGroupList.chunked(gridColumnCount).forEach {
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
        component.stateHolder.commonHandler.config.value.customerRegionList.chunked(gridColumnCount).forEach {
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
            enabled = false,
            disabledTextColor = KarikaColors.Gray2,
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
            enabled = false,
            disabledTextColor = KarikaColors.Gray2,
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
        KarikaText(
            modifier = Modifier
                .onClick {
                    component.deleteAccount.negate()
                },
            text = "Obriši nalog",
            color = KarikaColors.Red,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
    if (showState.value) {
        ChangePasswordSheet(
            onCancel = {
                showState.negate()
            },
            onChange = { old, new ->
                showState.negate()
                component.changePass(old, new)
            }
        )
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

@Composable
private fun DeleteAccountConfirmation(component: ProfileComponent) {
    val state = component.deleteAccount.asState()

    if (state.value) {
        Dialog(
            onDismissRequest = {},
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .rounded(shape = 16.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaText(
                        modifier = Modifier,
                        text = "Obriši nalog",
                        color = KarikaColors.Gray2,
                        textSize = 20.sp,
                        fontWeight = FontWeight.W600
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "Jeste li sigurni da želite obrisati nalog?",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.W600
                    )
                    HorizontalButtons(
                        modifier = Modifier,
                        primaryTitle = "Obriši nalog",
                        secondaryTitle = "Odustani"
                    ) {
                        if (it == "Odustani") {
                            state.negate()
                            return@HorizontalButtons
                        }
                        component.deleteAccount()
                    }
                }
            }
        }
    }
}
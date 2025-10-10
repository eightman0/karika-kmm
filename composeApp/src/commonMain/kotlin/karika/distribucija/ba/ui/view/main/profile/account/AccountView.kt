package karika.distribucija.ba.ui.view.main.profile.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.util.KarikaConstants

@Composable
fun AccountView(component: AccountComponent) {
    val editAddress by component.editAddress.asState()
    val editContact by component.editContact.asState()
    val showState = component.changePassSheet.asState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Moj nalog") {
                component.appBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
                .windowInsetsPadding(
                    WindowInsets.ime
                        .union(WindowInsets.navigationBars)
                        .only(WindowInsetsSides.Bottom)
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (editAddress.isNotEmpty()) {
                UpdateAddress(component)
            } else if (editContact) {
                UpdateContactInfo(component)
            } else {
                ContactInfo(component)
                BillingAddress(component)
                ShippingAddress(component)
            }
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
}

@Composable
private fun ContactInfo(component: AccountComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = Modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(color = KarikaColors.Gray12)
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                text = "Kontakt Informacije",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .onClick { component.edit("Kontakt informacije") }
                    .padding(16.dp),
                text = "Uredi",
                color = KarikaColors.Primary,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Ime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.firstname,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Prezime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.lastname,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Email",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.email,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    component.showChangePass()
                }
                .padding(16.dp),
            text = "Promijeni lozinku",
            color = KarikaColors.Primary,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
private fun BillingAddress(component: AccountComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = Modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(color = KarikaColors.Gray12)
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                text = "Informacije za naplatu",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .onClick { component.edit("Informacije za naplatu", false) }
                    .padding(16.dp),
                text = "Uredi",
                color = KarikaColors.Primary,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Naziv pravnog lica",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.companyName(),
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Adresa i broj ulice",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.street?.firstOrNull(),
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Broj telefona",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.telephone,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Grad",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.city,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Poštanski broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.postcode,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
private fun ShippingAddress(component: AccountComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = Modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .background(color = KarikaColors.Gray12)
                .fillMaxWidth()
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(1f),
                text = "Zadana adresa za dostavu",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .onClick { component.edit("Adresa za dostavu") }
                    .padding(16.dp),
                text = "Uredi",
                color = KarikaColors.Primary,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Ime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.firstname,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Prezime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.lastname,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Broj telefona",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.telephone,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Adresa i broj ulice",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.street?.firstOrNull(),
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Grad",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.city,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Poštanski broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.postcode,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
private fun UpdateAddress(component: AccountComponent) {
    var editAddress by component.editAddress.asState()
    val editableFields by component.editableFields.asState()

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = editAddress,
            color = KarikaColors.Black,
            textSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )

        if (editAddress == "Adresa za dostavu") {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Ime*",
                value = component.firstname.asState(),
                placeholder = "Ime",
                allowedChars = KarikaConstants.lettersSpace,
                imeAction = ImeAction.Next,
                enabled = editableFields
            )
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Prezime*",
                value = component.lastname.asState(),
                placeholder = "Prezime",
                allowedChars = KarikaConstants.lettersSpace,
                imeAction = ImeAction.Next,
                enabled = editableFields
            )
        } else {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Naziv pravnog lica*",
                value = component.firstname.asState(),
                placeholder = "Naziv pravnog lica",
                allowedChars = KarikaConstants.lettersSpace,
                imeAction = ImeAction.Next,
                enabled = editableFields
            )
        }
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Grad*",
            value = component.city.asState(),
            placeholder = "Grad",
            allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
            imeAction = ImeAction.Next,
            enabled = editableFields
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Adresa i broj ulice*",
            value = component.address.asState(),
            placeholder = "Adresa i broj ulice",
            allowedChars = KarikaConstants.numbersAndLettersSpace,
            imeAction = ImeAction.Next,
            enabled = editableFields
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Poštanski broj*",
            value = component.postal.asState(),
            placeholder = "Poštanski broj",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next,
            enabled = editableFields
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj telefona*",
            value = component.telephone.asState(),
            placeholder = "Broj telefona",
            allowedChars = KarikaConstants.numbers,
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
        )

        HorizontalButtons(
            modifier = Modifier,
            primaryTitle = "Sačuvaj izmjene",
            secondaryTitle = "Odustani"
        ) {
            if (it == "Odustani") {
                editAddress = ""
                return@HorizontalButtons
            }

            component.updateAddress()
        }
    }
}

@Composable
private fun UpdateContactInfo(component: AccountComponent) {
    var editContact by component.editContact.asState()
    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = "Kontakt informacije",
            color = KarikaColors.Black,
            textSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )

        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Ime*",
            value = component.firstname.asState(),
            placeholder = "Ime",
            allowedChars = KarikaConstants.lettersSpace,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Prezime*",
            value = component.lastname.asState(),
            placeholder = "Prezime",
            allowedChars = KarikaConstants.lettersSpace,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Email adresa*",
            value = component.email.asState(),
            placeholder = "Email adresa",
            keyboardType = KeyboardType.Email,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj telefona*",
            value = component.telephone.asState(),
            placeholder = "Broj telefona",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Next
        )

        HorizontalButtons(
            modifier = Modifier,
            primaryTitle = "Sačuvaj izmjene",
            secondaryTitle = "Odustani"
        ) {
            if (it == "Odustani") {
                editContact = false
                return@HorizontalButtons
            }

            component.updateContact()
        }
    }
}
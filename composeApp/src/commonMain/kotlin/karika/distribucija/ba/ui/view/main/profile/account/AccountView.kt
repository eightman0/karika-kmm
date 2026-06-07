package karika.distribucija.ba.ui.view.main.profile.account

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.ui.common.isKiosk
import karika.distribucija.ba.ui.components.HorizontalButtons
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaPicker
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.isTabletLandscape
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
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
            if (editAddress != null) {
                UpdateAddress(component)
            } else if (editContact) {
                UpdateContactInfo(component)
            } else {
                if (isTabletLandscape()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ContactInfo(modifier = Modifier.weight(1f), component = component)
                        BillingAddress(modifier = Modifier.weight(1f), component = component)
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        ShippingAddress(modifier = Modifier.weight(1f), component = component)
                        AllShippingAddress(modifier = Modifier.weight(1f), component = component)
                    }
                } else {
                    ContactInfo(component = component)
                    BillingAddress(component = component)
                    ShippingAddress(component = component)
                    AllShippingAddress(component = component)
                }
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
        DeleteAccountConfirmation(component)
    }
}

@Composable
private fun ContactInfo(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
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
                text = "Informacije profila",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaText(
                modifier = Modifier
                    .onClick { component.edit(null, "Informacije profila") }
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
                text = "Pravno lice",
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
                text = "ID broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.idNumber(),
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
                text = "PDV broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.pdv(),
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
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .weight(0.5f),
                text = "Veličina objekta",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.objectSize(),
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
                text = "Tip objekta",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.objectType(),
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
                text = "Broj zaposlenih",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.employeeCount(),
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
                text = "Viber broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.viberPhoneNumber(),
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
        if (!isKiosk()) {
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = KarikaColors.Gray11
            )
            KarikaText(
                modifier = Modifier
                    .onClick {
                        component.deleteAccount.negate()
                    }
                    .padding(16.dp),
                text = "Obriši nalog",
                color = KarikaColors.Red,
                textSize = 16.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

@Composable
private fun BillingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
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
                    .onClick {
                        component.edit(
                            profile.billingAddress(),
                            "Informacije za naplatu",
                            false
                        )
                    }
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
                text = "Ime i prezime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.firstname + " " + profile.billingAddress()?.lastname,
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
                text = "Grad i poštanski broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.billingAddress()?.city + ", " + profile.billingAddress()?.postcode,
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
                text = "Država",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = "Bosna i Hercegovina",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
private fun ShippingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
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
                    .onClick { component.edit(profile.shippingAddress(), "Adresa za dostavu") }
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
                text = "Ime i prezime",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.firstname + " " + profile.shippingAddress()?.lastname,
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
                text = "Grad i poštanski broj",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = profile.shippingAddress()?.city + ", " + profile.shippingAddress()?.postcode,
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
                text = "Država",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(16.dp),
                text = "Bosna i Hercegovina",
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
private fun AllShippingAddress(
    modifier: Modifier = Modifier.fillMaxWidth(),
    component: AccountComponent
) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    val deleteAddressConfirmation = mutableStateOf<Address?>(null).asState()
    if (profile.addresses.none { it.defaultShipping == null && it.defaultBilling == null }) {
        return
    }

    Column(
        modifier = modifier
            .border(width = 1.dp, shape = RoundedCornerShape(4.dp), color = KarikaColors.Gray11)
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
                text = "Spisak svih unesenih adresa za dostavu",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Gray11
        )
        profile.addresses
            .filter { it.defaultShipping == null && it.defaultBilling == null }
            .forEach { shippingAddress ->
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .border(
                            width = 1.dp,
                            shape = RoundedCornerShape(4.dp),
                            color = KarikaColors.Gray11
                        )
                        .fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier
                                .padding(16.dp)
                                .weight(0.5f),
                            text = "Ime i prezime",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                        KarikaText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            text = shippingAddress.firstname + " " + shippingAddress.lastname,
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
                            text = shippingAddress.telephone,
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
                            text = shippingAddress.street.firstOrNull(),
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
                            text = "Grad i poštanski broj",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                        KarikaText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            text = shippingAddress.city + ", " + shippingAddress.postcode,
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
                            text = "Država",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                        KarikaText(
                            modifier = Modifier
                                .weight(1f)
                                .padding(16.dp),
                            text = "Bosna i Hercegovina",
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
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        SecondaryButton(
                            modifier = Modifier
                                .height(40.dp),
                            title = "Obriši",
                            color = KarikaColors.Primary,
                        ) {
                            deleteAddressConfirmation.value = shippingAddress
                        }
                        PrimaryButton(
                            modifier = Modifier
                                .height(40.dp),
                            title = "Uredi",
                            color = KarikaColors.Primary
                        ) {
                            component.edit(shippingAddress, "Adresa za dostavu")
                        }
                    }
                }
            }
    }

    if (deleteAddressConfirmation.value != null) {
        ConfirmationModal(
            title = "Obriši adresu za dostavu",
            message = "Jeste li sigurni da želite obrisati ovu adresu za dostavu?",
            primaryButtonText = "Obriši",
            secondaryButtonText = "Odustani",
            onPrimaryClick = {
                component.deleteShippingAddress(deleteAddressConfirmation.value)
                deleteAddressConfirmation.value = null
            },
            onSecondaryClick = {
                deleteAddressConfirmation.value = null
            }
        )
    }
}

@Composable
private fun UpdateAddress(component: AccountComponent) {
    var editAddress by component.editAddress.asState()
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()

    Column(
        modifier = Modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            text = editAddress?.second,
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
            imeAction = ImeAction.Next,
            disabledTextColor = KarikaColors.Gray2
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Prezime*",
            value = component.lastname.asState(),
            placeholder = "Prezime",
            allowedChars = KarikaConstants.lettersSpace,
            imeAction = ImeAction.Next,
            disabledTextColor = KarikaColors.Gray2
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
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Adresa i broj ulice*",
            value = component.address.asState(),
            placeholder = "Adresa i broj ulice",
            allowedChars = KarikaConstants.numbersAndLettersSpace,
            imeAction = ImeAction.Next,
            disabledTextColor = KarikaColors.Gray2
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Država*",
            value = mutableStateOf("Bosna i Hercegovina").asState(),
            placeholder = "Država",
            imeAction = ImeAction.Next,
            enabled = false,
            disabledTextColor = KarikaColors.Gray6
        )
        KarikaPicker(
            title = "Grad*",
            placeholder = "Grad",
            value = component.city.asState(),
            values = mutableStateOf(KarikaConstants.cities()).asState()
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
            disabledTextColor = KarikaColors.Gray2
        )

        HorizontalButtons(
            modifier = Modifier,
            primaryTitle = "Sačuvaj izmjene",
            secondaryTitle = "Odustani"
        ) {
            if (it == "Odustani") {
                editAddress = null
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
            text = "Informacije profila",
            color = KarikaColors.Black,
            textSize = 16.sp,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.W600
        )

        KarikaPicker(
            title = "Veličina objekta*",
            placeholder = "Veličina objekta*",
            value = component.objectSize.asState(),
            values = mutableStateOf(KarikaConstants.companySizes).asState()
        )
        KarikaPicker(
            title = "Tip objekta*",
            placeholder = "Tip objekta",
            value = component.objectType.asState(),
            values = mutableStateOf(KarikaConstants.companyTypes).asState()
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Broj zaposlenih*",
            value = component.employeeCount.asState(),
            placeholder = "Broj zaposlenih",
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Viber broj telefona*",
            value = component.viberPhoneNumber.asState(),
            placeholder = "Broj telefona",
            keyboardType = KeyboardType.Phone,
            imeAction = ImeAction.Done
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

@Composable
private fun DeleteAccountConfirmation(component: AccountComponent) {
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


@Composable
fun ConfirmationModal(
    title: String,
    message: String,
    primaryButtonText: String,
    secondaryButtonText: String,
    type: Int = 0, // 0 primary, 1 secondary
    onPrimaryClick: () -> Unit,
    onSecondaryClick: () -> Unit
) {
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
                    text = title,
                    color = KarikaColors.Gray2,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    text = message,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W400
                )
                if (type == 0) {
                    HorizontalButtons(
                        modifier = Modifier,
                        primaryTitle = primaryButtonText,
                        secondaryTitle = secondaryButtonText
                    ) {
                        if (it == secondaryButtonText) {
                            onSecondaryClick()
                            return@HorizontalButtons
                        }
                        onPrimaryClick()
                    }
                } else {
                    HorizontalSecondaryButtons(
                        modifier = Modifier,
                        primaryTitle = primaryButtonText,
                        secondaryTitle = secondaryButtonText
                    ) {
                        if (it == secondaryButtonText) {
                            onSecondaryClick()
                            return@HorizontalSecondaryButtons
                        }
                        onPrimaryClick()
                    }
                }
            }
        }
    }
}
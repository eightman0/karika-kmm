package karika.distribucija.ba.ui.view.main.cart.nextstep

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaRadioButton
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.util.KarikaConstants
import karika.distribucija.ba.util.karikaPriceFormat

@Composable
fun ShippingDetailsView(component: ShippingDetailsComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Title(component)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AddressBox(component)
                Cart(
                    modifier = Modifier
                        .weight(1f), component
                )
            }
            PinnedFooter(component)
        }
    }

    LoadingView1(component)
}

@Composable
private fun Cart(modifier: Modifier, component: ShippingDetailsComponent) {
    val cart = component.stateHolder.cart1.collectAsState()
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp),
            color = KarikaColors.Black,
            text = "Ukupno:",
            fontWeight = FontWeight.W700,
            textSize = 20.sp
        )
        cart.value.entries.forEach {
            VendorItem(it)
        }
    }
}

@Composable
private fun VendorItem(entry: Map.Entry<Vendor, List<Pair<Product, Int>>>) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(color = KarikaColors.Gray10, shape = RoundedCornerShape(4.dp))
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier
                .padding(16.dp),
            color = KarikaColors.Gray2,
            text = entry.value.firstOrNull()?.first?.vendorName() ?: "-",
            fontWeight = FontWeight.W600,
            textSize = 16.sp
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .weight(1f),
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier,
                atext = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W300,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append("VPC: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Gray2,
                            fontSize = 16.sp
                        )
                    ) {
                        append(entry.totalVPC())
                    }
                }
            )
            KarikaText(
                modifier = Modifier,
                atext = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W300,
                            color = KarikaColors.Gray2,
                            fontSize = 14.sp
                        )
                    ) {
                        append("Ukupno sa PDV: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W600,
                            color = KarikaColors.Gray2,
                            fontSize = 16.sp
                        )
                    ) {
                        append(entry.total())
                    }
                }
            )
        }
    }
}

@Composable
private fun AddressBox(component: ShippingDetailsComponent) {
    val profile by component.stateHolder.userDetails.collectAsState()
    val newAddress = component.newAddress.asState()
    val addresses = component.addresses.collectAsState()
    val selectedAddress = component.selectedAddress.asState()

    addresses.value.forEach {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .onClick {
                    selectedAddress.value = it.id?.toString() ?: ""
                    newAddress.value = false
                }
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Gray5, shape = RoundedCornerShape(4.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                KarikaRadioButton(
                    title = profile.companyName(),
                    selected = selectedAddress.value == it.id?.toString()
                ) { _ ->
                    selectedAddress.value = it.id?.toString() ?: ""
                    newAddress.value = false
                }
                KarikaText(
                    modifier = Modifier
                        .padding(start = 48.dp, bottom = 16.dp),
                    color = KarikaColors.Gray2,
                    text = it.address(),
                    fontWeight = FontWeight.W400,
                    textSize = 14.sp
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .onClick {
                newAddress.value = true
                selectedAddress.value = ""
            }
            .fillMaxWidth()
            .border(width = 1.dp, color = KarikaColors.Gray5, shape = RoundedCornerShape(4.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            KarikaRadioButton(
                title = "Dodaj novu adresu",
                selected = newAddress.value
            ) {
                newAddress.value = true
                selectedAddress.value = ""
            }
            if (newAddress.value) {
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                ) {
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
                        title = "Naziv pravnog lica*",
                        value = component.companyName.asState(),
                        placeholder = "Naziv pravnog lica",
                        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    )
                    KarikaTextField1(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "Grad*",
                        value = component.city.asState(),
                        placeholder = "Grad",
                        allowedChars = KarikaConstants.numbersAndLetters.plus(" ").plus("."),
                        imeAction = ImeAction.Next
                    )
                    KarikaTextField1(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "Adresa i broj ulice*",
                        value = component.address.asState(),
                        placeholder = "Adresa i broj ulice",
                        allowedChars = KarikaConstants.numbersAndLettersSpace,
                        imeAction = ImeAction.Next
                    )
                    KarikaTextField1(
                        modifier = Modifier
                            .fillMaxWidth(),
                        title = "Poštanski broj*",
                        value = component.postal.asState(),
                        placeholder = "Poštanski broj",
                        allowedChars = KarikaConstants.numbers,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
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
                }
            }
        }
    }
}

@Composable
private fun Title(component: ShippingDetailsComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp),
            color = KarikaColors.Black,
            text = "Informacije za dostavu:",
            fontWeight = FontWeight.W700,
            textSize = 20.sp
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    component.mainBack()
                }
                .padding(end = 16.dp),
            color = KarikaColors.Primary,
            text = "Odustani",
            fontWeight = FontWeight.W600,
            textSize = 16.sp
        )
    }
}

@Composable
private fun PinnedFooter(component: ShippingDetailsComponent) {
    val newAddress = component.newAddress.asState()
    HorizontalDivider(
        modifier = Modifier.fillMaxWidth(),
        thickness = 1.dp,
        color = KarikaColors.Divider
    )
    PrimaryButtonFilled(
        modifier = Modifier
            .height(48.dp)
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        title = if (newAddress.value) "Spasi i nastavi dalje" else "Pošalji zahtjev dobavljaču",
        fontWeight = FontWeight.W700,
        textSize = 18.sp
    ) {
        component.handleShippingAddress()
    }
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.totalVPC(): String {
    return karikaPriceFormat(
        value.sumOf { it.first.vpc(it.second) }
    ) + " KM"
}

private fun Map.Entry<Vendor, List<Pair<Product, Int>>>.total(): String {
    return karikaPriceFormat(
        value.sumOf { it.first.vpc(it.second) } * 1.17
    ) + " KM"
}
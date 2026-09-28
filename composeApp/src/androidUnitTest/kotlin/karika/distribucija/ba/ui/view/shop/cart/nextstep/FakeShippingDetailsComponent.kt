package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.CartData
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.domain.model.Vendor
import kotlinx.coroutines.flow.MutableStateFlow

class FakeShippingDetailsComponent(
    customer: UserDetails,
    addresses: List<Address>,
    selected: Int?,
    items: Map<Vendor, List<Pair<Product, Int>>> = emptyMap(),
) : ShippingDetailsComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val cart = MutableStateFlow(CartData(items = items))
    override val userDetails = MutableStateFlow(customer)
    override val addresses = MutableStateFlow(addresses)
    override val selectedAddress = mutableStateOf(selected.toString())
    override val newAddress = mutableStateOf(false)
    override val firstname = mutableStateOf("")
    override val lastname = mutableStateOf("")
    override val companyName = mutableStateOf(customer.companyName())
    override val address = mutableStateOf("")
    override val city = mutableStateOf("")
    override val postal = mutableStateOf("")
    override val telephone = mutableStateOf("")
    override val vendorNote = mutableStateOf("")

    /** Every press of the footer button, with what was chosen then. */
    data class Submit(val newAddress: Boolean, val selectedAddress: String, val vendorNote: String)

    val submits = mutableListOf<Submit>()
    var backRequests = 0

    override fun handleShippingAddress() {
        submits += Submit(newAddress.value, selectedAddress.value, vendorNote.value)
    }

    override fun validateNewAddress() = isNewAddressValid(
        firstname = firstname.value,
        lastname = lastname.value,
        street = address.value,
        city = city.value,
        postal = postal.value,
        telephone = telephone.value,
    )

    override fun mainBack() {
        backRequests++
    }
}

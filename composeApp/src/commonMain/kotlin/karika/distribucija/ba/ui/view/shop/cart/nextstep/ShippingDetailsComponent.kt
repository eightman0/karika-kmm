package karika.distribucija.ba.ui.view.shop.cart.nextstep

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.CartData
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SetShippingAddressRequest
import karika.distribucija.ba.domain.model.ShippingAddress
import karika.distribucija.ba.domain.model.UserDetails
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.isPostalCodeValid
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

interface ShippingDetailsComponent : ScreenComponent {
    val cart: StateFlow<CartData>

    /** The logged-in customer, for the company name shown on every address. */
    val userDetails: StateFlow<UserDetails>

    /** The customer's addresses except the billing one, plus a new one once it is saved. */
    val addresses: StateFlow<List<Address>>

    /** The id of the chosen address, as text; "-100" is a new address. */
    val selectedAddress: MutableState<String>

    /** Whether the new address form is open. */
    val newAddress: MutableState<Boolean>

    val firstname: MutableState<String>
    val lastname: MutableState<String>
    val companyName: MutableState<String>
    val address: MutableState<String>
    val city: MutableState<String>
    val postal: MutableState<String>
    val telephone: MutableState<String>
    val vendorNote: MutableState<String>

    /**
     * With the new address form open, adds that address to the list and selects it; otherwise
     * sets the chosen address on the cart and places the order.
     */
    fun handleShippingAddress()

    fun validateNewAddress(): Boolean

    fun mainBack()
}

class DefaultShippingDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder), ShippingDetailsComponent {
    private val repository = CartRepository()
    private val _addresses = MutableStateFlow(
        stateHolder.customerSpecificHandler.userDetails.value.addresses
            .filter { it.defaultBilling == null || it.defaultBilling == "false" }
    )
    override val addresses = _addresses.asStateFlow()

    override val cart: StateFlow<CartData>
        get() = stateHolder.cartHandler.cart
    override val userDetails: StateFlow<UserDetails>
        get() = stateHolder.customerSpecificHandler.userDetails

    override val selectedAddress =
        mutableStateOf(stateHolder.customerSpecificHandler.userDetails.value.addresses.find { it.defaultShipping == "true" }?.id.toString())
    override val newAddress = mutableStateOf(false)

    override val firstname = mutableStateOf("")
    override val lastname = mutableStateOf("")
    override val companyName =
        mutableStateOf(stateHolder.customerSpecificHandler.userDetails.value.companyName())
    override val address = mutableStateOf("")
    override val city = mutableStateOf("")
    override val postal = mutableStateOf("")
    override val telephone = mutableStateOf("")
    override val vendorNote = mutableStateOf("")

    override fun handleShippingAddress() {
        if (newAddress.value) {
            _addresses.update {
                it.plus(
                    Address(
                        id = -100,
                        firstname = firstname.value,
                        lastname = lastname.value,
                        postcode = postal.value,
                        city = city.value,
                        street = listOf(address.value),
                        telephone = telephone.value,
                        customerId = _addresses.value.firstOrNull()?.customerId,
                        countryId = _addresses.value.firstOrNull()?.countryId,
                    )
                )
            }
            selectedAddress.value = "-100"
            newAddress.value = false
        } else {
            // No default shipping address, or it is the billing one (left out of the list)
            val chosen = addresses.value.firstOrNull { it.id?.toString() == selectedAddress.value }
            if (chosen == null) {
                showMessage("Odaberite adresu za dostavu.")
                return
            }
            scope.launch {
                repository.setAddress(
                    SetShippingAddressRequest(
                        addressInformation = ShippingAddress(
                            shippingAddress = chosen
                                .copy(
                                    id = null,
                                    defaultShipping = null,
                                    defaultBilling = null,
                                    save = if (selectedAddress.value == "-100") 1 else 0
                                ),
                            billingAddress = chosen
                                .copy(
                                    id = null,
                                    defaultShipping = null,
                                    defaultBilling = null,
                                    save = 0
                                ),
                            shippingCode = "freeshipping",
                            shippingMethodCode = "freeshipping"
                        )
                    )
                ).collect {
                    when (it) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            placeOrder(vendorNote.value)
                            stateHolder.customerSpecificHandler.getUserDetails()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(it.message ?: "")
                        }
                    }
                }
            }
        }
    }

    private fun placeOrder(note: String) {
        scope.launch {
            repository.placeOrder(note)
                .collect {
                    when (it) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            placedOrder(it.data)
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(it.message ?: "")
                        }
                    }
                }
        }
    }

    override fun validateNewAddress(): Boolean {
        return firstname.value.isNotEmpty() &&
                lastname.value.isNotEmpty() &&
                address.value.isNotEmpty() &&
                city.value.isNotEmpty() &&
                postal.value.isNotEmpty() &&
                postal.value.isPostalCodeValid() &&
                telephone.value.isNotEmpty()
    }

}
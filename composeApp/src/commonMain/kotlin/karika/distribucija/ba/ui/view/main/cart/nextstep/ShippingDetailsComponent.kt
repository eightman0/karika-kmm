package karika.distribucija.ba.ui.view.main.cart.nextstep

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.CartRepository
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.SetShippingAddressRequest
import karika.distribucija.ba.domain.model.ShippingAddress
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class ShippingDetailsComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
) : CommonComponent(componentContext, stateHolder) {
    private val repository = CartRepository()
    private val _addresses = MutableStateFlow(stateHolder.customerSpecificHandler.userDetails.value.addresses)
    val addresses = _addresses.asStateFlow()

    val selectedAddress =
        mutableStateOf(stateHolder.customerSpecificHandler.userDetails.value.addresses.find { it.defaultShipping == "true" }?.id.toString())
    val newAddress = mutableStateOf(false)

    val firstname = mutableStateOf("")
    val lastname = mutableStateOf("")
    val companyName = mutableStateOf(stateHolder.customerSpecificHandler.userDetails.value.companyName())
    val address = mutableStateOf("")
    val city = mutableStateOf("")
    val postal = mutableStateOf("")
    val telephone = mutableStateOf("")

    fun handleShippingAddress() {
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
            iOScope.launch {
                repository.setAddress(
                    SetShippingAddressRequest(
                        addressInformation = ShippingAddress(
                            shippingAddress = addresses.value.first { it.id?.toString() == selectedAddress.value }
                                .copy(
                                    id = null,
                                    defaultShipping = null,
                                    defaultBilling = null,
                                    save = if (selectedAddress.value == "-100") 1 else 0
                                ),
                            billingAddress = addresses.value.first { it.id?.toString() == selectedAddress.value }
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
                            placeOrder()
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

    private fun placeOrder() {
        iOScope.launch {
            repository.placeOrder()
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

}
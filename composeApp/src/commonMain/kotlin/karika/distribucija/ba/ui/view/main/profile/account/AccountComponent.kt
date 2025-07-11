package karika.distribucija.ba.ui.view.main.profile.account

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.launch

class AccountComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val editAddress = mutableStateOf("")
    val editContact = mutableStateOf(false)
    val firstname = mutableStateOf("")
    val lastname = mutableStateOf("")
    val address = mutableStateOf("")
    val city = mutableStateOf("")
    val email = mutableStateOf("")
    val postal = mutableStateOf("")
    val telephone = mutableStateOf("")

    fun updateAddress() {
        iOScope.launch {
            userRepository.put(
                UpdateCustomerRequest(
                    customer = stateHolder.userDetails.value
                        .copy(
                            addresses = stateHolder.userDetails.value
                                .addresses.filter {
                                    if (editAddress.value == "Adresa za dostavu") {
                                        it.defaultBilling == "true"
                                    } else {
                                        it.defaultShipping == "true"
                                    }
                                }.plus(
                                    stateHolder.userDetails.value.addresses.firstOrNull {
                                        if (editAddress.value == "Adresa za dostavu") {
                                            it.defaultShipping == "true"
                                        } else {
                                            it.defaultBilling == "true"
                                        }
                                    }?.copy(
                                        firstname = firstname.value,
                                        lastname = lastname.value,
                                        city = city.value,
                                        postcode = postal.value,
                                        telephone = telephone.value,
                                        street = listOf(address.value),
                                        defaultShipping = "${editAddress.value == "Adresa za dostavu"}",
                                        defaultBilling = "${editAddress.value == "Informacije za naplatu"}",
                                    ) ?: Address(
                                        firstname = firstname.value,
                                        lastname = lastname.value,
                                        city = city.value,
                                        postcode = postal.value,
                                        telephone = telephone.value,
                                        street = listOf(address.value),
                                        defaultShipping = "${editAddress.value == "Adresa za dostavu"}",
                                        defaultBilling = "${editAddress.value == "Informacije za naplatu"}",
                                        countryId = stateHolder.userDetails.value.addresses.firstOrNull()?.countryId
                                            ?: ""
                                    )
                                )
                        )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        stateHolder.getUserDetails {
                            hideLoader()
                            editAddress.value = ""
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun updateContact() {
        iOScope.launch {
            userRepository.put(
                UpdateCustomerRequest(
                    customer = stateHolder.userDetails.value
                        .copy(
                            firstname = firstname.value,
                            lastname =  lastname.value,
                            email = email.value
                        )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        stateHolder.getUserDetails {
                            hideLoader()
                            editAddress.value = ""
                            editContact.value = false
                        }
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    fun edit(value: String) {
        if (value == "Kontakt informacije") {
            firstname.value = stateHolder.userDetails.value.firstname ?: ""
            lastname.value = stateHolder.userDetails.value.lastname ?: ""
            email.value = stateHolder.userDetails.value.email ?: ""
            editContact.value = true
            return
        }
        if (value == "Adresa za dostavu") {
            firstname.value = stateHolder.userDetails.value.shippingAddress()?.firstname ?: ""
            lastname.value = stateHolder.userDetails.value.shippingAddress()?.lastname ?: ""
            address.value =
                stateHolder.userDetails.value.shippingAddress()?.street?.firstOrNull() ?: ""
            city.value = stateHolder.userDetails.value.shippingAddress()?.city ?: ""
            postal.value = stateHolder.userDetails.value.shippingAddress()?.postcode ?: ""
            telephone.value = stateHolder.userDetails.value.shippingAddress()?.telephone ?: ""
        } else {
            firstname.value = stateHolder.userDetails.value.billingAddress()?.firstname ?: ""
            lastname.value = stateHolder.userDetails.value.billingAddress()?.lastname ?: ""
            address.value =
                stateHolder.userDetails.value.billingAddress()?.street?.firstOrNull() ?: ""
            city.value = stateHolder.userDetails.value.billingAddress()?.city ?: ""
            postal.value = stateHolder.userDetails.value.billingAddress()?.postcode ?: ""
            telephone.value = stateHolder.userDetails.value.billingAddress()?.telephone ?: ""
        }
        editAddress.value = value
    }
}
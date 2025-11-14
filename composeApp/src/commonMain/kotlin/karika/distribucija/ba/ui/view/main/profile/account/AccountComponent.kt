package karika.distribucija.ba.ui.view.main.profile.account

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import kotlinx.coroutines.launch

class AccountComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val deleteAccount = mutableStateOf(false)
    val changePassSheet = mutableStateOf(false)
    val editAddress = mutableStateOf<Pair<Address?, String>?>(null)
    val editContact = mutableStateOf(false)
    val editableFields = mutableStateOf(true)
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
                    customer = stateHolder.customerSpecificHandler.userDetails.value
                        .copy(
                            addresses = stateHolder.customerSpecificHandler.userDetails.value
                                .addresses
                                .map {
                                    if (it.id == editAddress.value?.first?.id) {
                                        it.copy(
                                            firstname = firstname.value,
                                            lastname = lastname.value,
                                            city = city.value,
                                            postcode = postal.value,
                                            telephone = telephone.value,
                                            street = listOf(address.value),
                                        )
                                    } else {
                                        it
                                    }
                                }
                        )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        stateHolder.customerSpecificHandler.getUserDetails {
                            hideLoader()
                            editAddress.value = null
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
                    customer = stateHolder.customerSpecificHandler.userDetails.value
                        .copy(
                            firstname = firstname.value,
                            lastname = lastname.value,
                            email = email.value,
                            addresses = stateHolder.customerSpecificHandler.userDetails.value.addresses
                                .map { it.copy(telephone = telephone.value) }
                        )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        stateHolder.customerSpecificHandler.getUserDetails {
                            hideLoader()
                            editAddress.value = null
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

    fun deleteShippingAddress(address: Address?) {
        iOScope.launch {
            userRepository.put(
                UpdateCustomerRequest(
                    customer = stateHolder.customerSpecificHandler.userDetails.value
                        .copy(
                            addresses = stateHolder.customerSpecificHandler.userDetails.value.addresses
                                .filter { it.id != address?.id }
                        )
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> showLoader()
                    is ResultState.Success -> {
                        stateHolder.customerSpecificHandler.getUserDetails {
                            hideLoader()
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

    fun edit(it: Address?, value: String, edit: Boolean = true) {
        if (value == "Kontakt informacije") {
            firstname.value = stateHolder.customerSpecificHandler.userDetails.value.firstname ?: ""
            lastname.value = stateHolder.customerSpecificHandler.userDetails.value.lastname ?: ""
            email.value = stateHolder.customerSpecificHandler.userDetails.value.email ?: ""
            telephone.value =
                stateHolder.customerSpecificHandler.userDetails.value.addresses.firstOrNull()?.telephone
                    ?: ""
            editContact.value = true
            return
        }

        if (value == "Adresa za dostavu") {
            firstname.value = it?.firstname ?: ""
            lastname.value = it?.lastname ?: ""
            address.value = it?.street?.firstOrNull() ?: ""
            city.value = it?.city ?: ""
            postal.value = it?.postcode ?: ""
            telephone.value = it?.telephone ?: ""
        } else {
            firstname.value = stateHolder.customerSpecificHandler.userDetails.value.companyName()
            lastname.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.lastname
                    ?: ""
            address.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.street?.firstOrNull()
                    ?: ""
            city.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.city ?: ""
            postal.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.postcode
                    ?: ""
            telephone.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.telephone
                    ?: ""
        }

        editableFields.value = edit
        editAddress.value = Pair(it, value)
    }

    fun showChangePass() {
        changePassSheet.value = true
    }

    fun changePass(oldPass: String, newPass: String) {
        iOScope.launch {
            userRepository.changePass(oldPass, newPass)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            showMessage(result.data)
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun deleteAccount() {
        iOScope.launch {
            userRepository.deleteAccount()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            mainScope.launch {
                                deleteUser()
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
}
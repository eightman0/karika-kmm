package karika.distribucija.ba.ui.view.main.profile.account

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.RefType
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
    val firstname = mutableStateOf("")
    val lastname = mutableStateOf("")
    val address = mutableStateOf("")
    val city = mutableStateOf("")
    val email = mutableStateOf("")
    val postal = mutableStateOf("")
    val telephone = mutableStateOf("")

    val objectSize = mutableStateOf("")
    val objectType = mutableStateOf("")
    val employeeCount = mutableStateOf("")
    val viberPhoneNumber = mutableStateOf("")

    fun updateAddress() {
        scope.launch {
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
        scope.launch {
            userRepository.put(
                UpdateCustomerRequest(
                    customer = stateHolder.customerSpecificHandler.userDetails.value
                        .copy(
                            customAttributes = stateHolder.customerSpecificHandler.userDetails.value.customAttributes
                                .map {
                                    when (it.attributeCode) {
                                        "b2b_velicina_objekta" -> it.copy(value = objectSize.value)
                                        "b2b_tip_objekta" -> it.copy(value = objectType.value)
                                        "b2b_broj_zaposlenih" -> it.copy(value = employeeCount.value)
                                        "viber_messages_phone_number" -> it.copy(value = viberPhoneNumber.value)
                                        else -> it
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
        scope.launch {
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
        if (value == "Informacije profila") {
            objectSize.value = stateHolder.customerSpecificHandler.userDetails.value.objectSize()
            objectType.value = stateHolder.customerSpecificHandler.userDetails.value.objectType()
            employeeCount.value =
                stateHolder.customerSpecificHandler.userDetails.value.employeeCount()
            viberPhoneNumber.value =
                stateHolder.customerSpecificHandler.userDetails.value.viberPhoneNumber()

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
            firstname.value =
                stateHolder.customerSpecificHandler.userDetails.value.billingAddress()?.firstname
                    ?: ""
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

        editAddress.value = Pair(it, value)
    }

    fun showChangePass() {
        changePassSheet.value = true
    }

    fun changePass(oldPass: String, newPass: String) {
        scope.launch {
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
                    logEvent(
                        eventType = EventType.CUSTOMER_PASSWORD_CHANGE,
                        refType = RefType.USER_LOGIN
                    )
                }
        }
    }

    fun deleteAccount() {
        scope.launch {
            userRepository.deleteAccount()
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            scope.launch {
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
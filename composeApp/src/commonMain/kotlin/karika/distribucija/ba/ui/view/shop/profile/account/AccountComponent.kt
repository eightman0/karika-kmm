package karika.distribucija.ba.ui.view.shop.profile.account

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.model.Address
import karika.distribucija.ba.domain.model.Attributes
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.RefType
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.UpdateCustomerRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.products.details.toInt
import kotlinx.coroutines.launch

private val NOTIFICATION_ATTRIBUTE_CODES = setOf(
    "notification_email_enabled",
    "notification_viber_enabled",
    "notification_push_enabled"
)

private val CONTACT_ATTRIBUTE_CODES = NOTIFICATION_ATTRIBUTE_CODES + setOf(
    "b2b_velicina_objekta",
    "b2b_tip_objekta",
    "b2b_broj_zaposlenih",
    "viber_messages_phone_number"
)

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

    val emailNotifications = mutableStateOf(true)
    val viberNotifications = mutableStateOf(true)
    val pushNotifications = mutableStateOf(true)

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
                                },
                            customAttributes = stateHolder.customerSpecificHandler.userDetails.value.customAttributes
                                .filter { it.attributeCode !in NOTIFICATION_ATTRIBUTE_CODES }
                                .plus(
                                    listOf(
                                        Attributes(
                                            "notification_email_enabled",
                                            emailNotifications.value.toInt()
                                        ),
                                        Attributes(
                                            "notification_viber_enabled",
                                            viberNotifications.value.toInt()
                                        ),
                                        Attributes(
                                            "notification_push_enabled",
                                            pushNotifications.value.toInt()
                                        )
                                    )
                                )
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
                                .filter { it.attributeCode !in CONTACT_ATTRIBUTE_CODES }
                                .plus(
                                    listOf(
                                        Attributes(
                                            "notification_email_enabled",
                                            emailNotifications.value.toInt()
                                        ),
                                        Attributes(
                                            "notification_viber_enabled",
                                            viberNotifications.value.toInt()
                                        ),
                                        Attributes(
                                            "notification_push_enabled",
                                            pushNotifications.value.toInt()
                                        ),
                                        Attributes("b2b_velicina_objekta", objectSize.value),
                                        Attributes("b2b_tip_objekta", objectType.value),
                                        Attributes("b2b_broj_zaposlenih", employeeCount.value),
                                        Attributes("viber_messages_phone_number", viberPhoneNumber.value.ifEmpty { " " }),
                                    )
                                )
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
            val attrs = stateHolder.customerSpecificHandler.userDetails.value.customAttributes
            objectSize.value = attrs.find { attr -> attr.attributeCode == "b2b_velicina_objekta" }?.value ?: ""
            objectType.value = attrs.find { attr -> attr.attributeCode == "b2b_tip_objekta" }?.value ?: ""
            employeeCount.value = attrs.find { attr -> attr.attributeCode == "b2b_broj_zaposlenih" }?.value ?: ""
            viberPhoneNumber.value =
                attrs.find { attr -> attr.attributeCode == "viber_messages_phone_number" }?.value ?: ""

            emailNotifications.value =
                attrs.find { attr -> attr.attributeCode == "notification_email_enabled" }?.value == "1"
            viberNotifications.value =
                attrs.find { attr -> attr.attributeCode == "notification_viber_enabled" }?.value == "1"
            pushNotifications.value =
                attrs.find { attr -> attr.attributeCode == "notification_push_enabled" }?.value == "1"

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
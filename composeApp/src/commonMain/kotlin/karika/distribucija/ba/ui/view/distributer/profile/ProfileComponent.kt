package karika.distribucija.ba.ui.view.distributer.profile

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.HttpClientProvider.profileImage
import karika.distribucija.ba.domain.api.DashRepository
import karika.distribucija.ba.domain.model.EventType
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.RefType
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.launch

class ProfileComponent(componentContext: ComponentContext, stateHolder: KarikaStateHolder) :
    CommonComponent(componentContext, stateHolder) {

    val deleteAccount = mutableStateOf(false)
    val customerRegions = mutableStateOf(
        stateHolder.vendorSpecificHandler.vendorDetails.value.targetCustomerRegion
            ?.replace("|", "")
            ?.trim()
            ?.split(",")
            ?.map { KarikaUnit(label = it, it) }
            ?: emptyList()
    )
    val customerGroups = mutableStateOf(
        stateHolder.vendorSpecificHandler.vendorDetails.value.b2bTargetCustomerGroup
            ?.replace("|", "")
            ?.trim()
            ?.split(",")
            ?.map { KarikaUnit(label = it, unit = it) }
            ?: emptyList()
    )

    val companyName =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.publicName ?: "")
    val companyId =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorId ?: "")
    val companyPdv =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorPdvBroj ?: "")
    val companyEntity = mutableStateOf(
        KarikaConstants.entries.find { it.id.toString() == stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorEntitet }?.name
            ?: ""
    )
    val companyCanton =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorKanton ?: "")
    val companyCity =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorGrad ?: "")
    val companyMunicipality =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorOpicina ?: "")
    val companyPhone =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.b2bVendorPhone ?: "")

    val minOrderAmount =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.minOrderAmount ?: "")
    val bankAccountNumber =
        mutableStateOf(
            stateHolder.vendorSpecificHandler.vendorDetails.value.bankAccountNumber ?: ""
        )
    val contactName =
        mutableStateOf(stateHolder.vendorSpecificHandler.vendorDetails.value.name ?: "")

    val companyLogo =
        mutableStateOf(
            Triple<String, String, Any?>(
                "",
                "",
                profileImage(stateHolder.vendorSpecificHandler.vendorDetails.value.companyLogo)
            )
        )
    val companyBanner =
        mutableStateOf(
            Triple<String, String, Any?>(
                "",
                "",
                profileImage(stateHolder.vendorSpecificHandler.vendorDetails.value.companyBanner)
            )
        )

    val changePassSheet = mutableStateOf(false)

    fun changePass(oldPass: String, newPass: String) {
        iOScope.launch {
            userRepository.changePass(oldPass, newPass)
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            showMessage(result.data)

                            logEvent(
                                eventType = EventType.CUSTOMER_PASSWORD_CHANGE,
                                refType = RefType.USER_LOGIN
                            )
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun updateProfile() {
        if (bankAccountNumber.value.isNotEmpty() && bankAccountNumber.value.length != 16) {
            showMessage("Broj računa mora imati 16 cifara.")
            return
        }

        iOScope.launch {
            DashRepository()
                .updateProfile(
                    phone = companyPhone.value,
                    groupCustomers = customerGroups.value.joinToString(separator = ",") { it.unit() }
                        .trimIndent(),
                    groupRegions = customerRegions.value.joinToString(separator = ",") { it.unit() }
                        .trimIndent(),
                    name = contactName.value,
                    minOrderAmount = minOrderAmount.value,
                    bankAccountNumber = bankAccountNumber.value,
                    logo = if (companyLogo.value.first == "NEW") Pair(
                        companyLogo.value.second,
                        companyLogo.value.third as? ByteArray ?: return@launch
                    ) else null,
                    banner = if (companyBanner.value.first == "NEW") Pair(
                        companyBanner.value.second,
                        companyBanner.value.third as? ByteArray ?: return@launch
                    ) else null
                )
                .collect { result ->
                    when (result) {
                        is ResultState.Loading -> showLoader()
                        is ResultState.Success -> {
                            hideLoader()
                            showMessage("Informacije profila su sačuvane.")
                            stateHolder.vendorSpecificHandler.getVendorDetails()
                        }

                        is ResultState.Error -> {
                            hideLoader()
                            showMessage(result.message)
                        }
                    }
                }
        }
    }

    fun pickImage(value: Int) {
        stateHolder.handler.pickPhoto { name, data ->
            if (value == 1) {
                companyLogo.value = Triple("NEW", name, data)
                return@pickPhoto
            }

            companyBanner.value = Triple("NEW", name, data)
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
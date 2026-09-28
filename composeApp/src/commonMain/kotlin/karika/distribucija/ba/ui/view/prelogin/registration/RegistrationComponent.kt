package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.replaceAll
import karika.distribucija.ba.domain.api.RegistrationRepository
import karika.distribucija.ba.domain.model.Addresses
import karika.distribucija.ba.domain.model.ConfirmRegistration
import karika.distribucija.ba.domain.model.CustomAttributes
import karika.distribucija.ba.domain.model.Customer
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.RegisterDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorRegisterRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.ScreenComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

interface RegistrationComponent : ScreenComponent {
    val title: String
    val userType: KarikaType

    val companyName: MutableState<String>
    val companyId: MutableState<String>
    val companyPdv: MutableState<String>
    val companyEntity: MutableState<String>
    val companyCanton: MutableState<String>
    val companyCity: MutableState<String>
    val companySize: MutableState<String>
    val companyType: MutableState<String>
    val companyEmployees: MutableState<String>

    val contactFirstname: MutableState<String>
    val contactLastname: MutableState<String>
    val contactAddress: MutableState<String>
    val contactPostal: MutableState<String>
    val contactPhone: MutableState<String>

    val email: MutableState<String>
    val password: MutableState<String>
    val confirmPassword: MutableState<String>
    val agree: MutableState<Boolean>

    val entities: MutableState<List<String>>
    val canton: MutableState<List<String>>
    val city: MutableState<List<String>>

    /** Every customer group / region a vendor can target. */
    val customerGroupOptions: List<KarikaUnit>
    val customerRegionOptions: List<KarikaUnit>

    /** The customer groups / regions the vendor has currently selected. */
    val customerGroups: MutableState<List<KarikaUnit>>
    val customerRegions: MutableState<List<KarikaUnit>>

    fun register()

    fun navigateBack()

    fun getColor(): Color
}

class DefaultRegistrationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    override val userType: KarikaType,
) : CommonComponent(componentContext, stateHolder), RegistrationComponent {
    private val repository = RegistrationRepository()

    override val title: String
        get() = if (userType.isShop()) "Registracija kupca" else "Registracija dobavljača"

    override val companyName = mutableStateOf("")
    override val companyId = mutableStateOf("")
    override val companyPdv = mutableStateOf("")
    override val companyEntity = mutableStateOf("")
    override val companyCanton = mutableStateOf("")
    override val companyCity = mutableStateOf("")
    override val companySize = mutableStateOf("")
    override val companyType = mutableStateOf("")
    override val companyEmployees = mutableStateOf("")

    override val contactFirstname = mutableStateOf("")
    override val contactLastname = mutableStateOf("")
    override val contactAddress = mutableStateOf("")
    override val contactPostal = mutableStateOf("")
    override val contactPhone = mutableStateOf("")

    override val email = mutableStateOf("")
    override val password = mutableStateOf("")
    override val confirmPassword = mutableStateOf("")
    override val agree = mutableStateOf(false)

    override val entities = mutableStateOf(KarikaConstants.entries.map { it.name })
    override val canton = mutableStateOf<List<String>>(emptyList())
    override val city = mutableStateOf<List<String>>(emptyList())
    override val customerGroupOptions: List<KarikaUnit>
        get() = stateHolder.commonHandler.config.value.customerGroupList
    override val customerRegionOptions: List<KarikaUnit>
        get() = stateHolder.commonHandler.config.value.customerRegionList
    override val customerRegions = mutableStateOf(customerRegionOptions)
    override val customerGroups = mutableStateOf(customerGroupOptions)

    override fun register() {
        val error = input().validationError(userType.isShop())
        if (error != null) {
            showMessage(error)
            return
        }
        if (userType.isShop()) {
            registerShop()
            return
        }
        registerVendor()
    }

    fun input() = RegistrationInput(
        companyName = companyName.value,
        companyId = companyId.value,
        companyEntity = companyEntity.value,
        companyCanton = companyCanton.value,
        companyCity = companyCity.value,
        companySize = companySize.value,
        companyType = companyType.value,
        contactFirstname = contactFirstname.value,
        contactLastname = contactLastname.value,
        contactAddress = contactAddress.value,
        contactPostal = contactPostal.value,
        contactPhone = contactPhone.value,
        email = email.value,
        password = password.value,
        confirmPassword = confirmPassword.value,
        agree = agree.value,
    )

    override fun navigateBack() {
        stateHolder.preLoginNavigation.replaceAll(PreLoginConfig.Login(userType))
    }

    private fun registerShop() {
        scope.launch {
            repository.register(
                RegisterDto(
                    customer = Customer(
                        email = email.value.trim().replace(" ", ""),
                        firstname = contactFirstname.value.trim(),
                        lastname = contactLastname.value.trim(),
                        addresses = arrayListOf(
                            Addresses(
                                countryId = "BA",
                                street = arrayListOf(
                                    contactAddress.value.trim(),
                                    contactAddress.value.trim()
                                ),
                                telephone = contactPhone.value.trim(),
                                postcode = contactPostal.value.trim(),
                                firstname = contactFirstname.value.trim(),
                                lastname = contactLastname.value.trim(),
                                city = companyCity.value.trim(),
                                defaultBilling = true,
                                defaultShipping = false
                            ),
                            Addresses(
                                countryId = "BA",
                                street = arrayListOf(
                                    contactAddress.value.trim(),
                                    contactAddress.value.trim()
                                ),
                                telephone = contactPhone.value.trim(),
                                postcode = contactPostal.value.trim(),
                                firstname = contactFirstname.value.trim(),
                                lastname = contactLastname.value.trim(),
                                city = companyCity.value.trim(),
                                defaultBilling = false,
                                defaultShipping = true
                            )
                        ),
                        customAttributes = arrayListOf(
                            CustomAttributes(
                                "b2b_entitet",
                                JsonPrimitive(companyEntity.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_kanton",
                                JsonPrimitive(companyCanton.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_grad",
                                JsonPrimitive(companyCity.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_id",
                                JsonPrimitive(companyId.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_pdv_broj",
                                JsonPrimitive(companyPdv.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_velicina_objekta",
                                JsonPrimitive(companySize.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_tip_objekta",
                                JsonPrimitive(companyType.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_broj_zaposlenih",
                                JsonPrimitive(companyEmployees.value.trim())
                            ),
                            CustomAttributes(
                                "b2b_pravno_lice",
                                JsonPrimitive(companyName.value.trim())
                            )
                        )

                    ),
                    password = password.value
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        showMessage(
                            "Dobrodošli na Karika.ba\n" +
                                    "Nakon provjere informacija za registraciju, vaš nalog će biti odobren."
                        )
                        repository.confirmRegister(
                            ConfirmRegistration(
                                publicName = companyName.value.trim(),
                                email = email.value,
                                password = password.value,
                                userType = "customer"
                            )
                        ).collect()
                        preLoginBack()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    private fun registerVendor() {
        scope.launch {
            repository.registerVendor(
                VendorRegisterRequest(
                    customerGroups.value.joinToString(separator = ",") { "|${it.unit()}|" }
                        .trimIndent(),
                    customerRegions.value.joinToString(separator = ",") { "|${it.unit()}|" }
                        .trimIndent(),
                    KarikaConstants.entries.findLast { it.name == companyEntity.value }?.id?.toString()
                        ?: "",
                    companyCanton.value.trim(),
                    companyCity.value,
                    companyName.value.trim(),
                    companyPdv.value.trim(),
                    companyId.value.trim(),
                    email.value.trim().replace(" ", ""),
                    password.value,
                    confirmPassword.value,
                    contactFirstname.value.trim(),
                    contactLastname.value.trim(),
                    contactPhone.value.trim()
                )
            ).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        showLoader()
                    }

                    is ResultState.Success -> {
                        hideLoader()
                        showMessage(
                            "Registracija je uspješna, dobrodošli na Karika.ba. Nakon provjere validnosti unesenih podataka Vaš nalog će biti odobren."
                        )
                        repository.confirmRegister(
                            ConfirmRegistration(
                                publicName = companyName.value,
                                email = email.value,
                                password = password.value,
                                userType = "vendor"
                            )
                        ).collect()
                        preLoginBack()
                    }

                    is ResultState.Error -> {
                        hideLoader()
                        showMessage(result.message)
                    }
                }
            }
        }
    }

    override fun getColor() = if (userType.isShop()) KarikaColors.Primary else KarikaColors.Blue
}
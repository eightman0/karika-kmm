package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.runtime.mutableStateOf
import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.RegistrationRepository
import karika.distribucija.ba.domain.model.Addresses
import karika.distribucija.ba.domain.model.ConfirmRegistration
import karika.distribucija.ba.domain.model.CustomAttributes
import karika.distribucija.ba.domain.model.Customer
import karika.distribucija.ba.domain.model.RegisterDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.domain.model.VendorRegisterRequest
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.isEmailFormat
import karika.distribucija.ba.ui.components.isPhoneFormat
import karika.distribucija.ba.ui.components.isPostalCodeValid
import karika.distribucija.ba.ui.view.main.profile.account.isPassComplex
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

class RegistrationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder,
    val userType: KarikaType,
) : CommonComponent(componentContext, stateHolder) {
    private val repository = RegistrationRepository()

    override val title: String
        get() = if (userType.isShop()) "Registracija kupca" else "Registracija dobavljača"

    val companyName = mutableStateOf("")
    val companyId = mutableStateOf("")
    val companyPdv = mutableStateOf("")
    val companyEntity = mutableStateOf("")
    val companyCanton = mutableStateOf("")
    val companyCity = mutableStateOf("")
    val companySize = mutableStateOf("")
    val companyType = mutableStateOf("")
    val companyEmployees = mutableStateOf("")

    val contactFirstname = mutableStateOf("")
    val contactLastname = mutableStateOf("")
    val contactAddress = mutableStateOf("")
    val contactPostal = mutableStateOf("")
    val contactPhone = mutableStateOf("")

    val email = mutableStateOf("")
    val password = mutableStateOf("")
    val confirmPassword = mutableStateOf("")
    val agree = mutableStateOf(false)

    val entities = mutableStateOf(KarikaConstants.entries.map { it.name })
    val canton = mutableStateOf<List<String>>(emptyList())
    val city = mutableStateOf<List<String>>(emptyList())
    val customerRegions = mutableStateOf(stateHolder.commonHandler.config.value.customerRegionList)
    val customerGroups = mutableStateOf(stateHolder.commonHandler.config.value.customerGroupList)

    fun register() {
        if (companyName.value.isEmpty()) {
            showMessage("Naziv pravnog lica je obavezno polje!")
            return
        }
        if (companyId.value.isEmpty()) {
            showMessage("ID broj je obavezno polje!")
            return
        }
        if (companyEntity.value.isEmpty()) {
            showMessage("Entitet je obavezno polje!")
            return
        } else {
            if (companyEntity.value == "Federacija") {
                if (companyCanton.value.isEmpty()) {
                    showMessage("Kanton je obavezno polje!")
                    return
                }
            }

            if (companyCity.value.isEmpty()) {
                showMessage("Grad je obavezno polje!")
                return
            }
        }

        if (userType.isShop()) {
            if (companySize.value.isEmpty()) {
                showMessage("Veličina objekta je obavezno polje!")
                return
            }
            if (companyType.value.isEmpty()) {
                showMessage("Tip objekta je obavezno polje!")
                return
            }
        }
        if (contactFirstname.value.isEmpty()) {
            showMessage("Ime je obavezno polje!")
            return
        }
        if (contactLastname.value.isEmpty()) {
            showMessage("Prezime je obavezno polje!")
            return
        }
        if (userType.isShop()) {
            if (contactAddress.value.isEmpty()) {
                showMessage("Adresa je obavezno polje!")
                return
            }
            if (contactPostal.value.isEmpty()) {
                showMessage("Poštanski broj je obavezno polje!")
                return
            }
            if (!contactPostal.value.isPostalCodeValid()) {
                showMessage("Poštanski broj nije u odgovarajućem formatu!")
                return
            }
        }
        if (contactPhone.value.isEmpty()) {
            showMessage("Broj telefona je obavezno polje!")
            return
        }
        if (!contactPhone.value.isPhoneFormat()) {
            showMessage("Broj telefona nije u odgovarajućem formatu!")
            return
        }
        if (email.value.isEmpty()) {
            showMessage("Email adresa je obavezno polje!")
            return
        }
        if (!email.value.isEmailFormat()) {
            showMessage("Email nije u odgovarajućem formatu!")
            return
        }
        if (password.value.isEmpty()) {
            showMessage("Lozinka je obavezno polje!")
            return
        }
        if (password.value.length < 8) {
            showMessage("Lozinka mora imati najmanje 8 karaktera.")
            return
        }
        if (!password.value.isPassComplex()) {
            showMessage("Lozinka mora sadržavati najmanje jedno veliko slovo i jedan broj.")
            return
        }
        if (password.value != confirmPassword.value) {
            showMessage("Lozinke se ne podudaraju.")
            return
        }
        if (!agree.value) {
            showMessage("Morate prihvatiti uslove korištenja!")
            return
        }
        if (userType.isShop()) {
            registerShop()
            return
        }
        registerVendor()
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

    fun getColor() = if (userType.isShop()) KarikaColors.Primary else KarikaColors.Blue
}
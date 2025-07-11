package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.pop
import karika.distribucija.ba.domain.api.RegistrationRepository
import karika.distribucija.ba.domain.model.Addresses
import karika.distribucija.ba.domain.model.CustomAttributes
import karika.distribucija.ba.domain.model.Customer
import karika.distribucija.ba.domain.model.RegisterDto
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.components.isEmailFormat
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.launch
import kotlinx.serialization.json.JsonPrimitive

class RegistrationComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {
    private val repository = RegistrationRepository()

    override val title: String
        get() = "Registracija kupca"

    val companyName = mutableStateOf("")
    val companyId = mutableStateOf("")
    val companyPdv = mutableStateOf("")
    val companyEntity = mutableStateOf("")
    val companyCanton = mutableStateOf("")
    val companyCity = mutableStateOf("")
    val companyMunicipality = mutableStateOf("")
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
    val municipality = mutableStateOf<List<String>>(emptyList())

    fun forgotPassword() {

    }

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
                if (companyCity.value.isEmpty()) {
                    showMessage("Grad je obavezno polje!")
                    return
                }
            }
            if (companyEntity.value == "Republika Srpska") {
                if (companyMunicipality.value.isEmpty()) {
                    showMessage("Općina je obavezno polje!")
                    return
                }
            }
            if (companyEntity.value == "Distrikt Brčko") {
                if (companyMunicipality.value.isEmpty()) {
                    showMessage("Opština je obavezno polje!")
                    return
                }
            }
        }

        if (companySize.value.isEmpty()) {
            showMessage("Veličina objekta je obavezno polje!")
            return
        }
        if (companyType.value.isEmpty()) {
            showMessage("Tip objekta je obavezno polje!")
            return
        }
        if (contactFirstname.value.isEmpty()) {
            showMessage("Ime je obavezno polje!")
            return
        }
        if (contactLastname.value.isEmpty()) {
            showMessage("Prezime je obavezno polje!")
            return
        }
        if (contactAddress.value.isEmpty()) {
            showMessage("Adresa je obavezno polje!")
            return
        }
        if (contactPostal.value.isEmpty()) {
            showMessage("Poštanski broj je obavezno polje!")
            return
        }
        if (contactPhone.value.isEmpty()) {
            showMessage("Broj telefona je obavezno polje!")
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
            showMessage("Šifra je obavezno polje!")
            return
        }
        if (password.value != confirmPassword.value) {
            showMessage("Ne poklapaju se šifre!")
            return
        }
        if (!agree.value) {
            showMessage("Morate prihvatiti uslove korištenja!")
            return
        }
        iOScope.launch {
            repository.register(
                RegisterDto(
                    customer = Customer(
                        email = email.value.trim(),
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
                                "b2b_opcina",
                                JsonPrimitive(companyMunicipality.value.trim())
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
                        back()
                    }

                    else -> {
                        hideLoader()
                        showMessage("Email adresa već postoji u sistemu, molimo pokušajte sa drugom adresom.")
                    }
                }
            }
        }
    }

    fun back() {
        mainScope.launch {
            stateHolder.preLoginNavigation.pop()
        }
    }
}
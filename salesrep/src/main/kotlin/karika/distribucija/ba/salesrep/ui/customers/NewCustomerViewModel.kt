package karika.distribucija.ba.salesrep.ui.customers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import karika.distribucija.ba.salesrep.api.SalesRepository
import karika.distribucija.ba.salesrep.model.CustomAttribute
import karika.distribucija.ba.salesrep.model.NewCustomerAddress
import karika.distribucija.ba.salesrep.model.NewCustomerPayload
import karika.distribucija.ba.salesrep.model.NewCustomerRequest
import karika.distribucija.ba.salesrep.model.NewCustomerRequestBody
import karika.distribucija.ba.salesrep.model.ResultState
import karika.distribucija.ba.salesrep.util.KarikaConstants
import karika.distribucija.ba.salesrep.util.isEmailFormatValid
import karika.distribucija.ba.salesrep.util.isPhoneFormatValid
import karika.distribucija.ba.salesrep.util.isPostalCodeValidBa
import kotlinx.coroutines.launch

/** Mirrors composeApp's SalesNewCustomerComponent.kt (B2B onboarding form + validation + save()). */
class NewCustomerViewModel : ViewModel() {

    private val repository = SalesRepository()

    val entityOptions: List<String> = KarikaConstants.entries.map { it.name }
    val storeSizeOptions: List<String> = KarikaConstants.companySizes
    val storeTypeOptions: List<String> = KarikaConstants.companyTypes

    var company = ""
    var idNumber = ""
    var vatNumber = ""
    var street = ""
    var postcode = ""
    var entity: String? = null
    var canton: String? = null
    var city: String? = null
    var storeSize: String? = null
    var storeType: String? = null
    var employeeCount = ""
    var firstname = ""
    var lastname = ""
    var phone = ""
    var email = ""

    private val _cantonOptions = MutableLiveData<List<String>>(emptyList())
    val cantonOptions: LiveData<List<String>> = _cantonOptions

    private val _cityOptions = MutableLiveData<List<String>>(emptyList())
    val cityOptions: LiveData<List<String>> = _cityOptions

    private val _isSaving = MutableLiveData(false)
    val isSaving: LiveData<Boolean> = _isSaving

    private val _errorMessage = MutableLiveData<String?>(null)
    val errorMessage: LiveData<String?> = _errorMessage

    private val _showInviteDialog = MutableLiveData(false)
    val showInviteDialog: LiveData<Boolean> = _showInviteDialog

    private val _saved = MutableLiveData(false)
    val saved: LiveData<Boolean> = _saved

    fun dismissInviteDialog() {
        _showInviteDialog.value = false
    }

    fun onEntitySelected(value: String) {
        entity = value
        canton = null
        city = null
        _cityOptions.value = emptyList()
        _cantonOptions.value = when (value) {
            "Distrikt Brčko" -> {
                city = "Brčko Grad"
                emptyList()
            }
            else -> KarikaConstants.cantons(value)
        }
    }

    fun onCantonSelected(value: String) {
        canton = value
        city = null
        _cityOptions.value = if (entity == "Federacija") KarikaConstants.cities(value) else emptyList()
    }

    fun onCitySelected(value: String) {
        city = value
    }

    /** Label for the canton/općina field based on selected entity. */
    fun cantonLabel(): String = if (entity == "Federacija") "Kanton*" else "Općina*"

    fun save() {
        if (company.isEmpty()) { _errorMessage.value = "Naziv pravnog lica je obavezno polje!"; return }
        if (idNumber.isEmpty()) { _errorMessage.value = "ID broj je obavezno polje!"; return }

        val entityValue = entity ?: run { _errorMessage.value = "Entitet je obavezno polje!"; return }

        if (entityValue == "Federacija" && canton.isNullOrEmpty()) {
            _errorMessage.value = "Kanton je obavezno polje!"; return
        }
        if (city.isNullOrEmpty()) { _errorMessage.value = "Grad je obavezno polje!"; return }

        val storeSizeValue = storeSize ?: run { _errorMessage.value = "Veličina objekta je obavezno polje!"; return }
        val storeTypeValue = storeType ?: run { _errorMessage.value = "Tip objekta je obavezno polje!"; return }

        if (firstname.isEmpty()) { _errorMessage.value = "Ime je obavezno polje!"; return }
        if (lastname.isEmpty()) { _errorMessage.value = "Prezime je obavezno polje!"; return }
        if (street.isEmpty()) { _errorMessage.value = "Adresa je obavezno polje!"; return }
        if (postcode.isEmpty()) { _errorMessage.value = "Poštanski broj je obavezno polje!"; return }
        if (!postcode.isPostalCodeValidBa()) { _errorMessage.value = "Poštanski broj nije u odgovarajućem formatu!"; return }
        if (phone.isEmpty()) { _errorMessage.value = "Broj telefona je obavezno polje!"; return }
        if (!phone.isPhoneFormatValid()) { _errorMessage.value = "Broj telefona nije u odgovarajućem formatu!"; return }
        if (email.isEmpty()) { _errorMessage.value = "Email adresa je obavezno polje!"; return }
        if (!email.isEmailFormatValid()) { _errorMessage.value = "Email nije u odgovarajućem formatu!"; return }

        val entityId = KarikaConstants.entries.find { it.name == entityValue }?.id ?: 1
        val cityValue = city ?: canton ?: ""

        val customAttributes = buildList {
            add(CustomAttribute("b2b_pravno_lice", company.trim()))
            add(CustomAttribute("b2b_id", idNumber.trim()))
            add(CustomAttribute("b2b_velicina_objekta", storeSizeValue))
            add(CustomAttribute("b2b_tip_objekta", storeTypeValue))
            add(CustomAttribute("b2b_entitet", entityId.toString()))
            add(CustomAttribute("b2b_grad", cityValue))
            if (entityId == 1) {
                add(CustomAttribute("b2b_kanton", canton ?: ""))
            }
            vatNumber.trim().takeIf { it.isNotBlank() }?.let { add(CustomAttribute("b2b_pdv_broj", it)) }
            employeeCount.trim().takeIf { it.isNotBlank() }?.let { add(CustomAttribute("b2b_broj_zaposlenih", it)) }
        }

        val request = NewCustomerRequest(
            request = NewCustomerRequestBody(
                customer = NewCustomerPayload(
                    email = email.trim(),
                    firstname = firstname.trim(),
                    lastname = lastname.trim(),
                    addresses = listOf(
                        NewCustomerAddress(
                            countryId = "BA",
                            street = listOf(street.trim()),
                            postcode = postcode.trim(),
                            telephone = phone.trim(),
                            city = cityValue,
                            firstname = firstname.trim(),
                            lastname = lastname.trim(),
                            defaultBilling = true,
                            defaultShipping = false
                        ),
                        NewCustomerAddress(
                            countryId = "BA",
                            street = listOf(street.trim()),
                            postcode = postcode.trim(),
                            telephone = phone.trim(),
                            city = cityValue,
                            firstname = firstname.trim(),
                            lastname = lastname.trim(),
                            defaultBilling = false,
                            defaultShipping = true
                        )
                    ),
                    customAttributes = customAttributes
                ),
                autoAssignToCaller = true
            )
        )

        viewModelScope.launch {
            repository.createCustomer(request).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        _saved.value = true
                    }

                    is ResultState.Error -> {
                        _isSaving.value = false
                        if (result.message == "Kupac sa ovim email-om već postoji.") {
                            _showInviteDialog.value = true
                        } else {
                            _errorMessage.value = result.message
                        }
                    }
                }
            }
        }
    }
}

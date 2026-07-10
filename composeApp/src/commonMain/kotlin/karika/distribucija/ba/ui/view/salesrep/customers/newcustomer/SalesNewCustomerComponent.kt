package karika.distribucija.ba.ui.view.salesrep.customers.newcustomer

import com.arkivanov.decompose.ComponentContext
import karika.distribucija.ba.domain.api.SalesRepository
import karika.distribucija.ba.domain.model.CustomAttribute
import karika.distribucija.ba.domain.model.NewCustomerAddress
import karika.distribucija.ba.domain.model.NewCustomerPayload
import karika.distribucija.ba.domain.model.NewCustomerRequest
import karika.distribucija.ba.domain.model.ResultState
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SalesNewCustomerComponent(
    componentContext: ComponentContext,
    stateHolder: KarikaStateHolder
) : CommonComponent(componentContext, stateHolder) {

    private val repository = SalesRepository()

    // ── Section 1: Legal entity ───────────────────────────────────────────────
    private val _company = MutableStateFlow("")
    val company = _company.asStateFlow()

    private val _idNumber = MutableStateFlow("")
    val idNumber = _idNumber.asStateFlow()

    private val _vatNumber = MutableStateFlow("")
    val vatNumber = _vatNumber.asStateFlow()

    private val _street = MutableStateFlow("")
    val street = _street.asStateFlow()

    private val _postcode = MutableStateFlow("")
    val postcode = _postcode.asStateFlow()

    // ── Location ──────────────────────────────────────────────────────────────
    /** Entity names from KarikaConstants */
    val entityOptions: List<String> = KarikaConstants.entries.map { it.name }

    private val _entity = MutableStateFlow<String?>(null)
    val entity = _entity.asStateFlow()

    /** Kanton options (FBiH) or Općina options (RS), empty for Brčko */
    private val _cantonOptions = MutableStateFlow<List<String>>(emptyList())
    val cantonOptions = _cantonOptions.asStateFlow()

    /** Selected kanton (FBiH) or općina (RS) */
    private val _canton = MutableStateFlow<String?>(null)
    val canton = _canton.asStateFlow()

    /** Grad options — only populated for FBiH after kanton is selected */
    private val _cityOptions = MutableStateFlow<List<String>>(emptyList())
    val cityOptions = _cityOptions.asStateFlow()

    private val _city = MutableStateFlow<String?>(null)
    val city = _city.asStateFlow()

    // ── Store info ────────────────────────────────────────────────────────────
    val storeSizeOptions: List<String> = KarikaConstants.companySizes
    val storeTypeOptions: List<String> = KarikaConstants.companyTypes

    private val _storeSize = MutableStateFlow<String?>(null)
    val storeSize = _storeSize.asStateFlow()

    private val _storeType = MutableStateFlow<String?>(null)
    val storeType = _storeType.asStateFlow()

    private val _employeeCount = MutableStateFlow("")
    val employeeCount = _employeeCount.asStateFlow()

    // ── Section 2: Contact person ─────────────────────────────────────────────
    private val _firstname = MutableStateFlow("")
    val firstname = _firstname.asStateFlow()

    private val _lastname = MutableStateFlow("")
    val lastname = _lastname.asStateFlow()

    private val _phone = MutableStateFlow("")
    val phone = _phone.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    // ── Save state ────────────────────────────────────────────────────────────
    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    // ── Setters ───────────────────────────────────────────────────────────────
    fun setCompany(v: String) { _company.value = v }
    fun setIdNumber(v: String) { _idNumber.value = v }
    fun setVatNumber(v: String) { _vatNumber.value = v }
    fun setStreet(v: String) { _street.value = v }
    fun setPostcode(v: String) { _postcode.value = v }

    fun setEntity(v: String?) {
        _entity.value = v
        _canton.value = null
        _city.value = null
        _cityOptions.value = emptyList()
        when (v) {
            "Federacija" -> {
                _cantonOptions.value = KarikaConstants.cantons("Federacija")
                // Grad populated after kanton selected
            }
            "Republika Srpska" -> {
                // RS municipalities shown as Općina; no sub-city level
                _cantonOptions.value = KarikaConstants.cantons("Republika Srpska")
            }
            "Distrikt Brčko" -> {
                // No sub-picker; city is auto "Brčko Grad"
                _cantonOptions.value = emptyList()
                _city.value = "Brčko Grad"
            }
            else -> _cantonOptions.value = emptyList()
        }
    }

    fun setCanton(v: String?) {
        _canton.value = v
        _city.value = null
        // Only FBiH has cities under cantons
        if (_entity.value == "Federacija" && v != null) {
            _cityOptions.value = KarikaConstants.cities(v)
        } else {
            _cityOptions.value = emptyList()
        }
    }

    fun setCity(v: String?) { _city.value = v }
    fun setStoreSize(v: String?) { _storeSize.value = v }
    fun setStoreType(v: String?) { _storeType.value = v }
    fun setEmployeeCount(v: String) { _employeeCount.value = v.filter { it.isDigit() } }
    fun setFirstname(v: String) { _firstname.value = v }
    fun setLastname(v: String) { _lastname.value = v }
    fun setPhone(v: String) { _phone.value = v }
    fun setEmail(v: String) { _email.value = v }

    // ── Validation helpers ────────────────────────────────────────────────────

    /** Label for the canton/općina field based on selected entity */
    fun cantonLabel(): String = when (_entity.value) {
        "Federacija" -> "Kanton*"
        else -> "Općina*"
    }

    // ── Save ──────────────────────────────────────────────────────────────────
    fun save() {
        val entity = _entity.value ?: run { showErrorMessage("Odaberite entitet"); return }
        val storeSize = _storeSize.value ?: run { showErrorMessage("Odaberite veličinu objekta"); return }
        val storeType = _storeType.value ?: run { showErrorMessage("Odaberite tip objekta"); return }

        if (_company.value.isBlank()) { showErrorMessage("Unesite naziv pravnog lica"); return }
        if (_idNumber.value.isBlank()) { showErrorMessage("Unesite ID broj"); return }
        if (_street.value.isBlank()) { showErrorMessage("Unesite adresu"); return }
        if (_postcode.value.isBlank()) { showErrorMessage("Unesite poštanski broj"); return }

        // Location validation
        if (entity == "Federacija") {
            if (_canton.value.isNullOrBlank()) { showErrorMessage("Odaberite kanton"); return }
            if (_city.value.isNullOrBlank()) { showErrorMessage("Odaberite grad"); return }
        } else if (entity == "Republika Srpska") {
            if (_canton.value.isNullOrBlank()) { showErrorMessage("Odaberite općinu"); return }
        }

        if (_firstname.value.isBlank()) { showErrorMessage("Unesite ime"); return }
        if (_lastname.value.isBlank()) { showErrorMessage("Unesite prezime"); return }
        if (_phone.value.isBlank()) { showErrorMessage("Unesite broj telefona"); return }
        if (_email.value.isBlank()) { showErrorMessage("Unesite email adresu"); return }

        val entityId = KarikaConstants.entries.find { it.name == entity }?.id ?: 1
        val city = _city.value ?: _canton.value ?: ""

        val customAttributes = buildList {
            add(CustomAttribute("b2b_pravno_lice", _company.value.trim()))
            add(CustomAttribute("b2b_id", _idNumber.value.trim()))
            add(CustomAttribute("b2b_velicina_objekta", storeSize))
            add(CustomAttribute("b2b_tip_objekta", storeType))
            add(CustomAttribute("b2b_entitet", entityId.toString()))
            add(CustomAttribute("b2b_grad", city))
            if (entityId == 1) {
                // Federacija — kanton is required
                add(CustomAttribute("b2b_kanton", _canton.value ?: ""))
            }
            val vat = _vatNumber.value.trim()
            if (vat.isNotBlank()) add(CustomAttribute("b2b_pdv_broj", vat))
            val empCount = _employeeCount.value.trim()
            if (empCount.isNotBlank()) add(CustomAttribute("b2b_broj_zaposlenih", empCount))
        }

        val request = NewCustomerRequest(
            customer = NewCustomerPayload(
                email = _email.value.trim(),
                firstname = _firstname.value.trim(),
                lastname = _lastname.value.trim(),
                addresses = listOf(
                    NewCustomerAddress(
                        street = _street.value.trim(),
                        postcode = _postcode.value.trim(),
                        telephone = _phone.value.trim(),
                        city = city
                    )
                ),
                customAttributes = customAttributes
            )
        )

        scope.launch {
            repository.createCustomer(request).collect { result ->
                when (result) {
                    is ResultState.Loading -> _isSaving.value = true
                    is ResultState.Success -> {
                        _isSaving.value = false
                        salesRepBack()
                    }
                    is ResultState.Error -> {
                        _isSaving.value = false
                        showErrorMessage(result.message)
                    }
                }
            }
        }
    }

    fun goBack() = salesRepBack()
}

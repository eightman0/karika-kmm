package karika.distribucija.ba.ui.view.prelogin

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.mutableStateOf
import karika.distribucija.ba.domain.model.KarikaUnit
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.prelogin.landing.LandingComponent
import karika.distribucija.ba.ui.view.prelogin.login.LoginComponent
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationComponent
import karika.distribucija.ba.ui.view.prelogin.registration.input
import karika.distribucija.ba.ui.view.prelogin.registration.validationError
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.flow.MutableStateFlow

class FakeLandingComponent : LandingComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val promotedLogos = MutableStateFlow<List<PromotedVendor>>(emptyList())

    val loginRequests = mutableListOf<KarikaType>()
    var guestRequests = 0
    var bannerLoads = 0

    override fun navigateLogin(type: KarikaType) {
        loginRequests += type
    }

    override fun continueAsGuest() {
        guestRequests++
    }

    override fun loadBanners() {
        bannerLoads++
    }
}

class FakeLoginComponent(
    private val userType: KarikaType = KarikaType.SHOP,
    savedEmail: String = "",
    savedPassword: String = "",
) : LoginComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val forgotPassSheet = mutableStateOf(false)
    override val email = mutableStateOf(savedEmail)
    override val pass = mutableStateOf(savedPassword)
    override val rememberMe = mutableStateOf(savedPassword.isNotEmpty())
    override val formValid = mutableStateOf(savedPassword.isNotEmpty())

    var loginRequests = 0
    val forgotPasswordRequests = mutableListOf<String>()
    var registrationRequests = 0
    var landingRequests = 0

    override fun login(emailToken: String, token: String, callback: () -> Unit) {
        loginRequests++
    }

    override fun forgotPassword(email: String) {
        forgotPasswordRequests += email
    }

    override fun forgotPassword() {
        forgotPassSheet.negate()
    }

    override fun navigateRegistration() {
        registrationRequests++
    }

    override fun navigateLanding() {
        landingRequests++
    }

    override fun title() = if (isShop()) "Prijava kupac" else "Prijava dobavljač"

    override fun wifi() {}

    override fun isShop() = userType == KarikaType.SHOP

    override fun exitKiosk() {}
}

/**
 * Runs the real validation on submit, so a test can drive the form through the UI and check
 * which rule (if any) stopped it, without the network.
 */
class FakeRegistrationComponent(
    override val userType: KarikaType = KarikaType.SHOP,
    override val customerGroupOptions: List<KarikaUnit> = emptyList(),
    override val customerRegionOptions: List<KarikaUnit> = emptyList(),
) : RegistrationComponent {
    override val snackbarHostState = SnackbarHostState()
    override val loader = MutableStateFlow(false)
    override val title = if (userType.isShop()) "Registracija kupca" else "Registracija dobavljača"

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
    override val customerGroups = mutableStateOf(customerGroupOptions)
    override val customerRegions = mutableStateOf(customerRegionOptions)

    /** Every submit, with the validation message it produced (null = would be sent). */
    val submits = mutableListOf<String?>()
    var backRequests = 0

    override fun register() {
        submits += input().validationError(userType.isShop())
    }

    override fun navigateBack() {
        backRequests++
    }

    override fun getColor() = if (userType.isShop()) KarikaColors.Primary else KarikaColors.Blue
}

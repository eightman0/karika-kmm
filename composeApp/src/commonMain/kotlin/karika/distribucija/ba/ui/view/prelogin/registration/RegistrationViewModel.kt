package karika.distribucija.ba.ui.view.prelogin.registration

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import karika.distribucija.ba.domain.LoginRepository
import karika.distribucija.ba.ui.common.CommonViewModel
import karika.distribucija.ba.ui.common.KarikaStateHolder
import kotlinx.coroutines.launch

class RegistrationViewModel(navController: NavController, stateHolder: KarikaStateHolder) :
    CommonViewModel(navController, stateHolder) {
    private val repository = LoginRepository()

    override val title: String
        get() = "Registracija kupca"

    val companyName = mutableStateOf("")
    val companyId = mutableStateOf("")
    val companyPdv = mutableStateOf("")
    val companyEntity = mutableStateOf("")
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
        viewModelScope.launch {

        }
    }
}
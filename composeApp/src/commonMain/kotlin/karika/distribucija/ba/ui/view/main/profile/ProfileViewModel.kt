package karika.distribucija.ba.ui.view.main.profile

import androidx.navigation.NavController
import karika.distribucija.ba.Screen
import karika.distribucija.ba.ui.common.CommonViewModel
import karika.distribucija.ba.ui.common.KarikaStateHolder

class ProfileViewModel(
    navController: NavController,
    stateHolder: KarikaStateHolder,
    val navigate: (Screen) -> Unit
) :
    CommonViewModel(navController, stateHolder) {

    fun navigateToOrders() {
        navigate.invoke(Screen.Orders)
    }
}
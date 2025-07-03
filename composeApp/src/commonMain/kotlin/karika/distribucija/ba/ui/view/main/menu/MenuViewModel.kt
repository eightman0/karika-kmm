package karika.distribucija.ba.ui.view.main.menu

import androidx.navigation.NavController
import karika.distribucija.ba.Screen
import karika.distribucija.ba.ui.common.CommonViewModel
import karika.distribucija.ba.ui.common.KarikaStateHolder

class MenuViewModel(
    navController: NavController,
    stateHolder: KarikaStateHolder,
    val navigate: (Screen) -> Unit
) : CommonViewModel(navController, stateHolder) {

    fun categories() {
        navigate.invoke(Screen.Orders)
    }

    fun vendors() {

    }

    fun blog() {

    }

    fun karika() {

    }
}
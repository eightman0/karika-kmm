package karika.distribucija.ba

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import karika.distribucija.ba.ui.common.KarikaStateHolder
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.main.MainView
import karika.distribucija.ba.ui.view.main.MainViewModel
import karika.distribucija.ba.ui.view.order.OrderView
import karika.distribucija.ba.ui.view.order.OrderViewModel
import karika.distribucija.ba.ui.view.prelogin.landing.LandingView
import karika.distribucija.ba.ui.view.prelogin.landing.LandingViewModel
import karika.distribucija.ba.ui.view.prelogin.login.LoginView
import karika.distribucija.ba.ui.view.prelogin.login.LoginViewModel
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationView
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalComposeUiApi::class)
@Composable
@Preview
fun App() {
    val stateHolder = KarikaStateHolder()
    val navController = rememberNavController()
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = KarikaColors.Primary
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Landing.route,
            enterTransition = { EnterTransition.None },
            exitTransition = { ExitTransition.None }
        ) {
            composable(Screen.Landing.route) {
                LandingView(LandingViewModel(navController, stateHolder).asState())
            }
            composable(Screen.Login.route) {
                LoginView(LoginViewModel(navController, stateHolder).asState())
            }
            composable(Screen.Registration.route) {
                RegistrationView(RegistrationViewModel(navController, stateHolder).asState())
            }
            composable(Screen.Main.route) {
                MainView(MainViewModel(navController, stateHolder).asState())
                BackHandler(true) {}
            }
            composable(Screen.Orders.route) {
                OrderView(OrderViewModel(navController, stateHolder).asState())
            }
        }
    }
}

sealed class Screen(val route: String) {

    data object Landing : Screen("landing")
    data object Login : Screen("login")
    data object Registration : Screen("registration")
    data object Main : Screen("main")
    data object Orders : Screen("orders")

    // MAIN
    data object Home : Screen("home")
    data object Menu : Screen("category")
    data object Vendor : Screen("vendor")
    data object Cart : Screen("cart")
    data object Profile : Screen("profile")
}
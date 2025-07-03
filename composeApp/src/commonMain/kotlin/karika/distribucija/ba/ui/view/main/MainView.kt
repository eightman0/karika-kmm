package karika.distribucija.ba.ui.view.main

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import karika.distribucija.ba.Screen
import karika.distribucija.ba.ui.components.BottomBar
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.TopBar
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.main.cart.CartView
import karika.distribucija.ba.ui.view.main.cart.CartViewModel
import karika.distribucija.ba.ui.view.main.home.HomeView
import karika.distribucija.ba.ui.view.main.home.HomeViewModel
import karika.distribucija.ba.ui.view.main.menu.MenuView
import karika.distribucija.ba.ui.view.main.menu.MenuViewModel
import karika.distribucija.ba.ui.view.main.profile.ProfileView
import karika.distribucija.ba.ui.view.main.profile.ProfileViewModel
import karika.distribucija.ba.ui.view.main.vendor.VendorView
import karika.distribucija.ba.ui.view.main.vendor.VendorViewModel

@Composable
fun MainView(viewModel: MainViewModel) {
    val navController = rememberNavController()

    Box(
        modifier = Modifier
            .fillMaxSize(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = KarikaColors.White)
                .height(100.dp)
        )
        KarikaScaffold(
            modifier = Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize(),
            hostState = viewModel.snackbarHostState,
            topBar = { TopBar() },
            bottomBar = { BottomBar(navController) },
            viewModel = viewModel
        ) {
            NavHost(
                modifier = Modifier
                    .padding(it),
                navController = navController,
                startDestination = Screen.Home.route,
                enterTransition = { EnterTransition.None },
                exitTransition = { ExitTransition.None }
            ) {
                composable(Screen.Home.route) {
                    HomeView(HomeViewModel(navController, viewModel.stateHolder).asState())
                }
                composable(Screen.Menu.route) {
                    MenuView(
                        MenuViewModel(navController, viewModel.stateHolder) {
                            viewModel.navigate(it)
                        }.asState()
                    )
                }
                composable(Screen.Vendor.route) {
                    VendorView(
                        VendorViewModel(navController, viewModel.stateHolder) {
                            viewModel.navigate(it)
                        }.asState()
                    )
                }
                composable(Screen.Cart.route) {
                    CartView(
                        CartViewModel(navController, viewModel.stateHolder) {
                            viewModel.navigate(it)
                        }.asState()
                    )
                }
                composable(Screen.Profile.route) {
                    ProfileView(
                        ProfileViewModel(navController, viewModel.stateHolder) {
                            viewModel.navigate(it)
                        }.asState()
                    )
                }
            }
        }
    }
}
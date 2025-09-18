package karika.distribucija.ba.ui.common

import androidx.compose.material3.SnackbarHostState
import com.arkivanov.decompose.router.stack.StackNavigation
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.view.distributer.dashboard.DashConfig
import karika.distribucija.ba.ui.view.main.MainConfig
import karika.distribucija.ba.ui.view.prelogin.PreLoginConfig


open class NavigationHandler() : CartHandler() {
    val appNavigation = StackNavigation<AppConfig>()
    val mainNavigation = StackNavigation<MainConfig>()
    val preLoginNavigation = StackNavigation<PreLoginConfig>()
    val dashNavigation = StackNavigation<DashConfig>()

    val hostState = SnackbarHostState()
}
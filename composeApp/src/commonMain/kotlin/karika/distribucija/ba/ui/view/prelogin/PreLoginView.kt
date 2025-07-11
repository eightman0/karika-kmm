package karika.distribucija.ba.ui.view.prelogin

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.extensions.compose.stack.Children
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.view.prelogin.landing.LandingView
import karika.distribucija.ba.ui.view.prelogin.login.LoginView
import karika.distribucija.ba.ui.view.prelogin.registration.RegistrationView


@Composable
fun PreLoginView(component: PreLoginComponent) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = KarikaColors.Primary
    ) {
        Children(
            stack = component.stack
        ) {
            when (val child = it.instance) {
                is PreLoginChild.Landing -> LandingView(child.component)
                is PreLoginChild.Login -> LoginView(child.component)
                is PreLoginChild.Registration -> RegistrationView(child.component)
            }
        }
    }
}
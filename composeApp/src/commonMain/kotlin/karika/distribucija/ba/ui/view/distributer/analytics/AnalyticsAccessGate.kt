package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import karika.distribucija.ba.ui.common.state.KarikaStateHolder
import karika.distribucija.ba.ui.view.distributer.VendorAccent

/** Analytics is the start screen, but it may only show with the analytics capability: until `me`
 * is loaded (and while the dashboard redirects away without it) show a loader instead. */
@Composable
fun AnalyticsAccessGate(stateHolder: KarikaStateHolder, content: @Composable () -> Unit) {
    val access by stateHolder.salesSpecificHandler.analyticsAccess.collectAsState()
    if (access == true) {
        content()
    } else {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = VendorAccent)
        }
    }
}

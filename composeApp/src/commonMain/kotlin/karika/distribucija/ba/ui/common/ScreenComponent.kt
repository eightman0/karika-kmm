package karika.distribucija.ba.ui.common

import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.flow.StateFlow

/**
 * The part of a screen component that the shared scaffolding (KarikaScaffold, LoadingView2)
 * needs. Screens expose their own interface on top of it, so views can be driven by a fake
 * component in UI tests instead of a real CommonComponent.
 */
interface ScreenComponent {
    val snackbarHostState: SnackbarHostState
    val loader: StateFlow<Boolean>
}

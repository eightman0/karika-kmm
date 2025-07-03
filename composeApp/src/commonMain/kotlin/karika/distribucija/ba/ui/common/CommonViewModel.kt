package karika.distribucija.ba.ui.common

import androidx.compose.material3.SnackbarHostState
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import karika.distribucija.ba.Screen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

open class CommonViewModel(val navController: NavController, val stateHolder: KarikaStateHolder) :
    ViewModel() {
    open val title: String = ""
    val snackbarHostState = SnackbarHostState()
    private val scope = CoroutineScope(Dispatchers.Main)

    fun navigate(screen: Screen) {
        scope.launch {
            navController.navigate(screen.route)
        }
    }

    //LOADER
    private val _loader = MutableStateFlow(false)
    val loader = _loader.asStateFlow()
    fun showLoader() {
        _loader.update { true }
    }

    fun hideLoader() {
        _loader.update { false }
    }

    fun back() {
        scope.launch {
            navController.popBackStack()
        }
    }

    fun showMessage(message: String) {
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message
            )
        }
    }
    //LOADER
}
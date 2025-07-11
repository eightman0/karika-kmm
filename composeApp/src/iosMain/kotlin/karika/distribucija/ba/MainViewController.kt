package karika.distribucija.ba

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ComposeUIViewController
import com.arkivanov.decompose.ExperimentalDecomposeApi
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureIcon
import com.arkivanov.decompose.extensions.compose.stack.animation.predictiveback.PredictiveBackGestureOverlay
import com.arkivanov.essenty.backhandler.BackDispatcher
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalDecomposeApi::class)
fun MainViewController(
    component: AppComponent,
    backDispatcher: BackDispatcher
) = ComposeUIViewController {
    PredictiveBackGestureOverlay(
        backDispatcher = backDispatcher,
        backIcon = { progress, _ ->
            PredictiveBackGestureIcon(
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                progress = progress,
            )
        },
        modifier = Modifier.fillMaxSize(),
        endEdgeEnabled = false,
    ) {
        App(component)
    }
}
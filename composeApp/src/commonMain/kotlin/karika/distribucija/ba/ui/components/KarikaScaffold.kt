package karika.distribucija.ba.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.common.CommonViewModel
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_checked_circle
import karikav2.composeapp.generated.resources.ic_navigation_home
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource


@Composable
fun KarikaScaffold(
    modifier: Modifier = Modifier,
    viewModel: CommonViewModel,
    hostState: SnackbarHostState,
    containerColor: Color = KarikaColors.Primary,
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = KarikaColors.Primary,
        snackbarHost = {
            SnackbarHost(
                modifier = Modifier
                    .padding(bottom = 100.dp),
                hostState = hostState
            ) { data ->
                Snackbar(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    action = null,
                    containerColor = KarikaColors.Primary,
                    contentColor = KarikaColors.Primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                hostState.currentSnackbarData?.dismiss()
                            },
                        icon = vectorResource(Res.drawable.ic_checked_circle),
                        iconColor = KarikaColors.White,
                        textColor = KarikaColors.White,
                        text = data.visuals.message
                    )

                    LaunchedEffect(Unit) {
                        delay(1500)
                        hostState.currentSnackbarData?.dismiss()
                    }
                }
            }
        },
        topBar = topBar,
        bottomBar = bottomBar,
        contentWindowInsets = contentWindowInsets
    ) { padding ->
        content.invoke(padding)
    }

    LoadingView1(viewModel)
}
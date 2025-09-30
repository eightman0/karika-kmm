package karika.distribucija.ba.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.common.CommonComponent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_checked_circle
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource


@Composable
fun KarikaScaffold(
    modifier: Modifier = Modifier,
    component: CommonComponent,
    containerColor: Color = KarikaColors.Primary,
    contentWindowInsets: WindowInsets = WindowInsets(0.dp),
    topBar: @Composable () -> Unit = {},
    bottomBar: @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    disableSnackBar: Boolean = true,
    content: @Composable (PaddingValues) -> Unit
) {
    val imeVisible = rememberImeVisible()

    Scaffold(
        modifier = modifier,
        containerColor = containerColor,
        contentColor = KarikaColors.Primary,
        snackbarHost = {
            Box(
                modifier = Modifier
                    .padding(WindowInsets.systemBars.asPaddingValues())
                    .fillMaxSize()
            ) {
                if (!disableSnackBar) {
                    SnackbarHost(
                        modifier = Modifier
                            .padding(bottom = 100.dp),
                        hostState = component.snackbarHostState
                    ) { data ->
                        Snackbar(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            action = null,
                            containerColor = KarikaColors.Blue,
                            contentColor = KarikaColors.Blue,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            IconTextItem(
                                modifier = Modifier
                                    .onClick {
                                        component.snackbarHostState.currentSnackbarData?.dismiss()
                                    },
                                icon = vectorResource(Res.drawable.ic_checked_circle),
                                iconColor = KarikaColors.White,
                                textColor = KarikaColors.White,
                                text = data.visuals.message
                            )

                            LaunchedEffect(Unit) {
                                delay(1500)
                                component.snackbarHostState.currentSnackbarData?.dismiss()
                            }
                        }
                    }
                }
            }
        },
        topBar = topBar,
        bottomBar = {
            if (!imeVisible) {
                bottomBar.invoke()
            }
        },
        floatingActionButton = floatingActionButton,
        floatingActionButtonPosition = FabPosition.End,
        contentWindowInsets = contentWindowInsets
    ) { padding ->
        Box(
            modifier = Modifier
                .hideKeyboard()
                .padding(padding)
        ) {
            content.invoke(PaddingValues(0.dp))
        }
    }
}

@Composable
fun rememberImeVisible(): Boolean {
    val density = LocalDensity.current
    return WindowInsets.ime.getBottom(density) > 0
}
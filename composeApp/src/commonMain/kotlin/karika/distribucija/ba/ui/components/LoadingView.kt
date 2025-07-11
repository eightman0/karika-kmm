package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.common.CommonComponent

@Composable
fun LoadingView1(viewModel: CommonComponent) {
    val state = viewModel.loader.collectAsState()
    if (state.value) {
        Box(
            modifier = Modifier
                .background(color = KarikaColors.Black_20)
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier,
                color = KarikaColors.White
            )
        }
    }
}

@Composable
fun LoadingView(component: CommonComponent) {
    val state = component.loader.collectAsState()
    if (state.value) {
        Dialog(
            onDismissRequest = { },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    modifier = Modifier,
                    color = KarikaColors.White
                )
            }
        }
    }
}
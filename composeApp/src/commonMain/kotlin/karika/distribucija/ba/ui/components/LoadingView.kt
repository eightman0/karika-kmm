package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.common.CommonComponent

@Composable
fun LoadingView1(commonComponent: CommonComponent) {
    val state = commonComponent.loader.collectAsState()
    if (state.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            Box(
                modifier = Modifier
                    .width(120.dp)
                    .aspectRatio(1f)
                    .background(
                        color = KarikaColors.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .wrapContentSize(Alignment.Center)
            ) {
                CircularProgressIndicator(
                    modifier = Modifier,
                    color = KarikaColors.White
                )
            }
        }
    }
}

@Composable
fun LoadingView2(commonComponent: CommonComponent) {
    val state = commonComponent.loader.collectAsState()
    if (state.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier,
                color = KarikaColors.Primary
            )
        }
    }
}

@Composable
fun LoadingView3(state: MutableState<Boolean>) {
    if (state.value) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentSize(Alignment.Center)
        ) {
            CircularProgressIndicator(
                modifier = Modifier,
                color = KarikaColors.Primary
            )
        }
    }
}
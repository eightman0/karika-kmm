package karika.distribucija.ba.ui.view.order

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import karika.distribucija.ba.ui.components.KarikaText

@Composable
fun OrderView(viewModel: OrderViewModel) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        KarikaText("TEST_TEST")
    }
}
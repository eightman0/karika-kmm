package karika.distribucija.ba.ui.view.main.cart

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText

@Composable
fun CartView(viewModel: CartViewModel) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            modifier = Modifier
                .clickable {
                    viewModel.navigateToOrders()
                },
            color = KarikaColors.Secondary,
            text = "Korpa"
        )
    }
}
package karika.distribucija.ba.ui.view.salesrep.operations

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText

@Composable
fun SalesOperationsView(component: SalesOperationsComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = "Operacije",
            color = KarikaColors.Gray2,
            textSize = 18.sp,
            fontWeight = FontWeight.W600
        )
    }
}

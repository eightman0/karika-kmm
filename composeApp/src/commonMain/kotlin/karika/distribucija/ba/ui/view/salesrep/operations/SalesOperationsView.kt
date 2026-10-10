package karika.distribucija.ba.ui.view.salesrep.operations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chart
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesOperationsView(component: SalesOperationsComponent) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .padding(16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            KEmptyPlaceholder(
                icon = vectorResource(Res.drawable.ic_k_chart),
                title = "Uskoro dostupno",
                message = "Operacije komercijaliste će uskoro biti dostupne ovdje.",
                iconTint = VendorAccent,
                iconBackground = VendorAccentSoft
            )
        }
    }
}

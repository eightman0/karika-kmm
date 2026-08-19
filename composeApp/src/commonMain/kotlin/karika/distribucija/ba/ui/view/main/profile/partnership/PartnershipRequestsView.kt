package karika.distribucija.ba.ui.view.main.profile.partnership

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack

@Composable
fun PartnershipRequestsView(component: PartnershipRequestsComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Zahtjevi za partnerstvo") {
                component.appBack()
            }
        },
        component = component
    ) {
        Box(
            modifier = Modifier
                .padding(it)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp),
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W600,
                text = "Trenutno nemate zahtjeva za partnerstvo."
            )
        }
    }
}

package karika.distribucija.ba.ui.view.salesrep.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_lock
import org.jetbrains.compose.resources.vectorResource

/** Shown instead of the whole sales rep app when `vendor_operations_enabled` is false (or `me`
 * failed): every screen here is a Vendor Operations feature and would only answer 403. */
@Composable
fun VendorOpsUnavailableView(onRetry: () -> Unit, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page)
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(horizontal = 24.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        KEmptyPlaceholder(
            icon = vectorResource(Res.drawable.ic_k_lock),
            title = "Nije dostupno",
            message = "Ova funkcionalnost trenutno nije uključena za vašeg dobavljača. Obratite se administratoru.",
            iconTint = VendorAccent,
            iconBackground = VendorAccentSoft
        )
        Spacer(Modifier.height(24.dp))
        KPrimaryButton(
            text = "Pokušaj ponovo",
            modifier = Modifier.fillMaxWidth(),
            background = VendorAccent,
            onClick = onRetry
        )
        Spacer(Modifier.height(12.dp))
        KSecondaryButton(
            text = "Odjavi se",
            modifier = Modifier.fillMaxWidth(),
            textColor = KarikaUiColors.Red,
            onClick = onLogout
        )
    }
}

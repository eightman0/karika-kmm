package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.common.appUrl
import karika.distribucija.ba.ui.common.openPdf
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import org.jetbrains.compose.resources.vectorResource

@Composable
fun MandatoryUpdateModal(url: String = appUrl()) {
    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .rounded(shape = 16.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier,
                    text = "Nova verzija aplikacije",
                    color = KarikaColors.Gray2,
                    textSize = 20.sp,
                    fontWeight = FontWeight.W600
                )
                KarikaText(
                    modifier = Modifier,
                    text = "Dostupna je nova verzija aplikacije. Da biste nastavili sa korištenjem aplikacije, molimo Vas da je ažurirate na najnoviju verziju.",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.W600
                )
                PrimaryButtonFilled(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "Instaliraj novu verziju",
                ) {
                    openPdf(url)
                }
            }
        }
    }
}
@Composable
fun InfoModal(title: String, message: String, onDismiss: () -> Unit) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(KarikaColors.White)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.PinkSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_bell),
                    tint = KarikaUiColors.Pink,
                    size = 24.dp
                )
            }
            Spacer(Modifier.height(14.dp))
            KarikaText(
                text = title,
                color = KarikaUiColors.Ink,
                textSize = 19.sp,
                lineHeight = 24.sp,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.W700
            )
            Spacer(Modifier.height(8.dp))
            KarikaText(
                text = message,
                color = KarikaUiColors.Muted,
                textSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))
            KPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "U redu",
                onClick = onDismiss
            )
        }
    }
}

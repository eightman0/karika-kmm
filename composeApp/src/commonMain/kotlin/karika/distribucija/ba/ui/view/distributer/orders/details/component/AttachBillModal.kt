package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.utils.io.core.toByteArray
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_close
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_plus
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AttachBillModal(
    component: CommonComponent,
    onSubmit: (String, Pair<String, ByteArray>) -> Unit,
    onCancel: () -> Unit
) {
    val reason = remember { mutableStateOf("") }
    val attachedFile = mutableStateOf(Pair("", "".toByteArray())).asState()
    OrderModal(title = "Pošalji predračun", onDismiss = onCancel) {
        val shape = RoundedCornerShape(14.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .background(VendorAccentSoft)
                .border(1.5.dp, VendorAccent.copy(alpha = 0.45f), shape)
                .clickable {
                    component.stateHolder.handler.pickFile(
                        arrayOf(
                            "application/pdf",
                            "image/png",
                            "image/jpeg"
                        )
                    ) { name, data ->
                        attachedFile.value = Pair(name, data)
                    }
                }
                .padding(vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(KarikaColors.White),
                contentAlignment = Alignment.Center
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = VendorAccent, size = 20.dp)
            }
            KarikaText(
                text = "Dodaj predračun",
                color = VendorAccent,
                textSize = 15.sp,
                fontWeight = FontWeight.W700
            )
        }
        Spacer(Modifier.height(8.dp))
        KarikaText(
            text = "Dodajte Vaš predračun, ili ostavite prazno i predračun će biti automatski generisan.",
            color = KarikaUiColors.Muted,
            textSize = 13.sp,
            lineHeight = 18.sp
        )
        if (attachedFile.value.first.isNotEmpty()) {
            Spacer(Modifier.height(12.dp))
            val fileShape = RoundedCornerShape(12.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(fileShape)
                    .border(1.dp, KarikaUiColors.Line, fileShape)
                    .clickable {
                        attachedFile.value = Pair("", "".toByteArray())
                    }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_document), tint = VendorAccent, size = 18.dp)
                Spacer(Modifier.width(10.dp))
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = attachedFile.value.first,
                    color = KarikaUiColors.Ink,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    maxLines = 1
                )
                Spacer(Modifier.width(8.dp))
                KIcon(icon = vectorResource(Res.drawable.ic_k_close), tint = KarikaUiColors.Muted, size = 18.dp)
            }
        }
        Spacer(Modifier.height(16.dp))
        OrderField(
            modifier = Modifier.fillMaxWidth(),
            value = reason,
            label = "Poruka za kupca",
            placeholder = "Unesi poruku",
            imeAction = ImeAction.Next,
            singleLine = false,
            minHeight = 80.dp
        )
        Spacer(Modifier.height(20.dp))
        OrderModalButtons(
            primaryText = "Pošalji predračun",
            onPrimary = {
                onSubmit(
                    reason.value,
                    attachedFile.value
                )
            },
            onSecondary = onCancel
        )
    }
}

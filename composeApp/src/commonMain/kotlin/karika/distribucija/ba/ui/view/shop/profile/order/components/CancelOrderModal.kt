package karika.distribucija.ba.ui.view.shop.profile.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.RadioGroup
import karika.distribucija.ba.ui.components.YSpacer16
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CancelOrderModal(
    onSubmit: (String, String) -> Unit,
    onCancel: () -> Unit
) {
    val selected = remember { mutableStateOf(Pair("Pogrešna količina/artikal", 1)) }
    val reason = remember { mutableStateOf("") }
    Dialog(
        onDismissRequest = {
            onCancel()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(KarikaColors.White)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    KarikaText(
                        modifier = Modifier
                            .weight(1f),
                        text = "Otkazivanje narudžbe",
                        color = KarikaUiColors.Ink,
                        textSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.W700
                    )
                    KCircleButton(
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        size = 36.dp,
                        iconSize = 18.dp,
                        onClick = { onCancel() }
                    )
                }
                YSpacer16()
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Razlog otkazivanja narudžbe:",
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
                YSpacer16()
                RadioGroup(
                    selected = selected,
                    items = listOf(
                        Pair("Pogrešna količina/artikal", 1),
                        Pair("Dobavljač ne odgovara na narudžbu", 1),
                        Pair("Ostalo", 1),
                    )
                )

                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "",
                    value = reason,
                    placeholder = "Upiši razlog otkazivanja",
                    imeAction = ImeAction.Next
                )
                YSpacer16()
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KSecondaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Odustani"
                    ) {
                        onCancel()
                    }
                    KPrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Potvrdi"
                    ) {
                        onSubmit(selected.value.first, reason.value)
                    }
                }
            }
        }
    }
}
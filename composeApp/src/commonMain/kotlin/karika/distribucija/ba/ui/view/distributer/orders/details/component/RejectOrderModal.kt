package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun RejectOrderModal(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
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
                .rounded()
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
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
                        text = "Odbij narudžbu",
                        color = KarikaColors.Red,
                        textSize = 18.sp,
                        fontWeight = FontWeight.W700
                    )
                    Icon(
                        modifier = Modifier
                            .onClick {
                                onCancel()
                            }
                            .size(48.dp),
                        imageVector = vectorResource(Res.drawable.ic_tertiary),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                }
                YSpacer16()
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Jeste li sigurni da želite odbiti ovu narudžbu?",
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W600
                )
                YSpacer16()
                KarikaTextField1(
                    modifier = Modifier
                        .fillMaxWidth(),
                    title = "",
                    value = reason,
                    placeholder = "Dodajte poruku za kupca",
                    imeAction = ImeAction.Next
                )
                YSpacer16()
                HorizontalSecondaryButtons(
                    modifier = Modifier,
                    primaryTitle = "Odbij narudžbu",
                    secondaryTitle = "Odustani"
                ) {
                    if (it == "Odbij narudžbu") {
                        onSubmit(reason.value)
                    } else {
                        onCancel()
                    }
                }
            }
        }
    }
}
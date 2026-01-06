package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FabPosition
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
import io.ktor.utils.io.core.toByteArray
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.products.details.dashedBorder
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_attachment
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AttachBillModal(
    component: CommonComponent,
    onSubmit: (String, Pair<String, ByteArray>) -> Unit,
    onCancel: () -> Unit
) {
    val reason = remember { mutableStateOf("") }
    val attachedFile = mutableStateOf(Pair("", "".toByteArray())).asState()
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
                        text = "Pošalji predračun",
                        color = KarikaColors.Gray2,
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
                Column(
                    modifier = Modifier
                        .dashedBorder(color = KarikaColors.Blue2, cornerRadius = 3.dp)
                        .onClick {
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
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    YSpacer8()
                    Icon(
                        modifier = Modifier
                            .size(32.dp),
                        imageVector = vectorResource(Res.drawable.ic_attachment),
                        tint = KarikaColors.Gray2,
                        contentDescription = ""
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "Dodaj predračun",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W600
                    )
                    YSpacer8()
                }
                YSpacer8()
                KarikaText(
                    modifier = Modifier,
                    text = "Dodajte Vaš predračun, ili ostavite prazno i predračun će biti automatski generisan.",
                    color = KarikaColors.Gray6,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W300
                )
                if (attachedFile.value.first.isNotEmpty()) {
                    YSpacer16()
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                attachedFile.value = Pair("", "".toByteArray())
                            },
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        iconColor = KarikaColors.Gray2,
                        textColor = KarikaColors.Primary,
                        text = attachedFile.value.first,
                        fontWeight = FontWeight.W600,
                        textSize = 16.sp,
                        iconPosition = FabPosition.End
                    )
                }
                YSpacer16()
                KarikaTextField1(
                    modifier = Modifier
                        .height(80.dp)
                        .fillMaxWidth(),
                    title = "",
                    value = reason,
                    placeholder = "Unesi poruku",
                    imeAction = ImeAction.Next
                )
                YSpacer16()
                HorizontalSecondaryButtons(
                    modifier = Modifier,
                    primaryTitle = "Pošalji predračun",
                    secondaryTitle = "Odustani"
                ) {
                    if (it == "Pošalji predračun") {
                        onSubmit(
                            reason.value,
                            attachedFile.value
                        )
                    } else {
                        onCancel()
                    }
                }
            }
        }
    }
}
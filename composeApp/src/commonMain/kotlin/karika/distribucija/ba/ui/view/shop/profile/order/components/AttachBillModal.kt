package karika.distribucija.ba.ui.view.shop.profile.order.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
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
import io.ktor.utils.io.core.toByteArray
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.onClick
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
                        text = "Pošalji uplatnicu",
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(KarikaUiColors.PinkSoft)
                        .onClick {
                            component.stateHolder.handler.pickFile(arrayOf("application/pdf", "image/png", "image/jpeg")) { name, data ->
                                attachedFile.value = Pair(name, data)
                            }
                        }
                        .padding(vertical = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier
                            .size(32.dp),
                        imageVector = vectorResource(Res.drawable.ic_attachment),
                        tint = KarikaUiColors.Pink,
                        contentDescription = ""
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "Dodaj uplatnicu",
                        color = KarikaUiColors.Pink,
                        textSize = 15.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                if (attachedFile.value.first.isNotEmpty()) {
                    YSpacer16()
                    IconTextItem(
                        modifier = Modifier
                            .onClick {
                                attachedFile.value = Pair("", "".toByteArray())
                            },
                        icon = vectorResource(Res.drawable.ic_tertiary),
                        iconColor = KarikaUiColors.Muted,
                        textColor = KarikaUiColors.Ink,
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
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    KSecondaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Odustani"
                    ) {
                        onCancel()
                    }
                    KPrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Pošalji",
                        enabled = attachedFile.value.first.isNotEmpty()
                    ) {
                        onSubmit(
                            reason.value,
                            attachedFile.value
                        )
                    }
                }
            }
        }
    }
}
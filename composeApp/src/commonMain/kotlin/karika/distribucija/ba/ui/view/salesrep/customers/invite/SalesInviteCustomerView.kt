package karika.distribucija.ba.ui.view.salesrep.customers.invite

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.karikaFonts
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_info
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesInviteCustomerView(component: SalesInviteCustomerComponent) {
    val email by component.email.collectAsState()
    val note by component.note.collectAsState()
    val isSaving by component.isSaving.collectAsState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaColors.Gray20)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .navigationBarsPadding()
                .imePadding()
        ) {
            YSpacer16()

            // ── Info banner ────────────────────────────────────────────────────
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(KarikaColors.Blue.copy(alpha = 0.08f))
                    .border(1.dp, KarikaColors.Blue.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
                    .padding(14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_info),
                    contentDescription = "",
                    tint = KarikaColors.Blue,
                    modifier = Modifier.size(20.dp)
                )
                KarikaText(
                    text = "Kupac dobija zahtjev za partnerstvo i mora ga prihvatiti da bi se pojavio na vašoj listi.",
                    color = KarikaColors.Gray2,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W400
                )
            }

            YSpacer16()

            // ── Email ──────────────────────────────────────────────────────────
            KarikaText(
                text = "Email kupca*",
                color = KarikaColors.Gray2,
                textSize = 12.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_email),
                    contentDescription = "",
                    tint = KarikaColors.Gray6,
                    modifier = Modifier.size(18.dp)
                )
                BasicTextField(
                    value = email,
                    onValueChange = { component.setEmail(it) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    textStyle = TextStyle(
                        color = KarikaColors.Gray2,
                        fontSize = 15.sp,
                        fontFamily = karikaFonts()
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (email.isEmpty()) {
                            KarikaText(
                                text = "kupac@primjer.ba",
                                color = KarikaColors.Gray8,
                                textSize = 15.sp,
                                fontWeight = FontWeight.W400
                            )
                        }
                        inner()
                    }
                )
            }

            YSpacer16()

            // ── Napomena ───────────────────────────────────────────────────────
            KarikaText(
                text = "Napomena (opcionalno)",
                color = KarikaColors.Gray2,
                textSize = 12.sp,
                fontWeight = FontWeight.W600,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            BasicTextField(
                value = note,
                onValueChange = { component.setNote(it) },
                textStyle = TextStyle(
                    color = KarikaColors.Gray2,
                    fontSize = 15.sp,
                    fontFamily = karikaFonts()
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(KarikaColors.White)
                    .border(1.dp, KarikaColors.Gray9, RoundedCornerShape(14.dp))
                    .padding(14.dp),
                decorationBox = { inner ->
                    if (note.isEmpty()) {
                        KarikaText(
                            text = "Kratka napomena za kupca",
                            color = KarikaColors.Gray8,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                    inner()
                }
            )

            Spacer(Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .border(1.dp, KarikaColors.Blue, RoundedCornerShape(18.dp))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.goBack() },
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "Odustani",
                        color = KarikaColors.Blue,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(KarikaColors.Blue)
                        .clickable(
                            enabled = !isSaving,
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { component.send() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color = KarikaColors.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        KarikaText(
                            text = "Pošalji zahtjev",
                            color = KarikaColors.White,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }

            YSpacer16()
        }
    }
}

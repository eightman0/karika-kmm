package karika.distribucija.ba.ui.view.shop.cart.success

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CartSuccessView(component: CartSuccessComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        OrderPlaced(component)
    }
}

@Composable
fun OrderPlaced(component: CartSuccessComponent) {
    val orderId by component.orderId.collectAsState()
    KCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(KarikaUiColors.GreenSoft),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(KarikaUiColors.GreenDot),
                    contentAlignment = Alignment.Center
                ) {
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_check),
                        tint = Color.White,
                        size = 26.dp
                    )
                }
            }
            KarikaText(
                color = KarikaUiColors.Ink,
                text = "Vaš zahtjev za narudžbu je uspješno poslan dobavljačima.",
                fontWeight = FontWeight.W700,
                textSize = 20.sp,
                lineHeight = 26.sp,
                textAlign = TextAlign.Center
            )
            KarikaText(
                atext = buildAnnotatedString {
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W400,
                            color = KarikaUiColors.Muted,
                            fontSize = 15.sp
                        )
                    ) {
                        append("Broj Vaše narudžbe je: ")
                    }
                    withStyle(
                        style = SpanStyle(
                            fontWeight = FontWeight.W700,
                            color = KarikaUiColors.Pink,
                            fontSize = 15.sp
                        )
                    ) {
                        append(orderId)
                    }
                },
                color = KarikaUiColors.Muted,
                textAlign = TextAlign.Center
            )
            KarikaText(
                color = KarikaUiColors.Muted,
                text = "Poslat ćemo Vam e-poštom potvrdu narudžbe s detaljima i informacijama o praćenju.",
                fontWeight = FontWeight.W400,
                textSize = 14.sp,
                lineHeight = 20.sp,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(4.dp))
            KPrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Nastavi kupovati"
            ) {
                component.finish()
            }
        }
    }
}

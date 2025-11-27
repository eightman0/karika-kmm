package karika.distribucija.ba.ui.view.main.cart.success

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_success
import org.jetbrains.compose.resources.painterResource

@Composable
fun CartSuccessView(component: CartSuccessComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        OrderPlaced(component)
    }
}

@Composable
fun OrderPlaced(component: CartSuccessComponent) {
    val orderId by component.orderId.collectAsState()
    Column(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .padding(16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(Res.drawable.ic_success),
            contentDescription = ""
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = "Vaš zahtjev za narudžbu je uspješno poslan dobavljačima.",
            fontWeight = FontWeight.W700,
            textSize = 22.sp,
            textAlign = TextAlign.Center
        )
        KarikaText(
            modifier = Modifier,
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Black,
                        fontSize = 18.sp
                    )
                ) {
                    append("Broj Vaše narudžbe je: ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W700,
                        color = KarikaColors.Black,
                        fontSize = 16.sp
                    )
                ) {
                    append(orderId)
                }
            },
            textAlign = TextAlign.Center
        )
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            text = "Poslat ćemo vam e-poštom potvrdu narudžbe s detaljima i informacijama o praćenju.",
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            textAlign = TextAlign.Center
        )
        PrimaryButtonFilled(
            modifier = Modifier
                .height(48.dp)
                .padding(horizontal = 16.dp),
            title = "Nastavi kupovati",
            fontWeight = FontWeight.W700,
            textSize = 18.sp
        ) {
            component.finish()
        }
    }
}
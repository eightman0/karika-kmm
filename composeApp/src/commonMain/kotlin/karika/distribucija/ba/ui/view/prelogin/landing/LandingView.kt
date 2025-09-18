package karika.distribucija.ba.ui.view.prelogin.landing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.CarouselLogos
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaBox
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaLogo
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_checked_circle
import org.jetbrains.compose.resources.vectorResource


@Composable
fun LandingView(component: LandingComponent) {
    KarikaBox {
        KarikaScaffold(
            containerColor = KarikaColors.Transparent,
            contentWindowInsets = WindowInsets.systemBars,
            component = component
        ) {
            Box(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier
                        .padding(it),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaLogo()
                    KarikaText(
                        text = "Vaše centralno mjesto za\nefikasnu nabavku i prodaju",
                        color = KarikaColors.Black,
                        fontWeight = FontWeight.W700,
                        textSize = 24.sp,
                        textAlign = TextAlign.Center
                    )
                    YSpacer16()
                    KarikaText(
                        atext = buildAnnotatedString {
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W700,
                                    color = KarikaColors.Primary,
                                    fontSize = 18.sp
                                )
                            ) {
                                append("KARIKA ")
                            }
                            withStyle(
                                style = SpanStyle(
                                    fontWeight = FontWeight.W400,
                                    color = KarikaColors.Black,
                                    fontSize = 18.sp
                                )
                            ) {
                                append("povezuje kupce i provjerene dobavljače robe široke potrošnje.")
                            }
                        },
                        textAlign = TextAlign.Center
                    )
                    YSpacer16()
                    IconTextItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        icon = vectorResource(Res.drawable.ic_checked_circle),
                        iconColor = KarikaColors.Primary,
                        iconSize = 20.dp,
                        text = "Samo za pravna lica!",
                        textColor = KarikaColors.Black,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Start
                    )
                    IconTextItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        icon = vectorResource(Res.drawable.ic_checked_circle),
                        iconColor = KarikaColors.Primary,
                        iconSize = 20.dp,
                        text = "Direktna komunikacija kupaca i dobavljača",
                        textColor = KarikaColors.Black,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Start
                    )
                    IconTextItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        icon = vectorResource(Res.drawable.ic_checked_circle),
                        iconColor = KarikaColors.Primary,
                        iconSize = 20.dp,
                        text = "Direktna isporuka od strane dobavljača",
                        textColor = KarikaColors.Black,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Start
                    )
                    IconTextItem(
                        modifier = Modifier
                            .fillMaxWidth(),
                        icon = vectorResource(Res.drawable.ic_checked_circle),
                        iconColor = KarikaColors.Primary,
                        iconSize = 20.dp,
                        text = "Efikasnost i optimizacija poslovanja",
                        textColor = KarikaColors.Black,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Start
                    )
                    YSpacer8()
                    KarikaText(
                        text = "Prijavi se ili registruj kao:",
                        color = KarikaColors.Black,
                        fontWeight = FontWeight.W400,
                        textSize = 16.sp,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        PrimaryButtonFilled(
                            modifier = Modifier
                                .height(40.dp)
                                .weight(1f),
                            title = "Kupac",
                            fontWeight = FontWeight.W700,
                            textSize = 18.sp
                        ) {
                            component.navigateLogin("Kupac")
                        }
                        SecondaryButtonFilled(
                            modifier = Modifier
                                .height(40.dp)
                                .weight(1f),
                            title = "Dobavljač",
                            fontWeight = FontWeight.W700,
                            textSize = 18.sp
                        ) {
                            component.navigateLogin("Dobavljač")
                        }
                    }
                    YSpacer16()
                    //LandingBanner()
                    KarikaText(
                        modifier = Modifier,
                        color = KarikaColors.Black,
                        text = "Dobavljači",
                        textSize = 20.sp,
                        fontWeight = FontWeight.W700
                    )
                    CarouselLogos(component)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        component.loadData()
    }
}

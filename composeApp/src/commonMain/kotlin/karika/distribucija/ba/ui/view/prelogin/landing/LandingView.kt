package karika.distribucija.ba.ui.view.prelogin.landing

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.KarikaType
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFeatureTile
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KImage
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaHeaderShape
import karika.distribucija.ba.ui.components.KLogo
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.LoadingView2
import karika.distribucija.ba.ui.components.vendorLogoTag
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_check
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_store
import org.jetbrains.compose.resources.vectorResource


@Composable
fun LandingView(component: LandingComponent) {
    val promotedLogos by component.promotedLogos.collectAsState()
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        component = component
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // White header: logo, headline, what Karika is and the four promises
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(KarikaHeaderShape)
                    .background(KarikaColors.White)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(start = 20.dp, end = 20.dp, top = 28.dp, bottom = 24.dp)
            ) {
                KLogo(width = 52.dp)
                Spacer(Modifier.height(22.dp))
                KarikaText(
                    text = "Vaše centralno mjesto za efikasnu nabavku i prodaju",
                    color = KarikaUiColors.Ink,
                    fontWeight = FontWeight.W800,
                    textSize = 28.sp,
                    lineHeight = 32.sp
                )
                Spacer(Modifier.height(10.dp))
                KarikaText(
                    atext = buildAnnotatedString {
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.W700,
                                color = KarikaUiColors.Pink
                            )
                        ) {
                            append("KARIKA ")
                        }
                        append("povezuje kupce i provjerene dobavljače robe široke potrošnje.")
                    },
                    color = KarikaUiColors.Muted,
                    textSize = 15.sp,
                    lineHeight = 22.sp
                )
                Spacer(Modifier.height(16.dp))
                KDivider()
                Spacer(Modifier.height(6.dp))
                LandingBullet("Samo za pravna lica")
                LandingBullet("Direktna komunikacija kupaca i dobavljača")
                LandingBullet("Direktna isporuka od strane dobavljača")
                LandingBullet("Efikasnost i optimizacija poslovanja")
            }

            KarikaText(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
                text = "Prijavi se ili registruj kao",
                color = KarikaUiColors.Ink,
                fontWeight = FontWeight.W700,
                textSize = 17.sp,
                lineHeight = 22.sp
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                KFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Kupac",
                    subtitle = "Naručujem robu",
                    icon = vectorResource(Res.drawable.ic_k_cart),
                    background = KarikaUiColors.Pink,
                    showChevron = true
                ) {
                    component.navigateLogin(KarikaType.SHOP)
                }
                KFeatureTile(
                    modifier = Modifier.weight(1f),
                    title = "Dobavljač",
                    subtitle = "Prodajem robu",
                    icon = vectorResource(Res.drawable.ic_k_store),
                    background = KarikaUiColors.Ink,
                    showChevron = true
                ) {
                    component.navigateLogin(KarikaType.VENDOR)
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable {
                            component.appNavigate(AppConfig.Main)
                        }
                        .padding(horizontal = 18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        text = "Nastavi kao gost",
                        color = KarikaUiColors.Ink,
                        fontWeight = FontWeight.W600,
                        textSize = 14.sp
                    )
                    Spacer(Modifier.width(6.dp))
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_chevron_right),
                        tint = KarikaUiColors.Ink,
                        size = 16.dp
                    )
                }
            }

            // Promoted supplier logos, loaded with the banners
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                if (promotedLogos.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        KSectionTitle(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 12.dp),
                            title = "Dobavljači na Kariki"
                        )
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items = promotedLogos) { vendor ->
                                val shape = RoundedCornerShape(18.dp)
                                Box(
                                    modifier = Modifier
                                        .testTag(vendorLogoTag(vendor))
                                        .size(76.dp)
                                        .clip(shape)
                                        .background(KarikaColors.White)
                                        .border(1.dp, KarikaUiColors.Line, shape)
                                        .clickable {
                                            component.showVendor(vendor.toVendor())
                                        }
                                ) {
                                    KImage(
                                        modifier = Modifier.fillMaxSize(),
                                        url = vendor.logoImage(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                    }
                }
                LoadingView2(component)
                LaunchedEffect(Unit) {
                    component.loadBanners()
                }
            }

            Spacer(Modifier.height(20.dp))
            Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
        }
    }
}

/** One of the landing's promises with a pink check in a soft pink circle. */
@Composable
private fun LandingBullet(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(KarikaUiColors.PinkSoft),
            contentAlignment = Alignment.Center
        ) {
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_check),
                tint = KarikaUiColors.Pink,
                size = 14.dp
            )
        }
        Spacer(Modifier.width(12.dp))
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Ink,
            fontWeight = FontWeight.W500,
            textSize = 14.sp,
            lineHeight = 18.sp
        )
    }
}

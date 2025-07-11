package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.ui.view.main.home.HomeComponent
import kotlinx.coroutines.delay
import kotlinx.coroutines.yield

@Composable
fun Carousel(component: HomeComponent) {
    val promotedVendors by component.promotedVendors.collectAsState()

    if (promotedVendors.isNotEmpty()) {
        val pagerState = rememberPagerState(
            initialPage = 0,
            pageCount = { promotedVendors.size })

        HorizontalPager(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            state = pagerState,
            pageSpacing = 16.dp
        ) {
            CarouselDoubleItem(
                pair = promotedVendors[it],
                component = component
            )
        }

        LaunchedEffect(Unit) {
            while (true) {
                yield()
                delay(3000L)

                while (pagerState.isScrollInProgress) {
                    delay(100)
                }
                val nextPage = (pagerState.currentPage + 1) % promotedVendors.size
                pagerState.animateScrollToPage(nextPage)
            }
        }
    }
}

@Composable
private fun CarouselItem(
    modifier: Modifier,
    promotedVendor: PromotedVendor,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .onClick {
                onClick()
            }
            .clip(RoundedCornerShape(2.dp))
            .background(color = KarikaColors.Secondary)
            .fillMaxHeight(),
        contentAlignment = Alignment.BottomCenter
    ) {
        KarikaImage(
            modifier = Modifier
                .fillMaxSize(),
            model = promotedVendor.image()
        )
        Box(
            modifier = Modifier
                .shadow(
                    elevation = 5.dp,
                    shape = RoundedCornerShape(8.dp),
                    ambientColor = Color(0x26000000),
                    spotColor = Color(0x26000000)
                )
                .fillMaxSize()
        )
        Box(
            modifier = Modifier
                .height(23.dp)
                .background(color = KarikaColors.Blue1)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 2.dp),
                color = KarikaColors.Black,
                text = promotedVendor.name(),
                textAlign = TextAlign.Center,
                textSize = 10.sp,
                fontWeight = FontWeight.W700
            )
        }
    }
}

@Composable
fun CarouselDoubleItem(
    pair: Pair<PromotedVendor, PromotedVendor>,
    component: HomeComponent,
) {
    Row(
        modifier = Modifier
            .height(120.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CarouselItem(
            modifier = Modifier
                .weight(1f),
            pair.first
        ) {
            component.showVendor(pair.first.toVendor())
        }
        CarouselItem(
            modifier = Modifier
                .weight(1f),
            pair.second
        ) {
            component.showVendor(pair.second.toVendor())
        }
    }
}
package karika.distribucija.ba.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.PromotedVendor
import karika.distribucija.ba.ui.common.CommonComponent
import kotlinx.coroutines.delay

@Composable
fun CarouselBanners(component: CommonComponent) {
    val promotedVendors by component.promotedVendors.collectAsState()

    if (promotedVendors.isNotEmpty()) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val divideBy = if (gridColumnCount() == 4) 2 else 1
            val cardWidth = (maxWidth * (1f - 0.2f) - 16.dp) / divideBy

            LazyRow(
                modifier = Modifier
                    .height(200.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(items = promotedVendors) {
                    CarouselBannerItem(
                        modifier = Modifier
                            .width(cardWidth),
                        promotedVendor = it
                    ) {
                        component.showVendor(it.toVendor())
                    }
                }
            }
        }
    }
}

@Composable
fun CarouselBannersAuto(component: CommonComponent) {
    val promotedVendors by component.promotedVendors.collectAsState()
    val pagerState = rememberPagerState { promotedVendors.size }

    if (promotedVendors.isNotEmpty()) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(7f / 5f)
            ) {
                CarouselBannerItemAuto(
                    modifier = Modifier,
                    promotedVendor = promotedVendors[it]
                )
            }
        }

        LaunchedEffect(pagerState, promotedVendors.size, 3000, 600) {
            while (true) {
                delay(3000)
                if (!pagerState.isScrollInProgress && promotedVendors.size > 1) {
                    val next = (pagerState.currentPage + 1) % promotedVendors.size
                    pagerState.animateScrollToPage(
                        next,
                        animationSpec = tween(durationMillis = 600)
                    )
                }
            }
        }
    }
}

@Composable
private fun CarouselBannerItemAuto(
    modifier: Modifier,
    promotedVendor: PromotedVendor,
    onClick: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = modifier
                .onClick {
                    onClick()
                }
                .background(color = KarikaColors.Secondary)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = promotedVendor.bannerImage()
            )
            Box(
                modifier = Modifier
                    .height(60.dp)
                    .background(color = KarikaColors.Blue1)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 2.dp),
                    color = KarikaColors.White,
                    text = promotedVendor.name(),
                    textAlign = TextAlign.Center,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
private fun CarouselBannerItem(
    modifier: Modifier,
    promotedVendor: PromotedVendor,
    onClick: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = modifier
                .onClick {
                    onClick()
                }
                .background(color = KarikaColors.Secondary)
                .fillMaxHeight(),
            contentAlignment = Alignment.BottomCenter
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = promotedVendor.bannerImage()
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
                    color = KarikaColors.White,
                    text = promotedVendor.name(),
                    textAlign = TextAlign.Center,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
fun CarouselLogos(component: CommonComponent) {
    val promotedLogos by component.promotedLogos.collectAsState()

    if (promotedLogos.isNotEmpty()) {
        LazyRow(
            modifier = Modifier
                .height(100.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = promotedLogos) {
                CarouselLogoItem(
                    modifier = Modifier,
                    promotedVendor = it
                ) {
                    component.showVendor(it.toVendor())
                }
            }
        }
    }
}

@Composable
private fun CarouselLogoItem(
    modifier: Modifier,
    promotedVendor: PromotedVendor,
    onClick: () -> Unit = {}
) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = modifier
                .onClick {
                    onClick()
                }
                .background(color = KarikaColors.Secondary)
                .height(100.dp)
                .aspectRatio(1f),
            contentAlignment = Alignment.BottomCenter
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = promotedVendor.logoImage()
            )
            /*Box(
                modifier = Modifier
                    .height(23.dp)
                    .background(color = KarikaColors.Blue1)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 2.dp),
                    color = KarikaColors.White,
                    text = promotedVendor.name(),
                    textAlign = TextAlign.Center,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W700
                )
            }*/
        }
    }
}
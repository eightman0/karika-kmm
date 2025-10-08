package karika.distribucija.ba.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppComponent
import karika.distribucija.ba.AppConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_primary_logo
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ScreenSaver(component: AppComponent) {
    val show = component.showScreenSaver.asState()
    val promotedBanners = component.promotedVendors.collectAsState()
    val notificationCount = component.stateHolder
        .customerNotificationHandler.notificationCount.collectAsState()
    if (show.value && component.stateHolder.sessionHandler.hasJWT()) {
        Column(
            modifier = Modifier
                .onClick {

                }
                .background(color = KarikaColors.Black)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .fillMaxSize()
        ) {
            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            show.negate()
                            component.appNavigate(AppConfig.Notifications)
                        }
                        .background(
                            color = KarikaColors.Yellow
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    KarikaText(
                        text = "${notificationCount.value}",
                        textSize = 14.sp,
                        color = KarikaColors.Black,
                        fontWeight = FontWeight.Normal,
                        modifier = Modifier
                            .padding(8.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                KarikaText(
                    text = "notifikacije",
                    textSize = 16.sp,
                    color = KarikaColors.Yellow,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .onClick {
                            show.negate()
                            component.appNavigate(AppConfig.Notifications)
                        }
                        .padding(8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .clickable {
                            show.negate()
                        }
                        .background(
                            color = KarikaColors.White
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .padding(8.dp),
                        imageVector = vectorResource(Res.drawable.ic_tertiary),
                        contentDescription = null,
                        tint = KarikaColors.Gray2
                    )
                }
            }
            Box(
                modifier = Modifier
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                if (promotedBanners.value.isNotEmpty()) {
                    CarouselBannersAuto(component)
                } else {
                    Image(
                        painter = painterResource(Res.drawable.ic_primary_logo),
                        contentDescription = "",
                        contentScale = ContentScale.Inside
                    )
                }
            }
        }
        LaunchedEffect(Unit) {
            component.loadBanners()
        }
    }
}
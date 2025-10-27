package karika.distribucija.ba.ui.view.main.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.appVersionName
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.YSpacer16
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_gift
import karikav2.composeapp.generated.resources.ic_logout
import karikav2.composeapp.generated.resources.ic_messages
import karikav2.composeapp.generated.resources.ic_navigation_profile
import karikav2.composeapp.generated.resources.ic_notifications
import karikav2.composeapp.generated.resources.ic_orders
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProfileView(component: ProfileComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.Background)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Header(component)
            Actions(component)
        }
    }
}

@Composable
private fun Header(component: ProfileComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(color = KarikaColors.Red1, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_navigation_profile),
                    contentDescription = ""
                )
            }
            KarikaText(
                modifier = Modifier
                    .clickable {

                    },
                color = KarikaColors.Black,
                fontWeight = FontWeight.W700,
                textSize = 18.sp,
                text = profile.companyName()
            )
        }
    }

}

@Composable
private fun Actions(component: ProfileComponent) {
    val notificationCount =
        component.stateHolder.customerNotificationHandler.notificationCount.asStateFlow()
    val adminCount =
        component.stateHolder.customerNotificationHandler.messageUnreadCountAdmin.asStateFlow()
    val userCount =
        component.stateHolder.customerNotificationHandler.messageUnreadCountUser.asStateFlow()

    Column(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Moj nalog",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.Account)
            }
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Moje narudžbe",
                icon = Res.drawable.ic_orders,
                color = KarikaColors.Gray2,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.Orders)
            }
        }
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Poruke admina",
                icon = Res.drawable.ic_messages,
                color = KarikaColors.Gray2,
                badge = adminCount.value,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.AdminMessages)
            }
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Poruke dobavljača",
                icon = Res.drawable.ic_navigation_profile,
                color = KarikaColors.Gray2,
                badge = userCount.value,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.VendorMessages)
            }
        }
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Moji bodovi",
                icon = Res.drawable.ic_gift,
                color = KarikaColors.Gray2,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.Points)
            }
            PrimaryButton(
                modifier = Modifier
                    .height(50.dp)
                    .weight(1f),
                title = "Notifikacije",
                icon = Res.drawable.ic_notifications,
                color = KarikaColors.Gray2,
                badge = notificationCount.value,
                contentPadding = PaddingValues(4.dp)
            ) {
                component.appNavigate(AppConfig.Notifications)
            }
        }
        YSpacer16()
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Black,
            fontWeight = FontWeight.W600,
            textSize = 14.sp,
            text = appVersionName()
        )
        YSpacer16()
    }
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        PrimaryButton(
            modifier = Modifier,
            title = "Odjava",
            icon = Res.drawable.ic_logout,
            color = KarikaColors.Primary,
            textSize = 16.sp,
        ) {
            component.logout()
        }
    }
}
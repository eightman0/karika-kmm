package karika.distribucija.ba.ui.view.main.profile.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Notification
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.onClick

@Composable
fun NotificationsView(component: NotificationsComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Notifikacije") {
                component.appBack()
            }
        },
        component = component
    ) {
        Notifications(it, component)
    }
}

@Composable
private fun Notifications(padding: PaddingValues, component: NotificationsComponent) {
    val notifications by component.notifications.collectAsState()

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
        ) {
            items(items = notifications) {
                NotificationItem(it, component)
            }
        }

        if (notifications.isEmpty()) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                color = KarikaColors.Primary,
                fontWeight = FontWeight.W700,
                textSize = 16.sp,
                text = "Nema obavijesti",
                maxLines = 1
            )
        }
    }
}

@Composable
private fun NotificationItem(item: Notification, component: NotificationsComponent) {
    Box(
        modifier = Modifier
            .onClick {
                component.markAsRead(item)
            }
            .height(72.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        if (item.isRead == "false") {
            Box(
                modifier = Modifier
                    .background(color = KarikaColors.Red2)
                    .fillMaxSize(),
                contentAlignment = Alignment.CenterEnd
            ) {
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(8.dp)
                        .background(color = KarikaColors.Red3, shape = CircleShape)
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            YSpacer16()
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                color = KarikaColors.Gray18,
                fontWeight = FontWeight.W700,
                textSize = 14.sp,
                text = item.title,
                maxLines = 2
            )
            YSpacer8()
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                color = KarikaColors.Gray13,
                fontWeight = FontWeight.W400,
                textSize = 12.sp,
                text = item.createdAt
            )
            YSpacer16()
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth(),
                thickness = 1.dp,
                color = KarikaColors.Divider
            )
        }
    }
}
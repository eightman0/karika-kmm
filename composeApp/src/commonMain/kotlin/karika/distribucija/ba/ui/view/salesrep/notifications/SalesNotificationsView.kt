package karika.distribucija.ba.ui.view.salesrep.notifications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorNotification
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyPlaceholder
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karika.distribucija.ba.util.inSarajevo
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SalesNotificationsView(component: SalesNotificationsComponent) {
    val notifications by component.notifications.collectAsState()
    val hasUnread = notifications.any { !it.isRead }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(KarikaUiColors.Page),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (hasUnread) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        KarikaText(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { component.markAllAsRead() }
                                .padding(horizontal = 6.dp, vertical = 4.dp),
                            color = VendorAccent,
                            fontWeight = FontWeight.W700,
                            textSize = 13.sp,
                            text = "Označi sve kao pročitano"
                        )
                    }
                }
            }
            items(items = notifications) {
                SalesNotificationItem(it, component)
            }
        }

        if (notifications.isEmpty()) {
            KEmptyPlaceholder(
                icon = vectorResource(Res.drawable.ic_k_bell),
                title = "Nema obavijesti",
                message = "Ovdje će se pojaviti obavijesti o narudžbama i porukama.",
                iconTint = VendorAccent,
                iconBackground = VendorAccentSoft
            )
        }
    }

    LaunchedEffect(Unit) {
        component.get()
    }
}

@Composable
private fun SalesNotificationItem(item: VendorNotification, component: SalesNotificationsComponent) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        background = if (item.isRead) KarikaColors.White else VendorAccentSoft.copy(alpha = 0.45f),
        onClick = { component.markAsRead(item) }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (item.isRead) KarikaUiColors.Field else VendorAccentSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_bell),
                    tint = if (item.isRead) KarikaUiColors.Muted else VendorAccent,
                    size = 18.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    color = KarikaUiColors.Ink,
                    fontWeight = if (item.isRead) FontWeight.W500 else FontWeight.W700,
                    textSize = 14.sp,
                    lineHeight = 19.sp,
                    text = item.body,
                    maxLines = 2
                )
                KarikaText(
                    modifier = Modifier.padding(top = 4.dp),
                    color = KarikaUiColors.Muted,
                    textSize = 12.sp,
                    text = item.createdAt.inSarajevo()
                )
            }
            if (!item.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .padding(top = 6.dp)
                        .size(8.dp)
                        .background(color = VendorAccent, shape = CircleShape)
                )
            }
        }
    }
}

package karika.distribucija.ba.ui.view.shop.profile.notifications

import karika.distribucija.ba.util.inSarajevo
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.windowInsetsTopHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.VendorNotification
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KEmptyState
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.ReadFilterDropdown
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import org.jetbrains.compose.resources.vectorResource

@Composable
fun NotificationsView(component: NotificationsComponent) {
    KarikaScaffold(
        containerColor = KarikaUiColors.Page,
        contentWindowInsets = WindowInsets(0.dp),
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(KarikaUiColors.Page)) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsTopHeight(WindowInsets.statusBars)
                )
                KBackHeader(
                    title = "Notifikacije",
                    onBack = { component.appBack() },
                    actions = {
                        val readFilter by component.readFilter.collectAsState()
                        ReadFilterDropdown(
                            selected = readFilter,
                            borderColor = KarikaUiColors.Border,
                            textColor = KarikaUiColors.Ink,
                            iconColor = KarikaUiColors.Muted,
                            selectedTextColor = KarikaUiColors.Pink,
                            onSelect = { component.setReadFilter(it) }
                        )
                    }
                )
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
    val hasUnread = notifications.any { !it.isRead }

    Box(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .background(KarikaUiColors.Page),
        contentAlignment = Alignment.Center
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (hasUnread) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        KarikaText(
                            modifier = Modifier
                                .onClick { component.markAllAsRead() }
                                .padding(4.dp),
                            color = KarikaUiColors.Pink,
                            fontWeight = FontWeight.W600,
                            textSize = 14.sp,
                            text = "Označi sve kao pročitano"
                        )
                    }
                }
            }
            items(items = notifications) {
                NotificationItem(it, component)
            }
            item {
                Column {
                    Spacer(Modifier.height(20.dp))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }

        if (notifications.isEmpty()) {
            KEmptyState(text = "Nema obavijesti")
        }
    }
}

@Composable
private fun NotificationItem(item: VendorNotification, component: NotificationsComponent) {
    KCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        onClick = { component.markAsRead(item) }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (item.isRead) KarikaUiColors.Field else KarikaUiColors.PinkSoft),
                contentAlignment = Alignment.Center
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_bell),
                    tint = if (item.isRead) KarikaUiColors.Muted else KarikaUiColors.Pink,
                    size = 19.dp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    color = KarikaUiColors.Ink,
                    fontWeight = if (item.isRead) FontWeight.W500 else FontWeight.W700,
                    textSize = 14.sp,
                    lineHeight = 19.sp,
                    text = item.title,
                    maxLines = 2
                )
                Spacer(Modifier.height(4.dp))
                KarikaText(
                    color = KarikaUiColors.Subtle,
                    fontWeight = FontWeight.W400,
                    textSize = 12.sp,
                    text = item.createdAt.inSarajevo()
                )
            }
            if (!item.isRead) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(KarikaUiColors.Pink)
                )
            }
        }
    }
}

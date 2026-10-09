package karika.distribucija.ba.ui.view.shop.profile

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.appVersionName
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KHeader
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KMenuRow
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_logout
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_user
import kotlinx.coroutines.flow.asStateFlow
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProfileView(component: ProfileComponent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = KarikaUiColors.Page)
            .verticalScroll(rememberScrollState())
    ) {
        Header(component)
        Actions(component)
    }
}

@Composable
private fun Header(component: ProfileComponent) {
    val profile by component.stateHolder.customerSpecificHandler.userDetails.collectAsState()
    val notificationCount by component.stateHolder.customerNotificationHandler.notificationCount
        .asStateFlow()
        .collectAsState()

    KHeader(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 18.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KInitials(
                name = profile.companyNameNullable() ?: profile.email,
                size = 56.dp,
                shape = CircleShape,
                background = KarikaUiColors.Pink,
                color = KarikaColors.White,
                textSize = 20.sp
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = profile.companyName(),
                    color = KarikaUiColors.Ink,
                    textSize = 20.sp,
                    lineHeight = 24.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 2
                )
                KarikaText(
                    modifier = Modifier.padding(top = 2.dp),
                    text = profile.email,
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 16.sp,
                    maxLines = 1
                )
            }
            Spacer(Modifier.width(8.dp))
            KCircleButton(
                icon = vectorResource(Res.drawable.ic_k_bell),
                showDot = notificationCount > 0,
                onClick = { component.appNavigate(AppConfig.Notifications) }
            )
        }
    }
}

@Composable
private fun Actions(component: ProfileComponent) {
    val notificationCount =
        component.stateHolder.customerNotificationHandler.notificationCount
            .asStateFlow()
            .collectAsState()
    val adminCount =
        component.stateHolder.customerNotificationHandler.messageUnreadCount
            .asStateFlow()
            .collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Moj nalog",
                icon = vectorResource(Res.drawable.ic_k_user)
            ) {
                component.appNavigate(AppConfig.Account)
            }
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Moje narudžbe",
                icon = vectorResource(Res.drawable.ic_k_document)
            ) {
                component.appNavigate(AppConfig.Orders)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Poruke admina",
                icon = vectorResource(Res.drawable.ic_k_mail),
                badge = adminCount.value.customerAdmin
            ) {
                component.appNavigate(AppConfig.AdminMessages)
            }
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Poruke dobavljača",
                icon = vectorResource(Res.drawable.ic_k_chat),
                badge = adminCount.value.vendorCustomer
            ) {
                component.appNavigate(AppConfig.VendorMessages)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Moji bodovi",
                icon = vectorResource(Res.drawable.ic_k_gift)
            ) {
                component.appNavigate(AppConfig.Points)
            }
            ProfileTile(
                modifier = Modifier.weight(1f),
                text = "Notifikacije",
                icon = vectorResource(Res.drawable.ic_k_bell),
                badge = notificationCount.value
            ) {
                component.appNavigate(AppConfig.Notifications)
            }
        }
        KCard(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp)
        ) {
            KMenuRow(
                text = "Zahtjevi za partnerstvo",
                icon = vectorResource(Res.drawable.ic_k_mail),
                onClick = { component.appNavigate(AppConfig.PartnershipRequests) }
            )
        }
        Spacer(Modifier.height(14.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { component.logout() }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_logout),
                    tint = KarikaUiColors.Pink,
                    size = 18.dp
                )
                Spacer(Modifier.width(8.dp))
                KarikaText(
                    text = "Odjava",
                    color = KarikaUiColors.Pink,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            KarikaText(
                text = appVersionName(),
                color = KarikaUiColors.Subtle,
                textSize = 12.sp
            )
        }
    }
}

/** White grid tile: soft pink icon tile, bold label and an unread badge when [badge] > 0. */
@Composable
private fun ProfileTile(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    badge: Int = 0,
    onClick: () -> Unit,
) {
    KCard(
        modifier = modifier.heightIn(min = 100.dp),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(KarikaUiColors.PinkSoft),
                    contentAlignment = Alignment.Center
                ) {
                    KIcon(icon = icon, tint = KarikaUiColors.Pink, size = 20.dp)
                }
                Spacer(Modifier.weight(1f))
                if (badge > 0) {
                    Box(
                        modifier = Modifier
                            .heightIn(min = 22.dp)
                            .clip(RoundedCornerShape(50))
                            .background(KarikaUiColors.Pink)
                            .padding(horizontal = 7.dp, vertical = 3.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        KarikaText(
                            text = "$badge",
                            color = KarikaColors.White,
                            textSize = 11.sp,
                            fontWeight = FontWeight.W700,
                            maxLines = 1
                        )
                    }
                }
            }
            KarikaText(
                text = text,
                color = KarikaUiColors.Ink,
                textSize = 14.sp,
                lineHeight = 18.sp,
                fontWeight = FontWeight.W700,
                maxLines = 2
            )
        }
    }
}

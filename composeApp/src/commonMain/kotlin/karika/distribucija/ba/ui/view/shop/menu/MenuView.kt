package karika.distribucija.ba.ui.view.shop.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KMenuRow
import karika.distribucija.ba.ui.components.KSearchField
import karika.distribucija.ba.ui.components.KTitleHeader
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.shop.MainConfig
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_bell
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_mail
import karikav2.composeapp.generated.resources.ic_k_star
import karikav2.composeapp.generated.resources.ic_k_store
import karikav2.composeapp.generated.resources.ic_phone
import org.jetbrains.compose.resources.vectorResource

private val MenuCardShape = RoundedCornerShape(14.dp)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuView(component: MenuComponent) {
    var showSupportSheet by remember { mutableStateOf(false) }
    val notificationCount by component.stateHolder.customerNotificationHandler.notificationCount.collectAsState()

    Column(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        KTitleHeader(
            title = "Meni",
            trailing = {
                KCircleButton(
                    icon = vectorResource(Res.drawable.ic_k_bell),
                    showDot = notificationCount > 0
                ) {
                    if (component.isGuest()) {
                        component.stateHolder.commonHandler.showLoginRequired("*Potrebna registracija za pristup notifikacijama")
                    } else {
                        component.appNavigate(AppConfig.Notifications)
                    }
                }
            },
            below = {
                Spacer(Modifier.height(14.dp))
                KSearchField(
                    modifier = Modifier.fillMaxWidth(),
                    value = "",
                    onValueChange = {},
                    placeholder = "Pretraži…",
                    onClick = { component.mainNavigate(MainConfig.Search) }
                )
            }
        )

        CategoriesCard(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
            onClick = component::categories
        )

        KCard(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp)
                .fillMaxWidth(),
            shape = MenuCardShape
        ) {
            KMenuRow(
                text = "Blog",
                icon = vectorResource(Res.drawable.ic_k_document),
                onClick = component::blog
            )
            KDivider()
            KMenuRow(
                text = "Samo na Kariki",
                icon = vectorResource(Res.drawable.ic_k_star),
                onClick = component::karika
            )
            KDivider()
            KMenuRow(
                text = "Često postavljana pitanja",
                icon = vectorResource(Res.drawable.ic_k_chat)
            ) {
                component.appNavigate(AppConfig.Faq)
            }
        }

        KCard(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 20.dp)
                .fillMaxWidth(),
            shape = MenuCardShape
        ) {
            KMenuRow(
                text = "Kontaktirajte nas",
                icon = vectorResource(Res.drawable.ic_k_mail),
                tileBackground = KarikaUiColors.PinkSoft,
                iconTint = KarikaUiColors.Pink,
                textColor = KarikaUiColors.Pink
            ) {
                showSupportSheet = true
            }
        }
    }

    if (showSupportSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSupportSheet = false },
            containerColor = KarikaColors.White,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 24.dp)
            ) {
                KarikaText(
                    text = "Kontaktirajte nas",
                    fontWeight = FontWeight.W700,
                    textSize = 20.sp,
                    lineHeight = 24.sp,
                    color = KarikaUiColors.Ink
                )
                Spacer(Modifier.height(6.dp))
                KarikaText(
                    text = "Tu smo za sva vaša pitanja.",
                    textSize = 14.sp,
                    lineHeight = 20.sp,
                    color = KarikaUiColors.Muted
                )
                Spacer(Modifier.height(16.dp))
                KCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MenuCardShape
                ) {
                    ContactRow(
                        label = "Email",
                        value = "info@karika.ba",
                        icon = vectorResource(Res.drawable.ic_k_mail)
                    ) {
                        openEmail("info@karika.ba")
                    }
                    KDivider()
                    ContactRow(
                        label = "Telefon",
                        value = "033/246-830",
                        icon = vectorResource(Res.drawable.ic_phone)
                    ) {
                        openPhoneCall("033246830")
                    }
                }
            }
        }
    }
}

/** Navy "Kategorije proizvoda" card with a decorative pink circle. */
@Composable
private fun CategoriesCard(modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(KarikaUiColors.Ink)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 24.dp, y = 24.dp)
                .size(110.dp)
                .clip(CircleShape)
                .background(KarikaUiColors.Pink.copy(alpha = 0.35f))
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_store), tint = KarikaColors.White, size = 24.dp)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = "Kategorije proizvoda",
                    color = KarikaColors.White,
                    textSize = 17.sp,
                    lineHeight = 22.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
                KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaColors.White, size = 20.dp)
            }
        }
    }
}

/** Contact row of the "Kontaktirajte nas" sheet: pink icon tile, label and value. */
@Composable
private fun ContactRow(label: String, value: String, icon: ImageVector, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(KarikaUiColors.PinkSoft),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = icon, tint = KarikaUiColors.Pink, size = 19.dp)
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                text = label,
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 1
            )
            KarikaText(
                text = value,
                color = KarikaUiColors.Pink,
                textSize = 15.sp,
                lineHeight = 20.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
        }
        KIcon(icon = vectorResource(Res.drawable.ic_k_chevron_right), tint = KarikaUiColors.Subtle, size = 18.dp)
    }
}

package karika.distribucija.ba.ui.view.main.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.AppConfig
import karika.distribucija.ba.ui.common.openEmail
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TextArrowItem
import karika.distribucija.ba.ui.components.TextItem
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_phone
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenuView(component: MenuComponent) {
    var showSupportSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                color = KarikaColors.Gray2,
                text = "MENI",
                fontWeight = FontWeight.W700,
                textSize = 16.sp
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Divider
        )
        YSpacer16()
        TextArrowItem("Kategorije proizvoda", component::categories)
        TextItem("Blog", onClick = component::blog)
        TextItem("Samo na Kariki", onClick = component::karika)
        TextItem("Često postavljana pitanja") {
            component.appNavigate(AppConfig.Faq)
        }
        TextItem("Kontaktirajte nas", color = KarikaColors.Primary) {
            showSupportSheet = true
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
                    .padding(16.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = "Kontaktirajte nas",
                    fontWeight = FontWeight.Bold,
                    textSize = 20.sp,
                    color = KarikaColors.Gray2,
                    textAlign = TextAlign.Center
                )
                YSpacer16()
                KarikaText(
                    text = "Email:",
                    fontWeight = FontWeight.W400,
                    textSize = 16.sp,
                    color = KarikaColors.Gray2
                )
                Row(
                    modifier = Modifier
                        .clickable {
                            openEmail("info@karika.ba")
                        }
                        .height(40.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_email),
                        contentDescription = "",
                        tint = KarikaColors.Primary
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "info@karika.ba",
                        textSize = 16.sp,
                        color = KarikaColors.Primary,
                        fontWeight = FontWeight.W500
                    )
                }
                YSpacer8()
                HorizontalDivider(color = KarikaColors.Divider, thickness = 1.dp)
                YSpacer8()
                KarikaText(
                    text = "Telefon:",
                    fontWeight = FontWeight.W400,
                    textSize = 16.sp,
                    color = KarikaColors.Gray3
                )
                Row(
                    modifier = Modifier
                        .clickable {
                            openPhoneCall("033246830")
                        }
                        .height(40.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_phone),
                        contentDescription = "",
                        tint = KarikaColors.Primary
                    )
                    KarikaText(
                        modifier = Modifier,
                        text = "033/246-830",
                        textSize = 16.sp,
                        color = KarikaColors.Primary,
                        fontWeight = FontWeight.W500
                    )
                }
            }
        }
    }
}
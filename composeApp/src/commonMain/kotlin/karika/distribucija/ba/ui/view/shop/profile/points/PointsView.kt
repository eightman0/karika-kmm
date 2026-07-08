package karika.distribucija.ba.ui.view.shop.profile.points

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Transaction
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.components.roundedWithBorder
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_gift
import karikav2.composeapp.generated.resources.ic_info
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PointsView(component: PointsComponent) {
    val transactions by component.transactions.collectAsState()
    val state = rememberLazyListState()

    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Moji bodovi") {
                component.appBack()
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            state = state,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                ActiveBonus(component)
                YSpacer16()
                //PendingBonus(component)
                //YSpacer16()
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        modifier = Modifier
                            .weight(1f),
                        color = KarikaColors.Black,
                        fontWeight = FontWeight.W600,
                        textSize = 16.sp,
                        text = "Spisak trenutnih transakcija"
                    )
                    /*IconTextItem(
                        modifier = Modifier,
                        icon = vectorResource(Res.drawable.ic_arrow_right),
                        iconColor = KarikaColors.Primary,
                        iconSize = 14.dp,
                        text = "Vidi sve",
                        textColor = KarikaColors.Primary,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600,
                        iconPosition = FabPosition.End
                    )*/
                }
                YSpacer16()
            }
            items(items = transactions) { trx ->
                TrxItem(trx)
            }
        }

        LaunchedEffect(state.canScrollForward) {
            if (!state.canScrollForward) {
                component.loadNextPage()
            }
        }
    }
}

@Composable
private fun ActiveBonus(component: PointsComponent) {
    val bonus by component.points.collectAsState()

    Column(
        modifier = Modifier
            .rounded(color = KarikaColors.Green4, shape = 4.dp)
            .fillMaxWidth()
    ) {
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                color = KarikaColors.Black,
                fontWeight = FontWeight.W600,
                textSize = 20.sp,
                text = "Iznos ostvarenih bodova"
            )
           // Icon(
           //     imageVector = vectorResource(Res.drawable.ic_info),
           //     contentDescription = "",
           //     tint = KarikaColors.Black
           // )
        }
        YSpacer16()
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            color = KarikaColors.Black,
            fontWeight = FontWeight.W700,
            textSize = 28.sp,
            text = karikaPriceFormat(bonus.pointBalance) + " KM"
        )
        YSpacer16()
    }
}

@Composable
private fun PendingBonus(component: PointsComponent) {
    val bonus by component.points.collectAsState()
    Column(
        modifier = Modifier
            .rounded(color = KarikaColors.Pending, shape = 4.dp)
            .fillMaxWidth()
    ) {
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                color = KarikaColors.Black,
                fontWeight = FontWeight.W600,
                textSize = 20.sp,
                text = "Iznos bodova na čekanju"
            )
            Icon(
                imageVector = vectorResource(Res.drawable.ic_info),
                contentDescription = "",
                tint = KarikaColors.Black
            )
        }
        YSpacer16()
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            color = KarikaColors.Gray13,
            fontWeight = FontWeight.W600,
            textSize = 12.sp,
            text = "Zaključno sa 31.05.2025."
        )
        YSpacer16()
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            color = KarikaColors.Black,
            fontWeight = FontWeight.W700,
            textSize = 28.sp,
            text = karikaPriceFormat(bonus.pointSpent) + " KM"
        )
        YSpacer16()
    }
}

@Composable
private fun TrxItem(item: Transaction) {
    Column(
        modifier = Modifier
            .roundedWithBorder(
                color = KarikaColors.White,
                borderColor = KarikaColors.Border,
                shape = 4.dp
            )
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        YSpacer16()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = item.transactionId,
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray2,
                textSize = 14.sp
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = item.createdAt,
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray2,
                textSize = 14.sp
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = "BROJ TRANSAKCIJE",
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray13,
                textSize = 10.sp
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = "DATUM",
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray13,
                textSize = 10.sp
            )
        }
        YSpacer8()
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = item.id(),
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray2,
                textSize = 14.sp
            )
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = item.bonus(),
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray2,
                textSize = 14.sp
            )
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f),
                text = "BROJ NARUDŽBE",
                fontWeight = FontWeight.W600,
                color = KarikaColors.Gray13,
                textSize = 10.sp
            )
            Row(
                modifier = Modifier
                    .weight(1f)
            ) {
                Icon(
                    modifier = Modifier
                        .size(12.dp),
                    imageVector = vectorResource(Res.drawable.ic_gift),
                    tint = KarikaColors.Green1,
                    contentDescription = ""
                )
                KarikaText(
                    modifier = Modifier,
                    text = "OSTVARENI BONUS",
                    color = KarikaColors.Gray13,
                    textSize = 10.sp,
                    fontWeight = FontWeight.W600
                )
            }
        }
        YSpacer16()
    }
}

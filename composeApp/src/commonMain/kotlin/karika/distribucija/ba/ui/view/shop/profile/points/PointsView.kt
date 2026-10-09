package karika.distribucija.ba.ui.view.shop.profile.points

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
import androidx.compose.foundation.lazy.rememberLazyListState
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
import karika.distribucija.ba.domain.model.Transaction
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.util.karikaPriceFormat
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_info
import karikav2.composeapp.generated.resources.ic_k_gift
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PointsView(component: PointsComponent) {
    val transactions by component.transactions.collectAsState()
    val state = rememberLazyListState()

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
                KBackHeader(title = "Moji bodovi", onBack = { component.appBack() })
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .fillMaxSize()
                .background(KarikaUiColors.Page),
            state = state,
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                ActiveBonus(component)
                Spacer(Modifier.height(14.dp))
                //PendingBonus(component)
                KSectionTitle(title = "Spisak trenutnih transakcija")
            }
            items(items = transactions) { trx ->
                TrxItem(trx)
            }
            item {
                Column {
                    Spacer(Modifier.height(20.dp))
                    Spacer(Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
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

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(KarikaUiColors.Pink)
            .padding(18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            KarikaText(
                color = KarikaColors.White.copy(alpha = 0.85f),
                fontWeight = FontWeight.W600,
                textSize = 14.sp,
                text = "Iznos ostvarenih bodova"
            )
            Spacer(Modifier.height(8.dp))
            KarikaText(
                color = KarikaColors.White,
                fontWeight = FontWeight.W700,
                textSize = 28.sp,
                lineHeight = 32.sp,
                text = karikaPriceFormat(bonus.pointBalance) + " KM"
            )
        }
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(KarikaColors.White.copy(alpha = 0.18f)),
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_gift), tint = KarikaColors.White, size = 24.dp)
        }
    }
}

@Composable
private fun PendingBonus(component: PointsComponent) {
    val bonus by component.points.collectAsState()
    KCard(modifier = Modifier.fillMaxWidth(), background = KarikaUiColors.AmberSoft, border = null) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    color = KarikaUiColors.Ink,
                    fontWeight = FontWeight.W600,
                    textSize = 14.sp,
                    text = "Iznos bodova na čekanju"
                )
                KIcon(icon = vectorResource(Res.drawable.ic_info), tint = KarikaUiColors.Amber, size = 18.dp)
            }
            Spacer(Modifier.height(4.dp))
            KarikaText(
                color = KarikaUiColors.Muted,
                fontWeight = FontWeight.W500,
                textSize = 12.sp,
                text = "Zaključno sa 31.05.2025."
            )
            Spacer(Modifier.height(8.dp))
            KarikaText(
                color = KarikaUiColors.Ink,
                fontWeight = FontWeight.W700,
                textSize = 26.sp,
                lineHeight = 30.sp,
                text = karikaPriceFormat(bonus.pointSpent) + " KM"
            )
        }
    }
}

@Composable
private fun TrxItem(item: Transaction) {
    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            TrxValue(modifier = Modifier.weight(1f), label = "BROJ TRANSAKCIJE", value = item.transactionId)
            Spacer(Modifier.width(8.dp))
            TrxValue(modifier = Modifier.weight(1f), label = "DATUM", value = item.createdAt.inSarajevo())
        }
        KDivider()
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
            TrxValue(modifier = Modifier.weight(1f), label = "BROJ NARUDŽBE", value = item.id())
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    KIcon(icon = vectorResource(Res.drawable.ic_k_gift), tint = KarikaUiColors.Green, size = 12.dp)
                    Spacer(Modifier.width(4.dp))
                    KarikaText(
                        text = "OSTVARENI BONUS",
                        color = KarikaUiColors.Subtle,
                        textSize = 10.sp,
                        fontWeight = FontWeight.W600
                    )
                }
                Spacer(Modifier.height(4.dp))
                KarikaText(
                    text = item.bonus(),
                    color = KarikaUiColors.Green,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W700
                )
            }
        }
    }
}

@Composable
private fun TrxValue(modifier: Modifier, label: String, value: String?) {
    Column(modifier = modifier) {
        KarikaText(
            text = label,
            color = KarikaUiColors.Subtle,
            textSize = 10.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(4.dp))
        KarikaText(
            text = value,
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}

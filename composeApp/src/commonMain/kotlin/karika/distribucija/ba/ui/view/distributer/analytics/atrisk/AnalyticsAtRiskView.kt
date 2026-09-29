package karika.distribucija.ba.ui.view.distributer.analytics.atrisk

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.AtRiskCustomer
import karika.distribucija.ba.domain.model.CustomerRiskLevel
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.XSpacer8
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.analytics.AnalyticsEmptyState
import karika.distribucija.ba.util.karikaPriceFormat

@Composable
fun AnalyticsAtRiskView(component: AnalyticsAtRiskComponent) {
    val data by component.data.collectAsState()
    val selectedFilter by component.selectedFilter.collectAsState()
    val loader by component.loader.collectAsState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            text = "Kupci koji zahtijevaju pažnju",
            color = KarikaColors.Gray2,
            textSize = 20.sp,
            fontWeight = FontWeight.W700
        )
        KarikaText(
            text = "Neaktivnost se mjeri od danas, ne od odabranog perioda.",
            color = KarikaColors.Gray7,
            textSize = 12.sp,
            fontWeight = FontWeight.W400
        )

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            BucketChip("30–59 dana: ${data.bucket30to59}", KarikaColors.Yellow1, KarikaColors.Yellow2)
            BucketChip("60–89 dana: ${data.bucket60to89}", KarikaColors.Orange1.copy(alpha = 0.15f), KarikaColors.Orange1)
            BucketChip("90+ dana: ${data.bucket90plus}", KarikaColors.Red2, KarikaColors.Red3)
        }

        Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterPill("30+ dana", selectedFilter == AtRiskFilter.All) { component.selectFilter(AtRiskFilter.All) }
            FilterPill("Približava se riziku", selectedFilter == AtRiskFilter.ApproachingRisk) { component.selectFilter(AtRiskFilter.ApproachingRisk) }
            FilterPill("U riziku", selectedFilter == AtRiskFilter.AtRisk) { component.selectFilter(AtRiskFilter.AtRisk) }
            FilterPill("Ozbiljno kašnjenje", selectedFilter == AtRiskFilter.SeriouslyOverdue) { component.selectFilter(AtRiskFilter.SeriouslyOverdue) }
            FilterPill("Nikad naručio", selectedFilter == AtRiskFilter.NeverOrdered) { component.selectFilter(AtRiskFilter.NeverOrdered) }
        }

        val filtered = data.customers.filter { customer ->
            when (selectedFilter) {
                AtRiskFilter.All -> true
                AtRiskFilter.ApproachingRisk -> customer.riskLevel == CustomerRiskLevel.APPROACHING_RISK
                AtRiskFilter.AtRisk -> customer.riskLevel == CustomerRiskLevel.AT_RISK
                AtRiskFilter.SeriouslyOverdue -> customer.riskLevel == CustomerRiskLevel.SERIOUSLY_OVERDUE
                AtRiskFilter.NeverOrdered -> customer.riskLevel == CustomerRiskLevel.NEVER_ORDERED
            }
        }

        if (filtered.isEmpty()) {
            AnalyticsEmptyState(loader)
        }

        filtered.forEach { customer ->
            CustomerCard(customer)
        }
    }
}

@Composable
private fun BucketChip(text: String, background: Color, textColor: Color) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(background)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        KarikaText(text = text, color = textColor, textSize = 12.sp, fontWeight = FontWeight.W600)
    }
}

@Composable
private fun FilterPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) KarikaColors.Blue else KarikaColors.Gray20)
            .onClick(callback = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        KarikaText(
            text = text,
            color = if (selected) KarikaColors.White else KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = FontWeight.W600
        )
    }
}

private fun CustomerRiskLevel.label() = when (this) {
    CustomerRiskLevel.APPROACHING_RISK -> "Približava se riziku"
    CustomerRiskLevel.AT_RISK -> "U riziku"
    CustomerRiskLevel.SERIOUSLY_OVERDUE -> "Ozbiljno kašnjenje"
    CustomerRiskLevel.NEVER_ORDERED -> "Nikad naručio"
}

private fun CustomerRiskLevel.background() = when (this) {
    CustomerRiskLevel.APPROACHING_RISK -> KarikaColors.Yellow1
    CustomerRiskLevel.AT_RISK -> KarikaColors.Orange1.copy(alpha = 0.15f)
    CustomerRiskLevel.SERIOUSLY_OVERDUE -> KarikaColors.Red2
    CustomerRiskLevel.NEVER_ORDERED -> KarikaColors.Gray20
}

private fun CustomerRiskLevel.textColor() = when (this) {
    CustomerRiskLevel.APPROACHING_RISK -> KarikaColors.Yellow2
    CustomerRiskLevel.AT_RISK -> KarikaColors.Orange1
    CustomerRiskLevel.SERIOUSLY_OVERDUE -> KarikaColors.Red3
    CustomerRiskLevel.NEVER_ORDERED -> KarikaColors.Gray2
}

@Composable
private fun CustomerCard(customer: AtRiskCustomer) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            KarikaText(text = customer.displayId, color = KarikaColors.Blue, textSize = 15.sp, fontWeight = FontWeight.W700)
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(customer.riskLevel.background())
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                KarikaText(text = customer.riskLevel.label(), color = customer.riskLevel.textColor(), textSize = 11.sp, fontWeight = FontWeight.W600)
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            KarikaText(text = customer.name, color = KarikaColors.Gray2, textSize = 14.sp, fontWeight = FontWeight.W500)
            if (customer.suspended) {
                XSpacer8()
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(KarikaColors.Gray20)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    KarikaText(text = "Suspendovan", color = KarikaColors.Gray2, textSize = 10.sp, fontWeight = FontWeight.W600)
                }
            }
        }
        customer.note?.let {
            KarikaText(text = it, color = KarikaColors.Gray7, textSize = 11.sp, fontWeight = FontWeight.W400)
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            AtRiskStat("Neaktivan", "${customer.inactiveDays} d", KarikaColors.Red)
            AtRiskStat("Zadnja", customer.lastOrderDate)
            AtRiskStat("Prosj. vrij.", "${karikaPriceFormat(customer.avgOrderValue)} KM")
            AtRiskStat("Promet", "${karikaPriceFormat(customer.turnover)} KM")
        }
    }
}

@Composable
private fun AtRiskStat(label: String, value: String, valueColor: Color = KarikaColors.Gray2) {
    Column {
        KarikaText(text = label, color = KarikaColors.Gray7, textSize = 11.sp, fontWeight = FontWeight.W400)
        KarikaText(text = value, color = valueColor, textSize = 13.sp, fontWeight = FontWeight.W700)
    }
}

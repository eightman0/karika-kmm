package karika.distribucija.ba.ui.view.distributer.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KConfirmDialog
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaCardShape
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.VendorAccentSoft
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_plus
import karikav2.composeapp.generated.resources.ic_k_trash
import org.jetbrains.compose.resources.vectorResource

/** Test tag of every discount rule card in the "Rabati" list. */
const val CUSTOMER_RULE_TAG = "customer_rule"

@Composable
fun CustomersView(component: CustomersComponent) {
    val customerRules by component.customerRules.collectAsState()
    val customerTypeRules by component.customerTypeRules.collectAsState()
    val customerRegionRules by component.customerRegionRules.collectAsState()

    var ruleToDelete by remember { mutableStateOf<CustomerRule?>(null) }

    LazyColumn(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item { PriorityCard() }
        item {
            RuleSection(
                number = 1,
                title = "Po kupcu",
                subtitle = "Imaju prednost nad tipom i regijom.",
                rules = customerRules,
                onAddRow = { component.addCustomerRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
        item {
            RuleSection(
                number = 2,
                title = "Po tipu kupca",
                subtitle = "Primjenjuje se kad nema pravila za kupca.",
                rules = customerTypeRules,
                onAddRow = { component.addCustomerTypeRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER_TYPE, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
        item {
            RuleSection(
                number = 3,
                title = "Po regiji kupca",
                subtitle = "Primjenjuje se kad nema pravila ni za kupca ni za tip.",
                rules = customerRegionRules,
                onAddRow = { component.addCustomerRegionRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER_REGION, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
    }

    ruleToDelete?.let { rule ->
        KConfirmDialog(
            title = "Obriši pravilo",
            message = "Jeste li sigurni da želite obrisati ovo pravilo?",
            icon = vectorResource(Res.drawable.ic_k_trash),
            confirmText = "Obriši",
            dismissText = "Odustani",
            onConfirm = {
                component.deleteRule(rule)
                ruleToDelete = null
            },
            onDismiss = {
                ruleToDelete = null
            }
        )
    }

    LaunchedEffect(Unit) {
        component.loadRules()
    }
}

/** "Redoslijed primjene rabata": Kupac › Tip kupca › Regija. */
@Composable
private fun PriorityCard() {
    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(modifier = Modifier.padding(14.dp)) {
            KarikaText(
                text = "Redoslijed primjene rabata",
                color = KarikaUiColors.Muted,
                textSize = 12.sp,
                fontWeight = FontWeight.W500
            )
            Spacer(Modifier.height(10.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("Kupac", "Tip kupca", "Regija").forEachIndexed { index, label ->
                    if (index > 0) {
                        KIcon(
                            icon = vectorResource(Res.drawable.ic_k_chevron_right),
                            tint = KarikaUiColors.Subtle,
                            size = 14.dp
                        )
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        StepCircle(number = index + 1, first = index == 0)
                        KarikaText(
                            text = label,
                            color = KarikaUiColors.Ink,
                            textSize = 12.5.sp,
                            fontWeight = FontWeight.W600,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StepCircle(number: Int, first: Boolean) {
    Box(
        modifier = Modifier
            .size(20.dp)
            .clip(CircleShape)
            .background(if (first) VendorAccent else KarikaColors.White)
            .border(1.dp, if (first) VendorAccent else KarikaUiColors.Border, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = number.toString(),
            color = if (first) KarikaColors.White else KarikaUiColors.Muted,
            textSize = 11.sp,
            fontWeight = FontWeight.W700,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun RuleSection(
    number: Int,
    title: String,
    subtitle: String,
    rules: List<CustomerRule>,
    onAddRow: () -> Unit,
    onEditRow: (CustomerRule) -> Unit,
    onDeleteRow: (CustomerRule) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .padding(top = 1.dp)
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(if (number == 1) VendorAccent else KarikaUiColors.Ink),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = number.toString(),
                    color = KarikaColors.White,
                    textSize = 12.sp,
                    fontWeight = FontWeight.W700,
                    textAlign = TextAlign.Center
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = title,
                    color = KarikaUiColors.Ink,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                Spacer(Modifier.height(2.dp))
                KarikaText(
                    text = subtitle,
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.W400
                )
            }
            AddButton(onClick = onAddRow)
        }

        if (rules.isEmpty()) {
            KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
                KarikaText(
                    modifier = Modifier
                        .padding(14.dp)
                        .fillMaxWidth(),
                    text = "Još nema pravila — dodajte ga sa „Dodaj“.",
                    color = KarikaUiColors.Muted,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W400
                )
            }
        } else {
            rules.forEach { rule ->
                SwipeableRuleCard(
                    rule = rule,
                    onEdit = { onEditRow(rule) },
                    onDelete = { onDeleteRow(rule) }
                )
            }
        }
    }
}

@Composable
private fun AddButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .height(32.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(VendorAccentSoft)
            .clickable(onClick = onClick)
            .padding(start = 10.dp, end = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = VendorAccent, size = 15.dp)
        Spacer(Modifier.width(4.dp))
        KarikaText(
            text = "Dodaj",
            color = VendorAccent,
            textSize = 12.5.sp,
            fontWeight = FontWeight.W600,
            maxLines = 1
        )
    }
}

@Composable
private fun SwipeableRuleCard(
    rule: CustomerRule,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
            }
            false
        }
    )
    val shape = RoundedCornerShape(14.dp)

    SwipeToDismissBox(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape),
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            val swiping = dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (swiping) KarikaUiColors.Red else Color.Transparent)
                    .padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End
            ) {
                if (swiping) {
                    KIcon(
                        icon = vectorResource(Res.drawable.ic_k_trash),
                        tint = KarikaColors.White,
                        size = 22.dp
                    )
                }
            }
        }
    ) {
        RuleCard(rule = rule, onClick = onEdit)
    }
}

@Composable
private fun RuleCard(rule: CustomerRule, onClick: () -> Unit) {
    KCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag(CUSTOMER_RULE_TAG),
        shape = RoundedCornerShape(14.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                KarikaText(
                    text = rule.targetName,
                    color = KarikaUiColors.Ink,
                    textSize = 14.5.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 2
                )
                Spacer(Modifier.height(3.dp))
                KarikaText(
                    text = rule.detailLine(),
                    color = KarikaUiColors.Muted,
                    textSize = 12.5.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.W400,
                    maxLines = 2
                )
            }
            Box(
                modifier = Modifier
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(VendorAccentSoft)
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                KarikaText(
                    text = rule.percentLabel(),
                    color = VendorAccent,
                    textSize = 15.sp,
                    fontWeight = FontWeight.W700,
                    maxLines = 1
                )
            }
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_chevron_right),
                tint = KarikaUiColors.Subtle,
                size = 18.dp
            )
        }
    }
}

/** "Graševina Kutjevo · min. 30 kom": the product or category and the minimum quantity, if any. */
private fun CustomerRule.detailLine(): String {
    val item = when (itemType) {
        "category" -> "Kategorija: $itemOrCategoryName"
        else -> itemOrCategoryName
    }
    val minQty = minQtyForDiscount.toDoubleOrNull()?.toInt()
    return if (minQty != null) "$item · min. $minQty kom" else item
}

/** "10%" for 10.0, "7,5%" for 7.5. */
private fun CustomerRule.percentLabel(): String {
    val value = discountPercent.toDoubleOrNull()
    val text = when {
        value == null -> discountPercent
        value % 1.0 == 0.0 -> value.toLong().toString()
        else -> discountPercent.replace(".", ",")
    }
    return "$text%"
}

package karika.distribucija.ba.ui.view.distributer.customers

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.shop.profile.account.ConfirmationModal
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_add_plus
import karikav2.composeapp.generated.resources.ic_arrow_right
import karikav2.composeapp.generated.resources.ic_delete
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CustomersView(component: CustomersComponent) {
    val customerRules by component.customerRules.collectAsState()
    val customerTypeRules by component.customerTypeRules.collectAsState()
    val customerRegionRules by component.customerRegionRules.collectAsState()

    var ruleToDelete by remember { mutableStateOf<CustomerRule?>(null) }

    LazyColumn(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = "Upravljanje rabatima",
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W700
            )
            YSpacer16()
        }
        item {
            RuleSection(
                title = "Postavke po kupcu",
                subtitle = "Pravila po kupcu imaju prednost nad tipom i regijom.",
                targetLabel = "Kupac:",
                rules = customerRules,
                onAddRow = { component.addCustomerRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
        item {
            RuleSection(
                title = "Postavke po tipu kupca",
                subtitle = "Tip kupca primjenjuje se kada nema pravila za tog kupca.",
                targetLabel = "Tip:",
                rules = customerTypeRules,
                onAddRow = { component.addCustomerTypeRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER_TYPE, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
        item {
            RuleSection(
                title = "Postavke po regiji kupca",
                subtitle = "Regija je posljednja u prioritetu i primjenjuje se kada nema pravila za kupca ni tip.",
                targetLabel = "Regija:",
                rules = customerRegionRules,
                onAddRow = { component.addCustomerRegionRule() },
                onEditRow = { component.editRule(RuleScope.CUSTOMER_REGION, it) },
                onDeleteRow = { ruleToDelete = it }
            )
        }
    }

    ruleToDelete?.let { rule ->
        ConfirmationModal(
            title = "Obriši pravilo",
            message = "Jeste li sigurni da želite obrisati ovo pravilo?",
            primaryButtonText = "Obriši",
            secondaryButtonText = "Odustani",
            onPrimaryClick = {
                component.deleteRule(rule)
                ruleToDelete = null
            },
            onSecondaryClick = {
                ruleToDelete = null
            },
            type = 1
        )
    }

    LaunchedEffect(Unit) {
        component.loadRules()
    }
}

@Composable
private fun RuleSection(
    title: String,
    subtitle: String,
    targetLabel: String,
    rules: List<CustomerRule>,
    onAddRow: () -> Unit,
    onEditRow: (CustomerRule) -> Unit,
    onDeleteRow: (CustomerRule) -> Unit
) {
    Column(
        modifier = Modifier
            .background(color = KarikaColors.White, shape = RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = KarikaColors.Border,
                shape = RoundedCornerShape(6.dp)
            )
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = title,
                    color = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KarikaText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = subtitle,
                    color = KarikaColors.Gray15,
                    textSize = 13.sp,
                    fontWeight = FontWeight.W400
                )
            }
            SecondaryButtonFilled(
                modifier = Modifier
                    .height(36.dp),
                title = "Dodaj red",
                icon = Res.drawable.ic_add_plus,
                fontWeight = FontWeight.W600,
                textSize = 14.sp,
                contentPadding = PaddingValues(8.dp),
                onClick = onAddRow
            )
        }
        HorizontalDivider(color = KarikaColors.Border)

        if (rules.isEmpty()) {
            KarikaText(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                text = "Još nema pravila — kliknite \"Dodaj red\".",
                color = KarikaColors.Gray15,
                textSize = 13.sp,
                fontWeight = FontWeight.W400
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(0.dp)
            ) {
                rules.forEach { rule ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                onDeleteRow(rule)
                                false
                            } else {
                                false
                            }
                        }
                    )

                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            val color = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> Color.Red
                                else -> Color.Transparent
                            }
                            val arrangement = Arrangement.End
                            val icon = when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.EndToStart -> Res.drawable.ic_delete
                                else -> null
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(color)
                                    .padding(horizontal = 20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = arrangement
                            ) {
                                if (icon != null) {
                                    Icon(
                                        imageVector = vectorResource(icon),
                                        contentDescription = null,
                                        tint = Color.White
                                    )
                                }
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .background(KarikaColors.White)
                                .onClick { onEditRow(rule) }
                                .padding(16.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                RuleInfoRow(label = targetLabel, value = rule.targetName)
                                RuleInfoRow(
                                    label = rule.itemOrCategoryLabel,
                                    value = rule.itemOrCategoryName
                                )
                                rule.minQtyForDiscount.toDoubleOrNull()?.toInt()?.let { minQty ->
                                    RuleInfoRow(
                                        label = "Min. količina:",
                                        value = minQty.toString()
                                    )
                                }
                                RuleInfoRow(
                                    label = "Rabat:",
                                    value = "${rule.discountPercent.replace(".", ",")}%",
                                    isHighlight = true
                                )
                            }
                            Icon(
                                imageVector = vectorResource(Res.drawable.ic_arrow_right),
                                contentDescription = null,
                                tint = KarikaColors.Gray15
                            )
                        }
                    }
                    HorizontalDivider(color = KarikaColors.Border)
                }
            }
        }
    }
}

@Composable
private fun RuleInfoRow(
    label: String,
    value: String,
    isHighlight: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KarikaText(
            text = label,
            color = KarikaColors.Gray15,
            textSize = 13.sp,
            fontWeight = FontWeight.W400
        )
        KarikaText(
            text = value,
            color = if (isHighlight) KarikaColors.Primary else KarikaColors.Gray2,
            textSize = 13.sp,
            fontWeight = if (isHighlight) FontWeight.W700 else FontWeight.W600
        )
    }
}

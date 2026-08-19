package karika.distribucija.ba.ui.view.distributer.customers.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.customers.RuleScope
import karika.distribucija.ba.ui.view.distributer.customers.targetFieldPlaceholder
import karika.distribucija.ba.ui.view.distributer.customers.targetFieldTitle
import karika.distribucija.ba.ui.view.distributer.customers.title
import karika.distribucija.ba.ui.view.distributer.customers.backLabel
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_shopping_cart
import karikav2.composeapp.generated.resources.ic_navigation_category
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CustomerRuleEditorView(component: CustomerRuleEditorComponent) {
    Column(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Header(component)
        FormCard(component)
    }
}

@Composable
private fun Header(component: CustomerRuleEditorComponent) {
    IconTextItem(
        modifier = Modifier
            .onClick { component.cancel() },
        icon = vectorResource(Res.drawable.ic_arrow_back),
        iconColor = KarikaColors.Gray2,
        textColor = KarikaColors.Gray2,
        text = component.ruleScope.backLabel(),
        fontWeight = FontWeight.W400,
        textSize = 14.sp,
        iconPosition = FabPosition.Start
    )
    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = if (component.isEditing) {
            "Izmjena pravila — ${component.ruleScope.title().lowercase()}"
        } else {
            "Dodavanje pravila — ${component.ruleScope.title().lowercase()}"
        },
        color = KarikaColors.Gray2,
        textSize = 18.sp,
        fontWeight = FontWeight.W700
    )
}

@Composable
private fun FormCard(component: CustomerRuleEditorComponent) {
    val target = component.target.asState()
    val itemOrCategorySearchText = component.itemOrCategorySearchText.asState()
    val minQty = component.minQtyForDiscount.asState()
    val discount = component.discountPercent.asState()
    val dropdownState = remember { mutableStateOf(false) }.asState()
    val itemDropdownState = remember { mutableStateOf(false) }.asState()
    val customerTypeDropdownState = remember { mutableStateOf(false) }.asState()
    val customerRegionDropdownState = remember { mutableStateOf(false) }.asState()
    val shops by component.shops.collectAsState()
    val searchResults by component.searchResults.collectAsState()
    val filteredCustomerRegions by component.filteredCustomerRegions.collectAsState()
    val filteredCustomerGroups by component.filteredCustomerGroups.collectAsState()

    Column(
        modifier = Modifier
            .background(color = KarikaColors.White, shape = RoundedCornerShape(6.dp))
            .border(
                width = 1.dp,
                color = KarikaColors.Border,
                shape = RoundedCornerShape(6.dp)
            )
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (component.ruleScope == RuleScope.CUSTOMER) {
            CustomerTargetDropdown(
                component = component,
                target = target,
                dropdownState = dropdownState,
                shops = shops
            )
        } else if (component.ruleScope == RuleScope.CUSTOMER_TYPE) {
            CustomerTypeDropdown(
                component = component,
                target = target,
                dropdownState = customerTypeDropdownState,
                groups = filteredCustomerGroups
            )
        } else if (component.ruleScope == RuleScope.CUSTOMER_REGION) {
            CustomerRegionDropdown(
                component = component,
                target = target,
                dropdownState = customerRegionDropdownState,
                regions = filteredCustomerRegions
            )
        } else {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = component.ruleScope.targetFieldTitle(),
                value = target,
                placeholder = component.ruleScope.targetFieldPlaceholder(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                trailingIcons = {
                    Icon(
                        imageVector = vectorResource(Res.drawable.ic_arrow_down),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                }
            )
        }
        ItemOrCategoryDropdown(
            component = component,
            itemOrCategory = itemOrCategorySearchText,
            dropdownState = itemDropdownState,
            searchResults = searchResults
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Min. količina za rabat",
            value = minQty,
            placeholder = "0",
            keyboardType = KeyboardType.Number,
            allowedChars = KarikaConstants.numbers,
            imeAction = ImeAction.Next,
            leadingZero = false
        )
        KarikaTextField1(
            modifier = Modifier
                .fillMaxWidth(),
            title = "Rabat %",
            value = discount,
            placeholder = "0",
            keyboardType = KeyboardType.Decimal,
            allowedChars = KarikaConstants.numbers.plus(","),
            imeAction = ImeAction.Done,
            leadingZero = false,
            trailingIcons = {
                KarikaText(
                    modifier = Modifier,
                    text = "%",
                    color = KarikaColors.Gray4,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W400
                )
            }
        )
        ActionRow(component)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerTargetDropdown(
    component: CustomerRuleEditorComponent,
    target: androidx.compose.runtime.MutableState<String>,
    dropdownState: androidx.compose.runtime.MutableState<Boolean>,
    shops: List<karika.distribucija.ba.domain.model.Shop>
) {
    val focus = LocalFocusManager.current
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = dropdownState.value,
        onExpandedChange = { dropdownState.value = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        ) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = component.ruleScope.targetFieldTitle(),
                value = target,
                placeholder = component.ruleScope.targetFieldPlaceholder(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onValueChange = {
                    component.onTargetChanged(it)
                    dropdownState.value = true
                },
                trailingIcons = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = dropdownState.value
                    )
                },
                enabled = true
            )
        }

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = dropdownState.value,
            onDismissRequest = { dropdownState.negate() },
            containerColor = KarikaColors.White
        ) {
            if (target.value.length < 2) {
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = "Pretraži kupce (unesite najmanje 2 znaka).",
                            color = KarikaColors.Gray4,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            } else if (shops.isEmpty() && target.value.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = "Nema rezultata",
                            color = KarikaColors.Gray4,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            } else {
                shops.forEach { shop ->
                    DropdownMenuItem(
                        text = {
                            KarikaText(
                                text = shop.name.orEmpty(),
                                color = KarikaColors.Gray4,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400
                            )
                        },
                        onClick = {
                            component.onShopSelected(shop)
                            dropdownState.negate()
                            focus.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerTypeDropdown(
    component: CustomerRuleEditorComponent,
    target: androidx.compose.runtime.MutableState<String>,
    dropdownState: androidx.compose.runtime.MutableState<Boolean>,
    groups: List<karika.distribucija.ba.domain.model.KarikaUnit>
) {
    val focus = LocalFocusManager.current
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = dropdownState.value,
        onExpandedChange = { dropdownState.value = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        ) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = component.ruleScope.targetFieldTitle(),
                value = target,
                placeholder = component.ruleScope.targetFieldPlaceholder(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                disabledTextColor = KarikaColors.Gray2,
                onValueChange = {
                    component.onTargetChanged(it)
                    dropdownState.value = true
                },
                trailingIcons = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = dropdownState.value
                    )
                },
                enabled = true
            )
        }

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = dropdownState.value,
            onDismissRequest = { dropdownState.negate() },
            containerColor = KarikaColors.White
        ) {
            groups.forEach { group ->
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = group.label(),
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {
                        component.onCustomerGroupSelected(group.label())
                        dropdownState.negate()
                        focus.clearFocus()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomerRegionDropdown(
    component: CustomerRuleEditorComponent,
    target: androidx.compose.runtime.MutableState<String>,
    dropdownState: androidx.compose.runtime.MutableState<Boolean>,
    regions: List<karika.distribucija.ba.domain.model.KarikaUnit>
) {
    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = dropdownState.value,
        onExpandedChange = { dropdownState.value = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        ) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = component.ruleScope.targetFieldTitle(),
                value = target,
                placeholder = component.ruleScope.targetFieldPlaceholder(),
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onValueChange = {
                    component.onTargetChanged(it)
                    dropdownState.value = true
                },
                trailingIcons = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = dropdownState.value
                    )
                },
                enabled = true
            )
        }

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = dropdownState.value,
            onDismissRequest = { dropdownState.negate() },
            containerColor = KarikaColors.White
        ) {
            regions.forEach { region ->
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = region.label(),
                            color = KarikaColors.Gray4,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {
                        component.onCustomerRegionSelected(region)
                        dropdownState.negate()
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ItemOrCategoryDropdown(
    component: CustomerRuleEditorComponent,
    itemOrCategory: androidx.compose.runtime.MutableState<String>,
    dropdownState: androidx.compose.runtime.MutableState<Boolean>,
    searchResults: List<SearchItem>
) {
    val focus = LocalFocusManager.current

    ExposedDropdownMenuBox(
        modifier = Modifier.fillMaxWidth(),
        expanded = dropdownState.value,
        onExpandedChange = { dropdownState.value = it }
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable)
        ) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Odaberi artikal ili kategoriju",
                value = itemOrCategory,
                placeholder = "Svi artikli ili kategorije",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                onValueChange = {
                    component.onItemOrCategoryChanged(it)
                    dropdownState.value = true
                },
                trailingIcons = {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = dropdownState.value
                    )
                },
                enabled = true
            )
        }

        ExposedDropdownMenu(
            modifier = Modifier.exposedDropdownSize(),
            expanded = dropdownState.value,
            onDismissRequest = { dropdownState.negate() },
            containerColor = KarikaColors.White
        ) {
            if (itemOrCategory.value.length < 2) {
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = "Pretraži artikle i kategorije (unesite najmanje 2 znaka).",
                            color = KarikaColors.Gray4,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            } else if (searchResults.isEmpty() && itemOrCategory.value.isNotEmpty()) {
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = "Nema rezultata",
                            color = KarikaColors.Gray4,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {},
                    enabled = false
                )
            } else {
                searchResults.forEach { item ->
                    val icon = when (item) {
                        is SearchItem.ProductItem -> vectorResource(Res.drawable.ic_shopping_cart)
                        is SearchItem.CategoryItem -> vectorResource(Res.drawable.ic_navigation_category)
                    }

                    val iconColor = when (item) {
                        is SearchItem.ProductItem -> KarikaColors.MineMessage
                        is SearchItem.CategoryItem -> KarikaColors.Green5
                    }

                    DropdownMenuItem(
                        text = {
                            IconTextItem(
                                icon = icon,
                                iconSize = 20.dp,
                                text = item.displayName,
                                textColor = KarikaColors.Gray4,
                                iconColor = iconColor,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400,
                                textAlign = TextAlign.Start
                            )
                        },
                        onClick = {
                            component.onItemSelected(item)
                            dropdownState.negate()
                            focus.clearFocus()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ActionRow(component: CustomerRuleEditorComponent) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (component.isEditing) {
            PrimaryButton(
                modifier = Modifier
                    .height(40.dp)
                    .weight(1f),
                title = "Obriši",
                color = KarikaColors.Blue,
                fontWeight = FontWeight.W600,
                textSize = 14.sp,
                contentPadding = PaddingValues(horizontal = 8.dp)
            ) {
                component.delete()
            }
        }
        SecondaryButtonFilled(
            modifier = Modifier
                .height(40.dp)
                .weight(1f),
            title = if (component.isEditing) "Izmijeni" else "Sačuvaj",
            fontWeight = FontWeight.W600,
            textSize = 14.sp,
            contentPadding = PaddingValues(horizontal = 8.dp)
        ) {
            component.save()
        }
    }
}

package karika.distribucija.ba.ui.view.distributer.customers.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Shop
import karika.distribucija.ba.ui.components.KBackHeader
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KFieldLabel
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSecondaryButton
import karika.distribucija.ba.ui.components.KTextField
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.customers.RuleScope
import karika.distribucija.ba.ui.view.distributer.customers.targetFieldPlaceholder
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_document
import karikav2.composeapp.generated.resources.ic_k_info
import karikav2.composeapp.generated.resources.ic_k_pin
import karikav2.composeapp.generated.resources.ic_k_tag
import karikav2.composeapp.generated.resources.ic_k_trash
import karikav2.composeapp.generated.resources.ic_k_user
import karikav2.composeapp.generated.resources.ic_k_users
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CustomerRuleEditorView(component: CustomerRuleEditorComponent) {
    Column(
        modifier = Modifier
            .background(color = KarikaUiColors.Page)
            .fillMaxSize()
    ) {
        KBackHeader(
            title = if (component.isEditing) "Izmjena pravila" else "Novo pravilo",
            overline = component.ruleScope.overline(),
            onBack = { component.cancel() }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FormCard(component)
            InfoNote(component.ruleScope.infoText())
            if (component.isEditing) {
                DeleteButton(onClick = { component.delete() })
            }
        }
        KBottomPanel {
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                KSecondaryButton(
                    modifier = Modifier.weight(1f),
                    text = "Odustani",
                    onClick = { component.cancel() }
                )
                KPrimaryButton(
                    modifier = Modifier.weight(1.6f),
                    text = if (component.isEditing) "Sačuvaj izmjene" else "Sačuvaj pravilo",
                    background = VendorAccent,
                    onClick = { component.save() }
                )
            }
        }
    }
}

private fun RuleScope.overline(): String = when (this) {
    RuleScope.CUSTOMER -> "Rabati · po kupcu"
    RuleScope.CUSTOMER_TYPE -> "Rabati · po tipu kupca"
    RuleScope.CUSTOMER_REGION -> "Rabati · po regiji kupca"
}

private fun RuleScope.fieldLabel(): String = when (this) {
    RuleScope.CUSTOMER -> "Kupac"
    RuleScope.CUSTOMER_TYPE -> "Tip kupca"
    RuleScope.CUSTOMER_REGION -> "Regija kupca"
}

private fun RuleScope.infoText(): String = when (this) {
    RuleScope.CUSTOMER ->
        "Pravilo po kupcu ima najveći prioritet i poništava pravila po tipu i regiji za istog kupca."
    RuleScope.CUSTOMER_TYPE ->
        "Pravilo po tipu kupca primjenjuje se kad za kupca nema pravila po kupcu."
    RuleScope.CUSTOMER_REGION ->
        "Pravilo po regiji primjenjuje se kad za kupca nema pravila ni po kupcu ni po tipu."
}

@Composable
private fun FormCard(component: CustomerRuleEditorComponent) {
    val target = component.target.asState()
    val itemOrCategorySearchText = component.itemOrCategorySearchText.asState()
    val minQty = component.minQtyForDiscount.asState()
    val discount = component.discountPercent.asState()
    val dropdownState = remember { mutableStateOf(false) }.asState()
    val itemDropdownState = remember { mutableStateOf(false) }.asState()
    val shops by component.shops.collectAsState()
    val searchResults by component.searchResults.collectAsState()

    KCard(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(14.dp)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (component.ruleScope) {
                RuleScope.CUSTOMER -> CustomerTargetDropdown(
                    component = component,
                    target = target,
                    dropdownState = dropdownState,
                    shops = shops
                )

                RuleScope.CUSTOMER_TYPE -> DropdownField(
                    label = component.ruleScope.fieldLabel(),
                    icon = vectorResource(Res.drawable.ic_k_users),
                    value = target,
                    values = component.customerGroupLabels.asState().value,
                    placeholder = component.ruleScope.targetFieldPlaceholder(),
                    onChange = { component.onCustomerGroupSelected(it) }
                )

                RuleScope.CUSTOMER_REGION -> DropdownField(
                    label = component.ruleScope.fieldLabel(),
                    icon = vectorResource(Res.drawable.ic_k_pin),
                    value = target,
                    values = component.customerRegionLabels.asState().value,
                    placeholder = component.ruleScope.targetFieldPlaceholder(),
                    onChange = { component.onCustomerRegionSelected(it) }
                )
            }
            ItemOrCategoryDropdown(
                component = component,
                itemOrCategory = itemOrCategorySearchText,
                dropdownState = itemDropdownState,
                searchResults = searchResults
            )
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Column(modifier = Modifier.weight(1f)) {
                    KFieldLabel(text = "Min. količina")
                    NumberField(
                        value = minQty,
                        allowedChars = KarikaConstants.numbers,
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next,
                        suffix = "kom"
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    KFieldLabel(text = "Rabat", required = true, requiredColor = VendorAccent)
                    NumberField(
                        value = discount,
                        allowedChars = KarikaConstants.numbers.plus(","),
                        keyboardType = KeyboardType.Decimal,
                        imeAction = ImeAction.Done,
                        suffix = "%"
                    )
                }
            }
        }
    }
}

/** Bordered number field; like before, it takes only [allowedChars] and no leading zero. */
@Composable
private fun NumberField(
    value: MutableState<String>,
    allowedChars: List<String>,
    keyboardType: KeyboardType,
    imeAction: ImeAction,
    suffix: String
) {
    KTextField(
        value = value.value,
        onValueChange = { text ->
            if (text.startsWith("0") || text.startsWith(" ")) return@KTextField
            if (text.any { !allowedChars.contains(it.toString()) }) return@KTextField
            value.value = text
        },
        placeholder = "0",
        keyboardType = keyboardType,
        imeAction = imeAction,
        minHeight = 48.dp,
        trailing = {
            KarikaText(text = suffix, color = KarikaUiColors.Muted, textSize = 13.sp, fontWeight = FontWeight.W600)
        }
    )
}

/** Non-editable dropdown drawn as a bordered field with a chevron (as in the registration form). */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DropdownField(
    label: String,
    icon: ImageVector,
    value: MutableState<String>,
    values: List<String>,
    placeholder: String,
    onChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(12.dp)
    Column(modifier = Modifier.fillMaxWidth()) {
        KFieldLabel(text = label, required = true, requiredColor = VendorAccent)
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            Row(
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
                    .height(48.dp)
                    .clip(shape)
                    .background(KarikaColors.White)
                    .border(1.dp, if (expanded) VendorAccent else KarikaUiColors.Border, shape)
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KIcon(icon = icon, tint = KarikaUiColors.Muted, size = 18.dp)
                Spacer(Modifier.width(10.dp))
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = value.value.ifEmpty { placeholder },
                    color = if (value.value.isEmpty()) KarikaUiColors.Subtle else KarikaUiColors.Ink,
                    textSize = 14.5.sp,
                    maxLines = 1
                )
                KIcon(
                    icon = vectorResource(Res.drawable.ic_k_chevron_down),
                    tint = KarikaUiColors.Muted,
                    size = 18.dp
                )
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                shape = shape,
                containerColor = KarikaColors.White
            ) {
                values.forEachIndexed { index, option ->
                    DropdownMenuItem(
                        onClick = {
                            value.value = option
                            expanded = false
                            onChange(option)
                        },
                        text = {
                            KarikaText(
                                text = option,
                                fontWeight = if (option == value.value) FontWeight.W700 else FontWeight.W400,
                                textSize = 15.sp,
                                color = if (option == value.value) VendorAccent else KarikaUiColors.Ink
                            )
                        }
                    )
                    if (index < values.size - 1) {
                        KDivider()
                    }
                }
            }
        }
    }
}

/** Bordered search field with a dropdown of results under it. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchDropdownField(
    label: String,
    icon: ImageVector,
    value: String,
    placeholder: String,
    dropdownState: MutableState<Boolean>,
    onValueChange: (String) -> Unit,
    menu: @Composable () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        KFieldLabel(text = label)
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
                KTextField(
                    value = value,
                    onValueChange = { text ->
                        if (text.startsWith(" ")) return@KTextField
                        onValueChange(text)
                        dropdownState.value = true
                    },
                    placeholder = placeholder,
                    leadingIcon = icon,
                    minHeight = 48.dp,
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next,
                    trailing = {
                        KIcon(
                            icon = vectorResource(Res.drawable.ic_k_chevron_down),
                            tint = KarikaUiColors.Muted,
                            size = 18.dp
                        )
                    }
                )
            }
            ExposedDropdownMenu(
                modifier = Modifier.exposedDropdownSize(),
                expanded = dropdownState.value,
                onDismissRequest = { dropdownState.value = false },
                shape = RoundedCornerShape(12.dp),
                containerColor = KarikaColors.White
            ) {
                menu()
            }
        }
    }
}

@Composable
private fun MenuHint(text: String) {
    DropdownMenuItem(
        text = {
            KarikaText(text = text, color = KarikaUiColors.Muted, textSize = 14.sp, fontWeight = FontWeight.W400)
        },
        onClick = {},
        enabled = false
    )
}

@Composable
private fun CustomerTargetDropdown(
    component: CustomerRuleEditorComponent,
    target: MutableState<String>,
    dropdownState: MutableState<Boolean>,
    shops: List<Shop>
) {
    val focus = LocalFocusManager.current
    SearchDropdownField(
        label = component.ruleScope.fieldLabel(),
        icon = vectorResource(Res.drawable.ic_k_user),
        value = target.value,
        placeholder = component.ruleScope.targetFieldPlaceholder(),
        dropdownState = dropdownState,
        onValueChange = { component.onTargetChanged(it) }
    ) {
        if (target.value.length < 2) {
            MenuHint("Pretraži kupce (unesite najmanje 2 znaka).")
        } else if (shops.isEmpty() && target.value.isNotEmpty()) {
            MenuHint("Nema rezultata")
        } else {
            shops.forEachIndexed { index, shop ->
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = shop.name.orEmpty(),
                            color = KarikaUiColors.Ink,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {
                        component.onShopSelected(shop)
                        dropdownState.value = false
                        focus.clearFocus()
                    }
                )
                if (index < shops.size - 1) KDivider()
            }
        }
    }
}

@Composable
private fun ItemOrCategoryDropdown(
    component: CustomerRuleEditorComponent,
    itemOrCategory: MutableState<String>,
    dropdownState: MutableState<Boolean>,
    searchResults: List<SearchItem>
) {
    val focus = LocalFocusManager.current
    SearchDropdownField(
        label = "Artikal ili kategorija",
        icon = vectorResource(Res.drawable.ic_k_tag),
        value = itemOrCategory.value,
        placeholder = "Svi artikli i kategorije",
        dropdownState = dropdownState,
        onValueChange = { component.onItemOrCategoryChanged(it) }
    ) {
        if (itemOrCategory.value.length < 2) {
            MenuHint("Pretraži artikle i kategorije (unesite najmanje 2 znaka).")
        } else if (searchResults.isEmpty() && itemOrCategory.value.isNotEmpty()) {
            MenuHint("Nema rezultata")
        } else {
            searchResults.forEachIndexed { index, item ->
                val isProduct = item is SearchItem.ProductItem
                DropdownMenuItem(
                    leadingIcon = {
                        KIcon(
                            icon = vectorResource(if (isProduct) Res.drawable.ic_k_tag else Res.drawable.ic_k_document),
                            tint = if (isProduct) VendorAccent else KarikaUiColors.Green,
                            size = 18.dp
                        )
                    },
                    text = {
                        KarikaText(
                            text = item.displayName,
                            color = KarikaUiColors.Ink,
                            textSize = 15.sp,
                            fontWeight = FontWeight.W400
                        )
                    },
                    onClick = {
                        component.onItemSelected(item)
                        dropdownState.value = false
                        focus.clearFocus()
                    }
                )
                if (index < searchResults.size - 1) KDivider()
            }
        }
    }
}

@Composable
private fun InfoNote(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KarikaUiColors.Field)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_info), tint = KarikaUiColors.Muted, size = 18.dp)
        KarikaText(
            modifier = Modifier.weight(1f),
            text = text,
            color = KarikaUiColors.Muted,
            textSize = 12.5.sp,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun DeleteButton(onClick: () -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(shape)
            .background(KarikaUiColors.RedSoft)
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        KIcon(icon = vectorResource(Res.drawable.ic_k_trash), tint = KarikaUiColors.Red, size = 18.dp)
        Spacer(Modifier.width(8.dp))
        KarikaText(text = "Obriši pravilo", color = KarikaUiColors.Red, textSize = 15.sp, fontWeight = FontWeight.W700)
    }
}

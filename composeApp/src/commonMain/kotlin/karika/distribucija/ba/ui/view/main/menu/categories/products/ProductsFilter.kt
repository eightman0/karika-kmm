package karika.distribucija.ba.ui.view.main.menu.categories.products

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.RadioGroup
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.util.KarikaConstants
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged


@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun ProductsFilterSheet(
    showState: MutableState<Boolean>,
    component: ProductByCategoryComponent
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val startPrice = component.filterPriceFrom.asState()
    val endPrice = component.filterPriceTo.asState()
    val searchText = mutableStateOf("").asState()
    val vendors by component.vendors.collectAsState()
    val selectedVendor = component.selectedVendor.asState()
    val checkedElements = component.selectedRegion.asState()

    if (showState.value) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 100.dp),
            onDismissRequest = {
                showState.negate()
            },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = KarikaColors.Gray2,
                    width = 60.dp
                )
            }
        ) {
            Column {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = "FILTERI",
                    color = KarikaColors.Gray2,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center
                )
                YSpacer16()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = KarikaColors.Divider
                )
                YSpacer16()
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .hideKeyboard(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "CIJENA",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KarikaTextField2(
                            modifier = Modifier
                                .weight(1f),
                            value = startPrice,
                            placeholder = "OD",
                            allowedChars = KarikaConstants.numbers,
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        )
                        KarikaTextField2(
                            modifier = Modifier
                                .weight(1f),
                            value = endPrice,
                            placeholder = "DO",
                            allowedChars = KarikaConstants.numbers,
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "DOBAVLJAČI",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    SearchBoxBorder(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .height(50.dp),
                        onValueChange = {
                            searchText.value = it
                        },
                        onClose = {
                            searchText.value = ""
                            component.clear()
                        },
                        onSearchExecute = {
                            component.vendors(searchText.value)
                        },
                        placeholder = "Pretraži dobavljače.."
                    )

                    if (vendors.isEmpty() && searchText.value.length > 2) {
                        KarikaText(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            text = "Nema rezultata za unijeti pojam '${searchText.value}'",
                            color = KarikaColors.Primary,
                            textSize = 14.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.W600
                        )
                    } else {
                        RadioGroup(
                            selected = selectedVendor,
                            items = vendors.map { Pair(it.name(), it.entityId) },
                            onChange = {
                                searchText.value = ""
                                component.clear()
                            }
                        )
                    }

                    if (selectedVendor.value.first.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = null, indication = null
                                ) {
                                    selectedVendor.value = Pair("", 0)
                                    component.clear()
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Checkbox(
                                modifier = Modifier
                                    .height(24.dp)
                                    .padding(vertical = 0.dp),
                                checked = selectedVendor.value.first.isNotEmpty(),
                                onCheckedChange = { _ ->
                                    selectedVendor.value = Pair("", 0)
                                    component.clear()
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = KarikaColors.Primary,
                                    uncheckedColor = KarikaColors.Gray7
                                ),
                                enabled = true
                            )
                            KarikaText(
                                text = selectedVendor.value.first,
                                color = KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400,
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "REGIJA",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    component.stateHolder.config.value.customerRegionList.forEach {
                        Row(
                            modifier = Modifier
                                .clickable(
                                    interactionSource = null, indication = null
                                ) {
                                    if (checkedElements.value.contains(it)) {
                                        checkedElements.value =
                                            checkedElements.value.filter { it1 -> it1 != it }
                                    } else {
                                        checkedElements.value =
                                            checkedElements.value.plus(it)
                                    }
                                },
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            Checkbox(
                                modifier = Modifier
                                    .height(24.dp)
                                    .padding(vertical = 0.dp),
                                checked = checkedElements.value.contains(it),
                                onCheckedChange = { _ ->
                                    if (checkedElements.value.contains(it)) {
                                        checkedElements.value =
                                            checkedElements.value.filter { it1 -> it1 != it }
                                    } else {
                                        checkedElements.value =
                                            checkedElements.value.plus(it)
                                    }
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = KarikaColors.Primary,
                                    uncheckedColor = KarikaColors.Gray7
                                ),
                                enabled = true
                            )
                            KarikaText(
                                text = it.label(),
                                color = KarikaColors.Gray2,
                                textSize = 16.sp,
                                fontWeight = FontWeight.W400,
                            )
                        }
                    }
                }
                YSpacer32()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = KarikaColors.Divider
                )
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    SecondaryButton(
                        modifier = Modifier
                            .weight(1f),
                        title = "Zatvori",
                        textSize = 16.sp
                    ) {
                        showState.negate()
                    }
                    PrimaryButtonFilled(
                        modifier = Modifier
                            .weight(1f),
                        title = "Primijeni"
                    ) {
                        showState.negate()
                        component.loadNextPage(reset = true)
                    }
                }
                LaunchedEffect(Unit) {
                    snapshotFlow { searchText.value }
                        .debounce(500)
                        .distinctUntilChanged()
                        .collectLatest { newQuery ->
                            if (newQuery.isNotEmpty()) {
                                if (searchText.value.length > 2) {
                                    component.vendors(searchText.value)
                                }
                            }
                        }
                }
            }
            LoadingView1(component)
        }
    }
}
package karika.distribucija.ba.ui.view.main.vendor.details.filter

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButtonFilled
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.main.vendor.VendorComponent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(component: VendorComponent) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedRegion = remember { mutableStateOf(Pair("", 0)) }
    val showState = component.showFilter.asState()
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
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth()
                        .hideKeyboard(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "REGIJE",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        textAlign = TextAlign.Center
                    )
                    Row(
                        modifier = Modifier
                            .clickable(
                                interactionSource = null, indication = null
                            ) {
                                if (checkedElements.value.size == component.stateHolder.commonHandler.config.value.customerRegionList.size) {
                                    checkedElements.value = listOf()
                                } else {
                                    checkedElements.value = component.stateHolder.commonHandler.config.value.customerRegionList
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        Checkbox(
                            modifier = Modifier
                                .height(24.dp)
                                .padding(vertical = 0.dp),
                            checked = checkedElements.value.size == component.stateHolder.commonHandler.config.value.customerRegionList.size,
                            onCheckedChange = { _ ->
                                if (checkedElements.value.size == component.stateHolder.commonHandler.config.value.customerRegionList.size) {
                                    checkedElements.value = listOf()
                                } else {
                                    checkedElements.value = component.stateHolder.commonHandler.config.value.customerRegionList
                                }
                            },
                            colors = CheckboxDefaults.colors(
                                checkedColor = KarikaColors.Primary,
                                uncheckedColor = KarikaColors.Gray7
                            ),
                            enabled = true
                        )
                        KarikaText(
                            text = "Svi regioni",
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W400,
                        )
                    }
                    component.stateHolder.commonHandler.config.value.customerRegionList.forEach {
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
                        title = "Odustani",
                        textSize = 16.sp
                    ) {
                        showState.negate()
                    }
                    PrimaryButtonFilled(
                        modifier = Modifier
                            .weight(1f),
                        title = "Filtriraj"
                    ) {
                        showState.negate()
                        component.loadNextPage(reset = true)
                    }
                }
            }
        }
    }
}


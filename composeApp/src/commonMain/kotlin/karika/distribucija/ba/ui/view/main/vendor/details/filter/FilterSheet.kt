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
import androidx.compose.runtime.MutableState
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
import karika.distribucija.ba.ui.components.RadioGroup
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.util.KarikaConstants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterSheet(showState: MutableState<Boolean>, callback: (String, String, String) -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedEntity = remember { mutableStateOf(Pair("Svi entiteti", -1)) }
    val checkedElements = remember { mutableStateOf<List<String>>(emptyList()) }

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
                        text = "Entiteti",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700,
                        textAlign = TextAlign.Center
                    )
                    RadioGroup(
                        selected = selectedEntity,
                        items = KarikaConstants.entries
                            .map {
                                if (it.id == -1) {
                                    Pair("Svi entiteti", it.id)
                                } else {
                                    Pair(it.name, it.id)
                                }
                            },
                        onChange = {
                            checkedElements.value = emptyList()
                        }
                    )
                    if (selectedEntity.value.second != -1) {
                        KarikaText(
                            modifier = Modifier
                                .padding(horizontal = 16.dp),
                            text = when (selectedEntity.value.second) {
                                1 -> "KANTON"
                                2 -> "OPĆINA"
                                else -> "OPŠTINA"
                            },
                            color = KarikaColors.Gray2,
                            textSize = 16.sp,
                            fontWeight = FontWeight.W700
                        )
                        val list = mutableListOf(
                            when (selectedEntity.value.second) {
                                1 -> "Svi kantoni"
                                2 -> "Sve općine"
                                else -> "Sve opštine"
                            }
                        ).apply {
                            addAll(KarikaConstants.cantons(selectedEntity.value.first, true))
                        }
                        list.forEach {
                            Row(
                                modifier = Modifier
                                    .clickable(
                                        interactionSource = null, indication = null
                                    ) {
                                        if (listOf(
                                                "Svi kantoni",
                                                "Sve općine",
                                                "Sve opštine"
                                            ).contains(it)
                                        ) {
                                            if (checkedElements.value.contains(it)) {
                                                checkedElements.value = emptyList()
                                            } else {
                                                checkedElements.value = list
                                            }
                                            return@clickable
                                        }

                                        if (checkedElements.value.contains(it)) {
                                            checkedElements.value = checkedElements.value
                                                .filter { f -> f != it }
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
                                        if (listOf(
                                                "Svi kantoni",
                                                "Sve općine",
                                                "Sve opštine"
                                            ).contains(it)
                                        ) {
                                            if (checkedElements.value.contains(it)) {
                                                checkedElements.value = emptyList()
                                            } else {
                                                checkedElements.value = list
                                            }
                                            return@Checkbox
                                        }

                                        if (checkedElements.value.contains(it)) {
                                            checkedElements.value = checkedElements.value
                                                .filter { f -> f != it }
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
                                    text = it,
                                    color = KarikaColors.Gray2,
                                    textSize = 16.sp,
                                    fontWeight = FontWeight.W400,
                                )
                            }
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
                        when {
                            selectedEntity.value.second == -1 -> {
                                callback("", "", "")
                            }
                            selectedEntity.value.second == 1
                                    && (checkedElements.value.isEmpty()
                                    || checkedElements.value.contains("Svi kantoni")) -> {
                                callback(
                                    "b2b_vendor_entitet",
                                    selectedEntity.value.first,
                                    selectedEntity.value.second.toString()
                                )
                                return@PrimaryButtonFilled
                            }
                            selectedEntity.value.second == 1 -> {
                                callback(
                                    "b2b_vendor_kanton",
                                    "",
                                    checkedElements.value.firstOrNull() ?: ""
                                )
                                return@PrimaryButtonFilled
                            }

                            selectedEntity.value.second == 2
                                    && (checkedElements.value.isEmpty()
                                    || checkedElements.value.contains("Sve općine")) -> {
                                callback(
                                    "b2b_vendor_entitet",
                                    selectedEntity.value.first,
                                    selectedEntity.value.second.toString()
                                )
                                return@PrimaryButtonFilled
                            }
                            selectedEntity.value.second == 2 -> {
                                callback(
                                    "b2b_vendor_opcina",
                                    "",
                                    checkedElements.value.firstOrNull() ?: ""
                                )
                                return@PrimaryButtonFilled
                            }

                            selectedEntity.value.second == 3
                                    && (checkedElements.value.isEmpty()
                                    || checkedElements.value.contains("Sve opštine")) -> {
                                callback(
                                    "b2b_vendor_entitet",
                                    selectedEntity.value.first,
                                    selectedEntity.value.second.toString()
                                )
                                return@PrimaryButtonFilled
                            }
                            selectedEntity.value.second == 2 -> {
                                callback(
                                    "b2b_vendor_grad",
                                    "",
                                    checkedElements.value.firstOrNull() ?: ""
                                )
                                return@PrimaryButtonFilled
                            }
                        }
                    }
                }
            }
        }
    }
}


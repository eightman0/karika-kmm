package karika.distribucija.ba.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
fun RadioGroup(
    items: List<Pair<String, Int>>,
    selected: MutableState<Pair<String, Int>>,
    enabled: Boolean = true,
    onChange: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items.forEach {
            Row(
                modifier = Modifier
                    .onClick {
                        selected.value = it
                        onChange.invoke()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    enabled = enabled,
                    selected = selected.value.first == it.first,
                    onClick = {
                        selected.value = it
                        onChange.invoke()
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = KarikaColors.Primary,
                        unselectedColor = KarikaColors.Gray8
                    )
                )
                KarikaText(
                    modifier = Modifier,
                    text = it.first,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }

        }
    }
}

@Composable
fun RadioGroupSecondary(
    items: List<Pair<String, Int>>,
    selected: MutableState<Pair<String, Int>>,
    enabled: Boolean = true,
    onChange: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        items.forEach {
            Row(
                modifier = Modifier
                    .onClick {
                        selected.value = it
                        onChange.invoke()
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RadioButton(
                    enabled = enabled,
                    selected = selected.value.first == it.first,
                    onClick = {
                        selected.value = it
                        onChange.invoke()
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = KarikaColors.Blue,
                        unselectedColor = KarikaColors.Gray8
                    )
                )
                KarikaText(
                    modifier = Modifier,
                    text = it.first,
                    color = KarikaColors.Gray2,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600
                )
            }

        }
    }
}

@Composable
fun KarikaRadioButton(
    title: String = "",
    selected: Boolean,
    onChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = Modifier
            .onClick {
                onChange.invoke(selected)
            },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        RadioButton(
            enabled = true,
            selected = selected,
            onClick = {
                onChange.invoke(selected)
            },
            colors = RadioButtonDefaults.colors(
                selectedColor = KarikaColors.Primary,
                unselectedColor = KarikaColors.Gray8
            )
        )
        KarikaText(
            modifier = Modifier,
            text = title,
            color = KarikaColors.Gray2,
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}
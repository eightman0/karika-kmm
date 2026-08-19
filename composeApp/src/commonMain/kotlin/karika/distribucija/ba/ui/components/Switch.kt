package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun KarikaSwitch(
    title: String,
    checked: MutableState<Boolean>
) {
    Row(
        modifier = Modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(28.dp)
                .background(
                    color = if (checked.value) KarikaColors.Blue else KarikaColors.Error,
                    shape = RoundedCornerShape(100)
                ),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked = checked.value,
                onCheckedChange = {
                    checked.value = it
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = KarikaColors.Blue,
                    uncheckedTrackColor = KarikaColors.Gray23,
                    checkedThumbColor = KarikaColors.White,
                    uncheckedThumbColor = KarikaColors.White,
                    checkedBorderColor = KarikaColors.Blue,
                    uncheckedBorderColor = KarikaColors.Gray23
                ),
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
            )
        }
        KarikaText(
            text = title,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
    }
}

@Composable
fun KarikaSwitch1(
    modifier: Modifier = Modifier,
    title: String,
    checked: MutableState<Boolean>,
    onCheckedChange: (Boolean) -> Unit = {}
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        KarikaText(
            text = title,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
        Box(
            modifier = Modifier
                .width(44.dp)
                .height(28.dp)
                .background(
                    color = if (checked.value) KarikaColors.Blue else KarikaColors.Error,
                    shape = RoundedCornerShape(100)
                ),
            contentAlignment = Alignment.Center
        ) {
            Switch(
                checked = checked.value,
                onCheckedChange = {
                    checked.value = it
                    onCheckedChange(it)
                },
                colors = SwitchDefaults.colors(
                    checkedTrackColor = KarikaColors.Blue,
                    uncheckedTrackColor = KarikaColors.Error,
                    checkedThumbColor = KarikaColors.White,
                    uncheckedThumbColor = KarikaColors.White,
                    checkedBorderColor = KarikaColors.Blue,
                    uncheckedBorderColor = KarikaColors.Error
                ),
                modifier = Modifier
                    .width(40.dp)
                    .height(20.dp)
            )
        }
    }
}

@Composable
fun KarikaCheckbox(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    title: String,
    value: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(value) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            modifier = Modifier
                .size(16.dp),
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            },
            colors = CheckboxDefaults.colors(
                uncheckedColor = KarikaColors.Gray4,
                checkedColor = KarikaColors.Primary,
                checkmarkColor = KarikaColors.White
            )
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    checked = !checked
                    onCheckedChange.invoke(checked.not())
                },
            text = title,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
    }
}

@Composable
fun KarikaCheckboxSecondary(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    title: String,
    value: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(value) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            modifier = Modifier
                .size(16.dp),
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            },
            colors = CheckboxDefaults.colors(
                uncheckedColor = KarikaColors.Gray4,
                checkedColor = KarikaColors.Blue,
                checkmarkColor = KarikaColors.White
            )
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    checked = !checked
                    onCheckedChange.invoke(checked.not())
                },
            text = title,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
    }
}

@Composable
fun KarikaCheckbox(
    atitle: AnnotatedString,
    value: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(value) }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            modifier = Modifier
                .size(16.dp),
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            },
            colors = CheckboxDefaults.colors(
                uncheckedColor = KarikaColors.Gray4,
                checkedColor = KarikaColors.Primary,
                checkmarkColor = KarikaColors.White
            )
        )
        KarikaText(
            atext = atitle,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
    }
}

@Composable
fun KarikaCheckboxSecondary(
    atitle: AnnotatedString,
    value: Boolean = false,
    onCheckedChange: (Boolean) -> Unit
) {
    var checked by remember { mutableStateOf(value) }
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Checkbox(
            modifier = Modifier
                .size(16.dp),
            checked = checked,
            onCheckedChange = {
                checked = it
                onCheckedChange.invoke(it)
            },
            colors = CheckboxDefaults.colors(
                uncheckedColor = KarikaColors.Gray4,
                checkedColor = KarikaColors.Blue,
                checkmarkColor = KarikaColors.White
            )
        )
        KarikaText(
            atext = atitle,
            color = KarikaColors.Gray4,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
    }
}
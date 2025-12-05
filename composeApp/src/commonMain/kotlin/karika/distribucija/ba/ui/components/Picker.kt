package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import org.jetbrains.compose.resources.vectorResource


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarikaPicker(
    title: String = "",
    placeholder: String = "",
    values: MutableState<List<String>>,
    value: MutableState<String>,
    onChange: (String) -> Unit = {}
) {
    if (values.value.isEmpty()) {
        return
    }
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = Modifier,
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                text = title,
                color = KarikaColors.Gray4,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
            Row(
                modifier = Modifier
                    .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                    .border(
                        width = 1.dp,
                        color = KarikaColors.Border,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .background(
                        color = KarikaColors.White,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp),
                    text = value.value.ifEmpty { placeholder },
                    color = if (value.value.isEmpty()) KarikaColors.Placeholder else KarikaColors.Black,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W400,
                )
                Icon(
                    modifier = Modifier
                        .padding(16.dp),
                    imageVector = vectorResource(Res.drawable.ic_arrow_down),
                    contentDescription = "",
                    tint = KarikaColors.Black
                )
            }
        }
        ExposedDropdownMenu(
            modifier = Modifier,
            shape = RoundedCornerShape(8.dp),
            containerColor = KarikaColors.White,
            expanded = expanded.value,
            onDismissRequest = {
                expanded.negate()
            },
        ) {
            values.value.forEachIndexed { index, it ->
                DropdownMenuItem(
                    modifier = Modifier,
                    onClick = {
                        value.value = it
                        expanded.negate()
                        onChange(it)
                    },
                    text = {
                        KarikaText(
                            text = it,
                            modifier = Modifier,
                            fontWeight = if (it == value.value) FontWeight.W500 else FontWeight.W400,
                            textSize = 14.sp,
                            color = if (it == value.value) KarikaColors.Black else KarikaColors.Gray4,
                        )
                    },
                )
                if (index < values.value.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarikaPickerSmall1(
    modifier: Modifier,
    values: MutableState<List<Pair<String, Color>>>,
    value: MutableState<String>,
    padding: Dp = 16.dp,
    borderColor: Color = KarikaColors.Border,
    onChange: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it }
    ) {
        Row(
            modifier = modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(4.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(padding),
                text = value.value,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W400,
            )
            Icon(
                modifier = Modifier
                    .padding(padding),
                imageVector = vectorResource(Res.drawable.ic_arrow_down),
                contentDescription = "",
                tint = KarikaColors.Gray2
            )
        }
        ExposedDropdownMenu(
            modifier = Modifier,
            shape = RoundedCornerShape(8.dp),
            containerColor = KarikaColors.White,
            expanded = expanded.value,
            onDismissRequest = {
                expanded.negate()
            },
        ) {
            values.value.forEachIndexed { index, it ->
                DropdownMenuItem(
                    modifier = Modifier,
                    onClick = {
                        value.value = it.first
                        expanded.negate()
                        onChange.invoke()
                    },
                    text = {
                        Box(
                            modifier = Modifier
                                .background(
                                    color = it.second.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(4.dp)
                                )
                        ) {
                            KarikaText(
                                text = it.first,
                                modifier = Modifier
                                    .padding(4.dp),
                                fontWeight = FontWeight.W700,
                                textSize = 14.sp,
                                color = it.second
                            )
                        }
                    },
                    leadingIcon = {
                        KarikaRadioButton(
                            selected = it.first == value.value
                        ) { _ ->
                            value.value = it.first
                            expanded.negate()
                            onChange.invoke()
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarikaPickerSmall(
    modifier: Modifier,
    values: MutableState<List<String>>,
    value: MutableState<String>,
    padding: Dp = 16.dp,
    borderColor: Color = KarikaColors.Border,
    onChange: () -> Unit
) {
    val expanded = remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        modifier = modifier,
        expanded = expanded.value,
        onExpandedChange = { expanded.value = it }
    ) {
        Row(
            modifier = modifier
                .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                .border(
                    width = 1.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(4.dp)
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(padding),
                text = value.value,
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W400,
            )
            Icon(
                modifier = Modifier
                    .padding(padding),
                imageVector = vectorResource(Res.drawable.ic_arrow_down),
                contentDescription = "",
                tint = KarikaColors.Gray2
            )
        }
        ExposedDropdownMenu(
            modifier = Modifier,
            shape = RoundedCornerShape(8.dp),
            containerColor = KarikaColors.White,
            expanded = expanded.value,
            onDismissRequest = {
                expanded.negate()
            },
        ) {
            values.value.forEachIndexed { index, it ->
                DropdownMenuItem(
                    modifier = Modifier,
                    onClick = {
                        value.value = it
                        expanded.negate()
                        onChange.invoke()
                    },
                    text = {
                        KarikaText(
                            text = it,
                            modifier = Modifier,
                            fontWeight = if (it == value.value) FontWeight.W500 else FontWeight.W400,
                            textSize = 14.sp,
                            color = if (it == value.value) KarikaColors.Black else KarikaColors.Gray4,
                        )
                    },
                    leadingIcon = {
                        KarikaRadioButton(
                            selected = it == value.value
                        ) { _ ->
                            value.value = it
                            expanded.negate()
                            onChange.invoke()
                        }
                    }
                )
            }
        }
    }
}
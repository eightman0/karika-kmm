package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.gotham_bold
import karikav2.composeapp.generated.resources.gotham_book
import karikav2.composeapp.generated.resources.gotham_light
import karikav2.composeapp.generated.resources.gotham_medium
import karikav2.composeapp.generated.resources.gotham_thin
import karikav2.composeapp.generated.resources.ic_eye
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.vectorResource

@Composable
fun KarikaText(
    text: String? = "",
    modifier: Modifier = Modifier,
    textSize: TextUnit = 14.sp,
    color: Color = KarikaColors.White,
    textAlign: TextAlign = TextAlign.Start,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = textSize,
    fontStyle: FontStyle = FontStyle.Normal,
    decoration: TextDecoration = TextDecoration.None
) {
    val safeText = text.orEmpty()
    if (safeText.isEmpty()) {
        return
    }
    Text(
        modifier = modifier,
        text = safeText,
        fontSize = textSize,
        color = color,
        textAlign = textAlign,
        overflow = textOverflow,
        maxLines = maxLines,
        lineHeight = lineHeight,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = karikaFonts(),
        style = LocalTextStyle.current.copy(
            textDecoration = decoration
        )
    )
}

@Composable
fun KarikaText(
    atext: AnnotatedString,
    modifier: Modifier = Modifier,
    textSize: TextUnit = 14.sp,
    color: Color = KarikaColors.White,
    textAlign: TextAlign = TextAlign.Start,
    textOverflow: TextOverflow = TextOverflow.Ellipsis,
    fontWeight: FontWeight = FontWeight.Normal,
    maxLines: Int = Int.MAX_VALUE,
    lineHeight: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle = FontStyle.Normal,
    decoration: TextDecoration = TextDecoration.None,
) {
    Text(
        modifier = modifier,
        text = atext,
        fontSize = textSize,
        color = color,
        textAlign = textAlign,
        overflow = textOverflow,
        fontWeight = fontWeight,
        maxLines = maxLines,
        lineHeight = lineHeight,
        fontStyle = fontStyle,
        style = LocalTextStyle.current.copy(
            textDecoration = decoration
        ),
        fontFamily = karikaFonts()
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarikaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    textStyle: TextStyle = LocalTextStyle.current,
    label: @Composable (() -> Unit)? = null,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null,
    supportingText: @Composable (() -> Unit)? = null,
    isError: Boolean = false,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    singleLine: Boolean = false,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    minLines: Int = 1,
    interactionSource: MutableInteractionSource? = null,
    shape: Shape = TextFieldDefaults.shape,
    colors: TextFieldColors = TextFieldDefaults.colors(),
    contentPadding: PaddingValues = PaddingValues(8.dp)
) {
    @Suppress("NAME_SHADOWING")
    val interactionSource = interactionSource ?: remember { MutableInteractionSource() }
    val mergedTextStyle = textStyle.merge(TextStyle(color = textStyle.color))
    CompositionLocalProvider(LocalTextSelectionColors provides colors.textSelectionColors) {
        BasicTextField(
            value = value,
            modifier = modifier,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = mergedTextStyle,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            interactionSource = interactionSource,
            singleLine = singleLine,
            maxLines = maxLines,
            minLines = minLines,
            decorationBox =
                @Composable { innerTextField ->
                    // places leading icon, text field with label and placeholder, trailing icon
                    TextFieldDefaults.DecorationBox(
                        contentPadding = contentPadding,
                        value = value,
                        visualTransformation = visualTransformation,
                        innerTextField = innerTextField,
                        placeholder = placeholder,
                        label = label,
                        leadingIcon = leadingIcon,
                        trailingIcon = trailingIcon,
                        prefix = prefix,
                        suffix = suffix,
                        supportingText = supportingText,
                        shape = shape,
                        singleLine = singleLine,
                        enabled = enabled,
                        isError = isError,
                        interactionSource = interactionSource,
                        colors = colors,
                    )
                }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KarikaIntTextField(
    value: MutableState<Int>,
    onValueChange: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    minValue: Int = 1
) {
    val imeVisible = rememberImeVisible()
    var textFieldValue by remember(value) {
        mutableStateOf(
            TextFieldValue(
                text = value.value.toString(),
                selection = TextRange(value.value.toString().length)
            )
        )
    }

    CompositionLocalProvider {
        BasicTextField(
            value = textFieldValue,
            modifier = modifier,
            onValueChange = { newValue ->
                val newText = newValue.text
                val filtered = newText.filter { it.isDigit() }
                val withoutLeadingZero = if (filtered.startsWith("0") && filtered.length > 1) {
                    filtered.trimStart('0')
                } else if (filtered == "0") {
                    ""
                } else {
                    filtered
                }

                val parsedValue = withoutLeadingZero.toIntOrNull()

                // Dozvoljavamo unos bilo kojeg broja tokom kucanja
                textFieldValue = TextFieldValue(
                    text = withoutLeadingZero,
                    selection = TextRange(withoutLeadingZero.length)
                )

                // Obavijestimo parent o promjeni
                if (minValue <= (parsedValue ?: return@BasicTextField)) {
                    onValueChange(parsedValue)
                }
            },
            enabled = true,
            readOnly = false,
            textStyle = LocalTextStyle.current.copy(
                color = KarikaColors.Gray2,
                fontSize = 24.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp,
                fontFamily = karikaFonts()
            ),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            singleLine = true,
            maxLines = 1,
            minLines = 1,
            decorationBox = @Composable { innerTextField ->
                TextFieldDefaults.DecorationBox(
                    contentPadding = PaddingValues(8.dp),
                    value = textFieldValue.text,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    singleLine = true,
                    enabled = true,
                    isError = false,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )
            }
        )
    }

    LaunchedEffect(imeVisible) {
        if (!imeVisible) {
            val currentValue = textFieldValue.text.toIntOrNull()
            val finalValue = when {
                currentValue == null || currentValue < minValue -> minValue
                else -> currentValue
            }

            textFieldValue = TextFieldValue(
                text = finalValue.toString(),
                selection = TextRange(finalValue.toString().length)
            )
            onValueChange(finalValue)
        }
    }

    LaunchedEffect(value.value) {
        val valueString = value.value.toString()
        if (valueString != textFieldValue.text) {
            textFieldValue = TextFieldValue(
                text = valueString,
                selection = TextRange(valueString.length)
            )
        }
    }
}

@Composable
fun KarikaTextField1(
    modifier: Modifier = Modifier,
    title: String = "",
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    disabledTextColor: Color = KarikaColors.Gray21,
    textColor: Color = KarikaColors.Black,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcons: @Composable (() -> Unit)? = null,
    allowedChars: List<String> = emptyList(),
    error: MutableState<String> = mutableStateOf(""),
    leadingZero: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (title.isNotEmpty()) {
            KarikaText(
                text = title,
                color = KarikaColors.Gray4,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
        TextField(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = if (error.value.isNotEmpty()) KarikaColors.Error else KarikaColors.Border,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(4.dp)
                ),
            placeholder = {
                KarikaText(
                    text = placeholder,
                    color = placeholderColor,
                    textSize = placeholderSize,
                    fontWeight = FontWeight.W400
                )
            },
            enabled = enabled,
            value = value.value,
            onValueChange = {
                if (!leadingZero && it.startsWith("0")) {
                    return@TextField
                }
                if (it.startsWith(" ")) {
                    return@TextField
                }
                if (allowedChars.isNotEmpty() && it.any { f -> !allowedChars.contains(f.toString()) }) {
                    return@TextField
                }
                if (it.length > maxLength) {
                    return@TextField
                }
                value.value = it
                onValueChange.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = keyboardType,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (doneAction != null) {
                        if (value.value.isEmpty()) {
                            keyboardController?.hide()
                        }
                        doneAction.invoke()
                    } else {
                        keyboardController?.hide()
                    }
                },
                onSearch = {
                    if (doneAction != null) {
                        if (value.value.isEmpty()) {
                            keyboardController?.hide()
                        }
                        doneAction.invoke()
                    } else {
                        keyboardController?.hide()
                    }
                }
            ),
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedTextColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledTextColor = disabledTextColor
            ),
            textStyle = LocalTextStyle.current.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontSize = textSize,
                fontFamily = karikaFonts()
            ),
            trailingIcon = {
                trailingIcons?.invoke()
            }
        )
        KarikaText(
            text = error.value,
            color = KarikaColors.Error,
            textSize = 12.sp,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
fun KarikaTextField2(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    textColor: Color = KarikaColors.Black,
    disabledTextColor: Color = KarikaColors.Gray22,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcons: @Composable (() -> Unit)? = null,
    allowedChars: List<String> = emptyList(),
    error: MutableState<String> = mutableStateOf("")
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (error.value.isNotEmpty()) KarikaColors.Error else KarikaColors.Border,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(4.dp)
            ),
        placeholder = {
            KarikaText(
                text = placeholder,
                color = placeholderColor,
                textSize = placeholderSize,
                fontWeight = FontWeight.W400
            )
        },
        enabled = enabled,
        value = value.value,
        onValueChange = {
            if (it.startsWith(" ")) {
                return@TextField
            }
            if (allowedChars.isNotEmpty() && it.any { f -> !allowedChars.contains(f.toString()) }) {
                return@TextField
            }
            if (it.length > maxLength) {
                return@TextField
            }
            value.value = it
            onValueChange.invoke(it)
        },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (doneAction != null) {
                    if (value.value.isEmpty()) {
                        keyboardController?.hide()
                    }
                    doneAction.invoke()
                } else {
                    keyboardController?.hide()
                }
            }
        ),
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            focusedTextColor = textColor,
            disabledTextColor = disabledTextColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSize = textSize,
            fontFamily = karikaFonts()
        ),
        trailingIcon = {
            trailingIcons?.invoke()
        }
    )
}

@Composable
fun KarikaTextFieldWithoutBorder(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    textColor: Color = KarikaColors.Black,
    disabledTextColor: Color = KarikaColors.Gray22,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcons: @Composable (() -> Unit)? = null,
    allowedChars: List<String> = emptyList(),
    error: MutableState<String> = mutableStateOf("")
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        modifier = modifier
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(4.dp)
            ),
        placeholder = {
            KarikaText(
                text = placeholder,
                color = placeholderColor,
                textSize = placeholderSize,
                fontWeight = FontWeight.W400
            )
        },
        enabled = enabled,
        value = value.value,
        onValueChange = {
            if (it.startsWith(" ")) {
                return@TextField
            }
            if (allowedChars.isNotEmpty() && it.any { f -> !allowedChars.contains(f.toString()) }) {
                return@TextField
            }
            if (it.length > maxLength) {
                return@TextField
            }
            value.value = it
            onValueChange.invoke(it)
        },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = keyboardType,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (doneAction != null) {
                    if (value.value.isEmpty()) {
                        keyboardController?.hide()
                    }
                    doneAction.invoke()
                } else {
                    keyboardController?.hide()
                }
            }
        ),
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            focusedTextColor = textColor,
            disabledTextColor = disabledTextColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSize = textSize,
            fontFamily = karikaFonts()
        ),
        trailingIcon = {
            trailingIcons?.invoke()
        }
    )
}

@Composable
fun KarikaTextField4(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    textColor: Color = KarikaColors.Black,
    disabledTextColor: Color = KarikaColors.Gray22,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingIcons: @Composable (() -> Unit)? = null,
    allowedChars: List<String> = emptyList(),
    error: MutableState<String> = mutableStateOf(""),
    leadingZero: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        TextField(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = if (error.value.isNotEmpty()) KarikaColors.Error else KarikaColors.Border,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(4.dp)
                ),
            placeholder = {
                KarikaText(
                    text = placeholder,
                    color = placeholderColor,
                    textSize = placeholderSize,
                    fontWeight = FontWeight.W400
                )
            },
            enabled = enabled,
            value = value.value,
            onValueChange = {
                if (!leadingZero && it.startsWith("0") && it.length > 1) {
                    return@TextField
                }
                if (it.startsWith(" ")) {
                    return@TextField
                }
                if (allowedChars.isNotEmpty() && it.any { f -> !allowedChars.contains(f.toString()) }) {
                    return@TextField
                }
                if (it.length > maxLength) {
                    return@TextField
                }
                value.value = it
                onValueChange.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = keyboardType,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    if (doneAction != null) {
                        if (value.value.isEmpty()) {
                            keyboardController?.hide()
                        }
                        doneAction.invoke()
                    } else {
                        keyboardController?.hide()
                    }
                }
            ),
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedTextColor = textColor,
                disabledTextColor = disabledTextColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontSize = textSize,
                fontFamily = karikaFonts()
            ),
            trailingIcon = {
                trailingIcons?.invoke()
            }
        )
        KarikaText(
            modifier = modifier,
            text = error.value,
            color = KarikaColors.Error,
            textSize = 12.sp,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
fun KarikaAmountField(
    modifier: Modifier = Modifier,
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    textColor: Color = KarikaColors.Black,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    trailingIcons: @Composable (() -> Unit)? = null,
    error: MutableState<String> = mutableStateOf("")
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val regex = remember { Regex("^(0|[1-9]\\d*)([.]\\d{0,2})?$") }

    TextField(
        modifier = modifier
            .border(
                width = 1.dp,
                color = if (error.value.isNotEmpty()) KarikaColors.Error else KarikaColors.Border,
                shape = RoundedCornerShape(4.dp)
            )
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(4.dp)
            ),
        placeholder = {
            KarikaText(
                text = placeholder,
                color = placeholderColor,
                textSize = placeholderSize,
                fontWeight = FontWeight.W400
            )
        },
        enabled = enabled,
        value = value.value,
        onValueChange = {
            val newValue = it.replace(',', '.')

            if (newValue == "0") {
                value.value = newValue
                return@TextField
            }

            if (newValue.startsWith("0") && !newValue.startsWith("0.")) {
                return@TextField
            }

            if (newValue.isEmpty() || regex.matches(newValue)) {
                value.value = newValue
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = imeAction,
            keyboardType = KeyboardType.Decimal,
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (doneAction != null) {
                    if (value.value.isEmpty()) {
                        keyboardController?.hide()
                    }
                    doneAction.invoke()
                } else {
                    keyboardController?.hide()
                }
            }
        ),
        maxLines = maxLines,
        colors = TextFieldDefaults.colors(
            focusedTextColor = textColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            fontWeight = fontWeight,
            fontStyle = fontStyle,
            fontSize = textSize,
            fontFamily = karikaFonts()
        ),
        trailingIcon = {
            trailingIcons?.invoke()
        }
    )
}

@Composable
fun KarikaTextField3(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    KarikaTextField(
        modifier = modifier
            .focusable()
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(20.dp)
            ),
        value = value,
        onValueChange = onValueChange,
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Done,
            keyboardType = KeyboardType.Number,
        ),
        maxLines = 1,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            focusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent
        ),
        textStyle = LocalTextStyle.current.copy(
            color = KarikaColors.Gray2,
            fontSize = 24.sp,
            fontWeight = FontWeight.W600,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                focusManager.clearFocus()
                keyboardController?.hide()
            }
        ),
        contentPadding = PaddingValues(top = 6.dp)
    )
}

@Composable
fun KarikaPasswordTextField(
    modifier: Modifier = Modifier,
    title: String = "",
    value: MutableState<String> = mutableStateOf(""),
    placeholder: String = "",
    placeholderColor: Color = KarikaColors.Placeholder,
    placeholderSize: TextUnit = 14.sp,
    textColor: Color = KarikaColors.Black,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.Normal,
    fontStyle: FontStyle = FontStyle.Normal,
    maxLines: Int = Int.MAX_VALUE,
    maxLength: Int = Int.MAX_VALUE,
    onValueChange: (String) -> Unit = {},
    enabled: Boolean = true,
    imeAction: ImeAction = ImeAction.Done,
    doneAction: (() -> Unit)? = null,
    error: MutableState<String> = mutableStateOf("")
) {
    var passwordVisibility by remember { mutableStateOf(false) }
    val text = remember { value }
    val keyboardController = LocalSoftwareKeyboardController.current
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KarikaText(
            text = title,
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        TextField(
            modifier = modifier
                .border(
                    width = 1.dp,
                    color = if (error.value.isNotEmpty()) KarikaColors.Error else KarikaColors.Border,
                    shape = RoundedCornerShape(4.dp)
                )
                .background(
                    color = KarikaColors.White,
                    shape = RoundedCornerShape(4.dp)
                ),
            placeholder = {
                KarikaText(
                    text = placeholder,
                    color = placeholderColor,
                    textSize = placeholderSize,
                    fontWeight = FontWeight.W400
                )
            },
            enabled = enabled,
            value = text.value,
            onValueChange = {
                if (it.length > maxLength) {
                    return@TextField
                }
                text.value = it
                onValueChange.invoke(it)
            },
            keyboardOptions = KeyboardOptions(
                imeAction = imeAction,
                keyboardType = KeyboardType.Text,
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                    doneAction?.invoke()
                }
            ),
            maxLines = maxLines,
            colors = TextFieldDefaults.colors(
                focusedTextColor = textColor,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent
            ),
            textStyle = LocalTextStyle.current.copy(
                fontWeight = fontWeight,
                fontStyle = fontStyle,
                fontSize = textSize,
                fontFamily = karikaFonts()
            ),
            trailingIcon = {
                Icon(
                    modifier = Modifier
                        .clickable {
                            passwordVisibility = !passwordVisibility
                        },
                    imageVector = vectorResource(if (passwordVisibility) Res.drawable.ic_eye else Res.drawable.ic_eye),
                    contentDescription = ""
                )
            },
            visualTransformation = if (passwordVisibility) VisualTransformation.None else PasswordVisualTransformation(),
        )
        KarikaText(
            text = error.value,
            color = KarikaColors.Error,
            textSize = 12.sp,
            fontWeight = FontWeight.W400
        )
    }
}

@Composable
fun IconTextItem(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    icon: ImageVector,
    iconColor: Color = KarikaColors.Secondary,
    iconSize: Dp = 24.dp,
    text: String?,
    textColor: Color = KarikaColors.Secondary,
    fontWeight: FontWeight = FontWeight.W500,
    textSize: TextUnit = 16.sp,
    textAlign: TextAlign = TextAlign.Center,
    iconPosition: FabPosition = FabPosition.Start,
    badge: Int = 0
) {
    if (!text.isNullOrEmpty()) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (iconPosition == FabPosition.Start) {
                Box(
                    modifier = Modifier
                        .size(iconSize + if (badge > 0) 10.dp else 0.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        modifier = Modifier
                            .size(iconSize),
                        imageVector = icon,
                        tint = iconColor,
                        contentDescription = ""
                    )
                    if (badge > 0) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(color = KarikaColors.Red, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                KarikaText(
                                    modifier = Modifier
                                        .padding(0.dp),
                                    text = "$badge",
                                    textSize = 10.sp,
                                    fontWeight = FontWeight.W400,
                                    color = KarikaColors.White
                                )
                            }
                        }
                    }
                }
                XSpacer8()
            }
            Box(
                modifier = Modifier
            ) {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 6.dp),
                    text = text,
                    textSize = textSize,
                    lineHeight = textSize,
                    textAlign = textAlign,
                    fontWeight = fontWeight,
                    color = textColor
                )
            }
            if (iconPosition == FabPosition.End) {
                XSpacer8()
                Icon(
                    modifier = Modifier
                        .size(iconSize),
                    imageVector = icon,
                    tint = iconColor,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun EndIconTextItem(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    icon: ImageVector?,
    iconColor: Color = KarikaColors.Secondary,
    iconSize: Dp = 24.dp,
    text: String?,
    textColor: Color = KarikaColors.Secondary,
    fontWeight: FontWeight = FontWeight.W500,
    textSize: TextUnit = 16.sp,
) {
    if (!text.isNullOrEmpty()) {
        Row(
            modifier = modifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 6.dp),
                text = text,
                textSize = textSize,
                lineHeight = textSize,
                fontWeight = fontWeight,
                color = textColor
            )
            if (icon != null) {
                Icon(
                    modifier = Modifier
                        .size(iconSize),
                    imageVector = icon,
                    tint = iconColor,
                    contentDescription = ""
                )
            }
        }
    }
}

@Composable
fun karikaFonts() = FontFamily(
    Font(Res.font.gotham_book, FontWeight.Normal),
    Font(Res.font.gotham_bold, FontWeight.Bold),
    Font(Res.font.gotham_thin, FontWeight.Thin),
    Font(Res.font.gotham_light, FontWeight.Light),
    Font(Res.font.gotham_medium, FontWeight.Medium)
)
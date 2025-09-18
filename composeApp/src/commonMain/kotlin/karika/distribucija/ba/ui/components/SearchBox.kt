package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_search
import karikav2.composeapp.generated.resources.ic_tertiary
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.jetbrains.compose.resources.vectorResource

@OptIn(FlowPreview::class)
@Composable
fun SearchBox(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit,
    onClose: (String) -> Unit = {},
    onSearchExecute: (String) -> Unit,
    focusRequester: FocusRequester = FocusRequester(),
    searchText: MutableState<String> = mutableStateOf(""),
    enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    KarikaTextField(
        modifier = modifier
            .height(40.dp)
            .focusable()
            .focusRequester(focusRequester)
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(20.dp)
            ),
        placeholder = {
            KarikaText(
                modifier = Modifier,
                text = "Pretraži proizvode...",
                color = KarikaColors.Gray1,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
        },
        value = searchText.value,
        onValueChange = {
            searchText.value = it
            onValueChange.invoke(it)
            if (it.isEmpty()) {
                onClose.invoke("")
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchExecute(searchText.value)
                keyboardController?.hide()
            },
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
        trailingIcon = {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_search),
                null,
                tint = KarikaColors.Secondary
            )
        },
        textStyle = LocalTextStyle.current.copy(
            color = KarikaColors.Secondary,
            fontSize = 14.sp,
            fontWeight = FontWeight.W400
        ),
        enabled = enabled,
    )

    LaunchedEffect(Unit) {
        snapshotFlow { searchText.value }
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { newQuery ->
                if (newQuery.isNotEmpty()) {
                    onSearchExecute.invoke(newQuery)
                }
            }
    }
}

@OptIn(FlowPreview::class)
@Composable
fun SearchBoxBorder(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit,
    onClose: (String) -> Unit = {},
    onSearchExecute: (String) -> Unit,
    placeholder: String = "Pretraži proizvode..",
    focusRequester: FocusRequester = FocusRequester(),
    borderShape: Dp = 12.dp,
    preselected: String = ""
) {
    var searchText by remember { mutableStateOf(preselected) }
    val keyboardController = LocalSoftwareKeyboardController.current
    TextField(
        modifier = modifier
            .focusable()
            .focusRequester(focusRequester)
            .border(
                width = 1.dp,
                color = KarikaColors.Divider,
                shape = RoundedCornerShape(borderShape)
            )
            .background(
                color = KarikaColors.White,
                shape = RoundedCornerShape(borderShape)
            ),
        placeholder = {
            KarikaText(
                text = placeholder,
                color = KarikaColors.Placeholder
            )
        },
        value = searchText,
        onValueChange = {
            searchText = it
            onValueChange.invoke(it)
            if (it.isEmpty()) {
                onClose.invoke("")
            }
        },
        keyboardOptions = KeyboardOptions(
            imeAction = ImeAction.Search,
            keyboardType = KeyboardType.Text,
        ),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearchExecute(searchText)
                keyboardController?.hide()
            },
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
        leadingIcon = {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_search),
                null,
                tint = KarikaColors.Gray2
            )
        },
        trailingIcon = {
            if (searchText.isNotEmpty()) {
                Icon(
                    imageVector = vectorResource(Res.drawable.ic_tertiary),
                    null,
                    tint = KarikaColors.Gray2,
                    modifier = Modifier
                        .onClick {
                            searchText = ""
                            onClose.invoke("")
                            keyboardController?.hide()
                        }
                )
            }
        }
    )

    LaunchedEffect(Unit) {
        snapshotFlow { searchText }
            .debounce(500)
            .distinctUntilChanged()
            .collectLatest { newQuery ->
                if (newQuery.isNotEmpty()) {
                    onSearchExecute.invoke(newQuery)
                }
            }
    }
}
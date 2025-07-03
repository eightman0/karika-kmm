package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_search
import org.jetbrains.compose.resources.vectorResource

@Composable
fun SearchBox(
    modifier: Modifier = Modifier.fillMaxWidth(),
    onValueChange: (String) -> Unit,
    onClose: (String) -> Unit = {},
    onSearchExecute: (String) -> Unit,
    focusRequester: FocusRequester = FocusRequester()
) {
    var searchText by remember { mutableStateOf("") }
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
                text = "Pretraži proidzvode...",
                color = KarikaColors.Gray1,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
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
        trailingIcon = {
            Icon(
                imageVector = vectorResource(Res.drawable.ic_search),
                null,
                tint = KarikaColors.Secondary
            )
        },
        textStyle = LocalTextStyle.current.copy(
            color = KarikaColors.Secondary,
            fontSize = 14.sp
        )
    )

    /* LaunchedEffect(Unit) {
         snapshotFlow { searchText }
             .debounce(500)
             .distinctUntilChanged()
             .collectLatest { newQuery ->
                 if (newQuery.isNotEmpty()) {
                     onSearchExecute.invoke(newQuery)
                 }
             }
     }*/
}
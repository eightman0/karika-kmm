package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors

@Composable
fun RejectOrderModal(
    onSubmit: (String) -> Unit,
    onCancel: () -> Unit
) {
    val reason = remember { mutableStateOf("") }
    OrderModal(title = "Odbij narudžbu", titleColor = KarikaUiColors.Red, onDismiss = onCancel) {
        KarikaText(
            modifier = Modifier.fillMaxWidth(),
            text = "Jeste li sigurni da želite odbiti ovu narudžbu?",
            color = KarikaUiColors.Muted,
            textSize = 14.sp,
            lineHeight = 20.sp
        )
        Spacer(Modifier.height(16.dp))
        OrderField(
            modifier = Modifier.fillMaxWidth(),
            value = reason,
            label = "Poruka za kupca",
            placeholder = "Dodajte poruku za kupca",
            imeAction = ImeAction.Next
        )
        Spacer(Modifier.height(20.dp))
        OrderModalButtons(
            primaryText = "Odbij narudžbu",
            primaryColor = KarikaUiColors.Red,
            onPrimary = { onSubmit(reason.value) },
            onSecondary = onCancel
        )
    }
}

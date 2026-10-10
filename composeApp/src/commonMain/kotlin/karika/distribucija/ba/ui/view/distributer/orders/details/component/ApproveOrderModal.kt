package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors

@Composable
fun ApproveOrderModal(
    value: Pair<String, String>,
    onSubmit: (String, Int, String) -> Unit,
    onCancel: () -> Unit
) {
    val selected = remember { mutableStateOf(Pair("Bez dostave", 0)) }
    val reason = remember { mutableStateOf("") }
    OrderModal(title = "Odobri narudžbu", onDismiss = onCancel) {
        KarikaText(
            text = "Usluga dostave",
            color = KarikaUiColors.Muted,
            textSize = 13.sp,
            fontWeight = FontWeight.W600
        )
        Spacer(Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(
                Pair("Bez dostave", 0),
                Pair("A2B - ${value.first}", 1),
                Pair("EuroExpress - ${value.second}", 2),
            ).forEach { item ->
                OrderOptionRow(
                    text = item.first,
                    selected = selected.value.first == item.first
                ) {
                    selected.value = item
                }
            }
        }
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
            primaryText = "Odobri narudžbu",
            primaryColor = KarikaUiColors.Green,
            onPrimary = { onSubmit(selected.value.first, selected.value.second, reason.value) },
            onSecondary = onCancel
        )
    }
    LaunchedEffect(selected.value) {
        if (!value.first.contains("KM")) {
            selected.value = Pair("Bez dostave", 0)
        }
        if (!value.second.contains("KM")) {
            selected.value = Pair("Bez dostave", 0)
        }
    }
}

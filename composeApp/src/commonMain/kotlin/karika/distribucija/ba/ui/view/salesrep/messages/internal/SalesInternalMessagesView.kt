package karika.distribucija.ba.ui.view.salesrep.messages.internal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText

@Composable
fun SalesInternalMessagesView(component: SalesInternalMessagesComponent) {
    Box(
        modifier = Modifier
            .background(KarikaColors.Gray20)
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            text = "Uskoro dostupno",
            color = KarikaColors.Gray7,
            textSize = 14.sp,
            fontWeight = FontWeight.W400
        )
    }
}

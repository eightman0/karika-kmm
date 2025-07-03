package karika.distribucija.ba.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_right
import org.jetbrains.compose.resources.vectorResource

@Composable
fun TextArrowItem(text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f),
            color = KarikaColors.Gray3,
            text = text,
            fontWeight = FontWeight.W700,
            textSize = 14.sp
        )
        Icon(
            modifier = Modifier
                .padding(end = 16.dp),
            imageVector = vectorResource(Res.drawable.ic_arrow_right),
            contentDescription = "",
            tint = KarikaColors.Black
        )
    }
}

@Composable
fun TextItem(text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp)
                .fillMaxWidth(),
            color = KarikaColors.Gray3,
            text = text,
            fontWeight = FontWeight.W700,
            textSize = 14.sp
        )
    }
}
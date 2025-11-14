package karika.distribucija.ba.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
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
fun TextItem(
    text: String,
    color: Color = KarikaColors.Gray3,
    onClick: () -> Unit = {}
) {
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
            color = color,
            text = text,
            fontWeight = FontWeight.W700,
            textSize = 14.sp
        )
    }
}

@Composable
fun TextIconItem(text: String, icon: ImageVector, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .clickable { onClick() }
            .height(40.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = "",
            tint = KarikaColors.Gray3
        )
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp),
            color = KarikaColors.Gray3,
            text = text,
            fontWeight = FontWeight.W700,
            textSize = 14.sp
        )
    }
}

@Composable
fun RoundedItem(
    title: String,
    select: Boolean = false,
    onClick: () -> Unit = {}
) {
    val selected = mutableStateOf(select).asState()
    Box(
        modifier = Modifier
            .onClick {
                selected.negate()
                onClick()
            }
            .roundedWithBorder(
                color = if (selected.value) KarikaColors.Primary else KarikaColors.White,
                borderColor = if (selected.value) KarikaColors.Primary else KarikaColors.Placeholder,
            ),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            modifier = Modifier
                .padding(vertical = 8.dp, horizontal = 16.dp),
            text = title,
            color = if (selected.value) KarikaColors.White else KarikaColors.Gray2,
            textSize = 14.sp,
            fontWeight = FontWeight.W600
        )
    }
}

@Composable
fun DropdownItem(text: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = KarikaColors.Gray1, shape = RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(start = 16.dp),
            color = KarikaColors.Gray2,
            text = text,
            fontWeight = FontWeight.W400,
            textSize = 14.sp
        )
        Icon(
            modifier = Modifier
                .padding(end = 16.dp),
            imageVector = vectorResource(Res.drawable.ic_arrow_down),
            contentDescription = "",
            tint = KarikaColors.Gray2
        )
    }
}
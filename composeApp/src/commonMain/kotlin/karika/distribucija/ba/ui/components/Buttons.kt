package karika.distribucija.ba.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    title: String,
    color: Color = KarikaColors.Primary,
    icon: DrawableResource,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.W600,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .height(40.dp),
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(width = 1.dp, color = color),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KarikaColors.Transparent
        )
    ) {
        IconTextItem(
            modifier = Modifier,
            icon = vectorResource(icon),
            iconColor = color,
            textColor = color,
            text = title,
            fontWeight = fontWeight,
            textSize = textSize
        )
    }
}

@Composable
fun PrimaryButtonFilled(
    modifier: Modifier = Modifier
        .fillMaxWidth(),
    title: String,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier
            .height(48.dp),
        shape = RoundedCornerShape(100.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KarikaColors.Primary,
            disabledContentColor = KarikaColors.Secondary
        ),
        enabled = enabled
    ) {
        KarikaText(
            text = title,
            color = KarikaColors.White,
            fontWeight = FontWeight.W600,
            textSize = 18.sp
        )
    }
}

@Composable
fun HorizontalButtons(vararg buttons: String, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        buttons.forEach {
            PrimaryButtonFilled(
                modifier = Modifier
                    .weight(1f),
                title = it
            ) {
                onClick(it)
            }
        }
    }
}
package karika.distribucija.ba.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_gift
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource

@Composable
fun PrimaryButton(
    modifier: Modifier = Modifier
        .height(40.dp)
        .fillMaxWidth(),
    title: String,
    color: Color = KarikaColors.Primary,
    icon: DrawableResource? = null,
    textSize: TextUnit = 14.sp,
    fontWeight: FontWeight = FontWeight.W600,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        border = BorderStroke(width = 1.dp, color = color),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KarikaColors.Transparent
        )
    ) {
        IconTextItem(
            modifier = Modifier,
            icon = vectorResource(icon ?: Res.drawable.ic_gift),
            iconColor = color,
            textColor = color,
            text = title,
            fontWeight = fontWeight,
            textSize = textSize,
            iconPosition = if (icon == null) FabPosition.EndOverlay else FabPosition.Start
        )
    }
}

@Composable
fun PrimaryButtonFilled(
    modifier: Modifier = Modifier
        .height(48.dp)
        .fillMaxWidth(),
    title: String,
    enabled: Boolean = true,
    fontWeight: FontWeight = FontWeight.W600,
    color: Color = KarikaColors.White,
    icon: DrawableResource? = null,
    textSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KarikaColors.Primary,
            disabledContentColor = KarikaColors.Secondary
        ),
        enabled = enabled
    ) {
        IconTextItem(
            modifier = Modifier,
            icon = vectorResource(icon ?: Res.drawable.ic_gift),
            iconColor = color,
            textColor = color,
            text = title,
            fontWeight = fontWeight,
            textSize = textSize,
            iconPosition = if (icon == null) FabPosition.EndOverlay else FabPosition.Start
        )
    }
}

@Composable
fun SecondaryButton(
    modifier: Modifier = Modifier
        .height(48.dp)
        .fillMaxWidth(),
    title: String,
    enabled: Boolean = true,
    fontWeight: FontWeight = FontWeight.W600,
    color: Color = KarikaColors.Primary,
    icon: DrawableResource? = null,
    textSize: TextUnit = 14.sp,
    onClick: () -> Unit
) {
    Button(
        modifier = modifier,
        shape = RoundedCornerShape(100.dp),
        onClick = onClick,
        colors = ButtonDefaults.buttonColors(
            containerColor = KarikaColors.White,
            disabledContentColor = KarikaColors.Secondary
        ),
        enabled = enabled
    ) {
        IconTextItem(
            modifier = Modifier,
            icon = vectorResource(icon ?: Res.drawable.ic_gift),
            iconColor = color,
            textColor = color,
            text = title,
            fontWeight = fontWeight,
            textSize = textSize,
            iconPosition = if (icon == null) FabPosition.EndOverlay else FabPosition.Start
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

@Composable
fun HorizontalButtons(
    modifier: Modifier = Modifier
        .padding(horizontal = 16.dp),
    primaryTitle: String,
    secondaryTitle: String,
    onClick: (String) -> Unit
) {
    Row(
        modifier = modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        PrimaryButton(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .weight(1f),
            title = secondaryTitle,
        ) {
            onClick(secondaryTitle)
        }
        PrimaryButtonFilled(
            modifier = Modifier
                .heightIn(min = 40.dp)
                .weight(1f),
            title = primaryTitle
        ) {
            onClick(primaryTitle)
        }
    }
}
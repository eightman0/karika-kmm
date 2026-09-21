package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import org.jetbrains.compose.resources.vectorResource

/** Read/unread filter for a notifications feed, shared across shop/distributer/salesrep -
 * maps to a `searchCriteria[filter_groups]` clause on `is_read` (ALL sends no filter at all). */
enum class NotificationReadFilter(val label: String) {
    ALL("Sve"), UNREAD("Nepročitano"), READ("Pročitano")
}

@Composable
fun ReadFilterDropdown(
    selected: NotificationReadFilter,
    onSelect: (NotificationReadFilter) -> Unit,
    borderColor: Color = KarikaColors.Gray9,
    textColor: Color = KarikaColors.Gray2,
    iconColor: Color = KarikaColors.Gray6,
    selectedTextColor: Color = KarikaColors.Blue
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .border(1.dp, borderColor, RoundedCornerShape(10.dp))
                .onClick { expanded = true }
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            KarikaText(
                text = selected.label,
                color = textColor,
                textSize = 14.sp,
                fontWeight = FontWeight.W600
            )
            Icon(
                imageVector = vectorResource(Res.drawable.ic_arrow_down),
                contentDescription = "",
                tint = iconColor,
                modifier = Modifier.size(16.dp)
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = KarikaColors.White
        ) {
            NotificationReadFilter.entries.forEach { option ->
                DropdownMenuItem(
                    text = {
                        KarikaText(
                            text = option.label,
                            color = if (option == selected) selectedTextColor else KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = if (option == selected) FontWeight.W700 else FontWeight.W500
                        )
                    },
                    onClick = {
                        expanded = false
                        onSelect(option)
                    }
                )
            }
        }
    }
}

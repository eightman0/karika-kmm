package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import org.jetbrains.compose.resources.vectorResource

/** Options with more entries than this get a search field on top (e.g. the cities). */
private const val SEARCH_FROM = 12

/**
 * Bottom sheet that picks one of [options], the selected one marked in [accent].
 * Replaces the dropdown menus in forms.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KOptionSheet(
    title: String,
    options: List<String>,
    selected: String?,
    accent: Color = KarikaUiColors.Pink,
    accentSoft: Color = KarikaUiColors.PinkSoft,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by remember { mutableStateOf("") }
    val shown = remember(options, query) {
        if (query.isBlank()) options else options.filter { it.contains(query.trim(), ignoreCase = true) }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Bottom) },
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp, bottom = 6.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(KarikaUiColors.Border)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 16.dp)
        ) {
            KarikaText(
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 4.dp, bottom = 12.dp),
                text = title,
                color = KarikaUiColors.Ink,
                textSize = 18.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W700
            )
            if (options.size > SEARCH_FROM) {
                KSearchField(
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, bottom = 12.dp),
                    value = query,
                    onValueChange = { query = it },
                    placeholder = "Pretraži…",
                    cursorColor = accent
                )
            }
            KDivider()
            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                shown.forEachIndexed { index, option ->
                    val isSelected = selected == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isSelected) accentSoft else Color.Transparent)
                            .clickable { onSelect(option) }
                            .padding(horizontal = 20.dp, vertical = 15.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        KarikaText(
                            modifier = Modifier.weight(1f),
                            text = option,
                            color = if (isSelected) accent else KarikaUiColors.Ink,
                            textSize = 15.sp,
                            fontWeight = if (isSelected) FontWeight.W700 else FontWeight.W500
                        )
                        if (isSelected) {
                            KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = accent, size = 18.dp)
                        }
                    }
                    if (index < shown.lastIndex) {
                        KDivider(modifier = Modifier.padding(horizontal = 20.dp))
                    }
                }
                if (shown.isEmpty()) {
                    KarikaText(
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 20.dp),
                        text = "Nema rezultata",
                        color = KarikaUiColors.Muted,
                        textSize = 14.sp
                    )
                }
            }
        }
    }
}

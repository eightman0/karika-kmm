package karika.distribucija.ba.ui.view.distributer.products.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import karika.distribucija.ba.domain.model.Category
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.KarikaCheckboxSecondary
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ChooseCategoryModal(
    categories: List<Category>,
    selectedCategories: List<Category>,
    onSubmit: (List<Category>) -> Unit,
    onCancel: () -> Unit
) {
    val selected = mutableStateOf(selectedCategories).asState()
    Dialog(
        onDismissRequest = {
            onCancel()
        },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .padding(16.dp)
                .rounded()
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        KarikaText(
                            modifier = Modifier
                                .weight(1f),
                            text = "Kategorije",
                            color = KarikaColors.Gray2,
                            textSize = 18.sp,
                            fontWeight = FontWeight.W700
                        )
                        Icon(
                            modifier = Modifier
                                .onClick {
                                    onCancel()
                                }
                                .size(48.dp),
                            imageVector = vectorResource(Res.drawable.ic_tertiary),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                    }
                    categories.forEach { c ->
                        val expand = mutableStateOf(false).asState()
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            KarikaCheckboxSecondary(
                                modifier = Modifier.weight(1f),
                                title = c.name,
                                value = selected.value.any { v -> v.id == c.id }
                            ) { _ ->
                                if (selected.value.any { v -> v.id == c.id }) {
                                    selected.value -= c
                                } else {
                                    selected.value += c
                                }
                            }
                            if (c.childrenData.isNotEmpty()) {
                                Icon(
                                    modifier = Modifier
                                        .onClick {
                                            expand.negate()
                                        },
                                    imageVector = vectorResource(Res.drawable.ic_arrow_down),
                                    tint = KarikaColors.Gray2,
                                    contentDescription = ""
                                )
                            }
                        }
                        if (expand.value) {
                            c.childrenData.forEach { c1 ->
                                val expand1 = mutableStateOf(false).asState()
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    KarikaCheckboxSecondary(
                                        modifier = Modifier.padding(start = 24.dp)
                                            .weight(1f),
                                        title = c1.name,
                                        value = selected.value.any { v -> v.id == c1.id }
                                    ) { _ ->
                                        if (selected.value.any { v -> v.id == c1.id }) {
                                            selected.value -= c1
                                        } else {
                                            selected.value += c1
                                        }
                                    }
                                    if (c1.childrenData.isNotEmpty()) {
                                        Icon(
                                            modifier = Modifier
                                                .onClick {
                                                    expand1.negate()
                                                },
                                            imageVector = vectorResource(Res.drawable.ic_arrow_down),
                                            tint = KarikaColors.Gray2,
                                            contentDescription = ""
                                        )
                                    }
                                }
                                if (expand1.value) {
                                    c1.childrenData.forEach { c2 ->
                                        KarikaCheckboxSecondary(
                                            modifier = Modifier.padding(start = 48.dp),
                                            title = c2.name,
                                            value = selected.value.any { v -> v.id == c2.id }
                                        ) { _ ->
                                            if (selected.value.any { v -> v.id == c2.id }) {
                                                selected.value -= c2
                                            } else {
                                                selected.value += c2
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                HorizontalSecondaryButtons(
                    modifier = Modifier
                        .padding(horizontal = 16.dp),
                    primaryTitle = "Spremi",
                    secondaryTitle = "Odustani"
                ) {
                    if (it == "Spremi") {
                        onSubmit(selected.value)
                    } else {
                        onCancel()
                    }
                }
                YSpacer16()
            }
        }
    }
}
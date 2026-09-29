package karika.distribucija.ba.ui.view.distributer.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaDatePicker
import karika.distribucija.ba.ui.components.KarikaPicker
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_calendar
import org.jetbrains.compose.resources.vectorResource

@Composable
fun AnalyticsFiltersView(component: AnalyticsFiltersComponent) {
    val filters = component.filters
    val showDateDialogFrom = mutableStateOf(false).asState()
    val showDateDialogTo = mutableStateOf(false).asState()

    Column(
        modifier = Modifier
            .background(KarikaColors.Gray20)
            .fillMaxSize()
            .hideKeyboard(true)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        IconTextItem(
            modifier = Modifier.onClick { component.back() },
            icon = vectorResource(Res.drawable.ic_arrow_back),
            iconColor = KarikaColors.Gray2,
            textColor = KarikaColors.Gray2,
            text = "Nazad na analitiku",
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            iconPosition = FabPosition.Start
        )
        KarikaText(
            modifier = Modifier.fillMaxWidth(),
            text = "Filteri",
            color = KarikaColors.Gray2,
            textSize = 18.sp,
            fontWeight = FontWeight.W700
        )

        FiltersCard {
            KarikaText(text = "Period", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                KarikaTextField2(
                    modifier = Modifier
                        .onClick { showDateDialogFrom.negate() }
                        .weight(1f),
                    value = filters.dateFrom,
                    placeholder = "OD",
                    imeAction = ImeAction.Next,
                    enabled = false,
                    disabledTextColor = KarikaColors.Gray2,
                    trailingIcons = {
                        Icon(
                            modifier = Modifier.onClick { showDateDialogFrom.negate() },
                            imageVector = vectorResource(Res.drawable.ic_calendar),
                            tint = KarikaColors.Gray22,
                            contentDescription = ""
                        )
                    }
                )
                KarikaTextField2(
                    modifier = Modifier
                        .onClick { showDateDialogTo.negate() }
                        .weight(1f),
                    value = filters.dateTo,
                    placeholder = "DO",
                    imeAction = ImeAction.Next,
                    enabled = false,
                    disabledTextColor = KarikaColors.Gray2,
                    trailingIcons = {
                        Icon(
                            modifier = Modifier.onClick { showDateDialogTo.negate() },
                            imageVector = vectorResource(Res.drawable.ic_calendar),
                            tint = KarikaColors.Gray22,
                            contentDescription = ""
                        )
                    }
                )
            }
        }

        FiltersCard {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    KarikaPicker(
                        title = "Grupisanje",
                        values = filters.groupingOptions,
                        value = filters.grouping
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    KarikaPicker(
                        title = "Poređenje",
                        values = filters.comparisonOptions,
                        value = filters.comparison
                    )
                }
            }
        }

        FiltersCard {
            KarikaText(text = "Komercijalista", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaTextField2(
                modifier = Modifier.fillMaxWidth(),
                value = filters.repQuery,
                placeholder = "Pretraži komercijaliste",
                imeAction = ImeAction.Next
            )
        }

        FiltersCard {
            KarikaText(text = "Kupac", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaTextField2(
                modifier = Modifier.fillMaxWidth(),
                value = filters.customerQuery,
                placeholder = "Pretraži kupce",
                imeAction = ImeAction.Next
            )
        }

        FiltersCard {
            KarikaText(text = "Proizvod", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaTextField2(
                modifier = Modifier.fillMaxWidth(),
                value = filters.productQuery,
                placeholder = "Pretraži proizvode",
                imeAction = ImeAction.Next
            )
        }

        FiltersCard {
            KarikaText(text = "Kategorija", color = KarikaColors.Gray2, textSize = 16.sp, fontWeight = FontWeight.W700)
            KarikaTextField2(
                modifier = Modifier.fillMaxWidth(),
                value = filters.categoryQuery,
                placeholder = "Pretraži kategorije",
                imeAction = ImeAction.Done
            )
        }

        YSpacer16()
        SecondaryButtonFilled(
            modifier = Modifier.fillMaxWidth(),
            title = "Primijeni"
        ) {
            component.back()
        }
    }

    KarikaDatePicker(showPicker = showDateDialogFrom, selectableDatesInPast = true) {
        filters.dateFromMillis.value = it
        filters.dateFrom.value = it.toDate1()
    }
    KarikaDatePicker(showPicker = showDateDialogTo, selectableDatesInPast = true) {
        filters.dateToMillis.value = it
        filters.dateTo.value = it.toDate1()
    }
}

@Composable
private fun FiltersCard(content: @Composable ColumnScope.() -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(KarikaColors.White, RoundedCornerShape(8.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = content
    )
}

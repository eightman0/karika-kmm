package karika.distribucija.ba.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
@Composable
fun KarikaDatePicker(
    showPicker: MutableState<Boolean>,
    preselectedDateMillis: Long = Clock.System.now().toEpochMilliseconds(),
    selectableDatesInPast: Boolean = false,
    selectableCurrentWeek: Boolean = false,
    //formatter: DatePickerFormatter = remember {
    //    DatePickerDefaults.dateFormatter(
    //        selectedDateSkeleton = "dd/MM/yyyy",
    //        selectedDateDescriptionSkeleton = "dd/MM/yyyy",
    //        yearSelectionSkeleton = "dd/MM/yyyy"
    //    )
    //},
    onSelected: (Long) -> Unit
) {
    fun getWeekRange(): Pair<Long, Long> {
        val timeZone = TimeZone.currentSystemDefault()
        val now = Clock.System.now()
        val currentDate = now.toLocalDateTime(timeZone).date

        val isoDay = currentDate.dayOfWeek.isoDayNumber

        val daysSinceSaturday = if (isoDay == 6) 0 else (isoDay + 1) % 7

        val startOfWeekDate = currentDate.minus(daysSinceSaturday, DateTimeUnit.DAY)
        val endOfWeekDate = startOfWeekDate.plus(6, DateTimeUnit.DAY)

        val startOfWeek = LocalDateTime(startOfWeekDate, LocalTime(0, 0)).toInstant(timeZone)
        val endOfWeek = LocalDateTime(endOfWeekDate, LocalTime(23, 59, 59)).toInstant(timeZone)

        return Pair(startOfWeek.toEpochMilliseconds(), endOfWeek.toEpochMilliseconds())
    }

    val weekRange = getWeekRange()
    val datePickerState = rememberDatePickerState(
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                if (selectableCurrentWeek) {
                    return utcTimeMillis in weekRange.first..weekRange.second
                }
                if (selectableDatesInPast) {
                    return true
                }
                return utcTimeMillis >= Clock.System.now()
                    .toLocalDateTime(TimeZone.UTC)
                    .date
                    .atStartOfDayIn(TimeZone.UTC)
                    .toEpochMilliseconds()
            }

            override fun isSelectableYear(year: Int): Boolean {
                if (selectableDatesInPast) {
                    return true
                }
                return year >= Clock.System.now()
                    .toLocalDateTime(TimeZone.UTC)
                    .year
            }
        },
        initialSelectedDateMillis = if (preselectedDateMillis in -2208988800000..4102444800000) {
            preselectedDateMillis
        } else {
            Clock.System.now().toEpochMilliseconds()
        }
    )

    if (showPicker.value) {
        DatePickerDialog(
            modifier = Modifier,
            onDismissRequest = {
                showPicker.negate()
            },
            confirmButton = {
                Button(
                    modifier = Modifier
                        .padding(16.dp),
                    onClick = {
                        onSelected.invoke(datePickerState.selectedMillis)
                        showPicker.negate()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = KarikaColors.Blue
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    KarikaText(
                        modifier = Modifier,
                        text = "OK",
                        color = KarikaColors.White,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W400
                    )
                }
            },
            shape = RoundedCornerShape(12.dp),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            colors = DatePickerDefaults.colors(
                containerColor = KarikaColors.Gray20,
            )
        ) {
            DatePicker(
                state = datePickerState,
                modifier = Modifier,
                // dateFormatter = formatter,
                colors = DatePickerDefaults.colors(
                    containerColor = KarikaColors.Gray20,
                    selectedDayContainerColor = KarikaColors.Gray2,
                    selectedDayContentColor = KarikaColors.White,
                    todayContentColor = KarikaColors.Gray2,
                    todayDateBorderColor = KarikaColors.Gray2,
                    disabledDayContentColor = KarikaColors.Gray15
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalTime::class)
private val DatePickerState.selectedMillis: Long
    get() = this.selectedDateMillis ?: Clock.System.now().toEpochMilliseconds()
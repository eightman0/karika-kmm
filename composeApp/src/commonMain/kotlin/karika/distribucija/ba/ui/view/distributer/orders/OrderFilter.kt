package karika.distribucija.ba.ui.view.distributer.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaAmountField
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaDatePicker
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.YSpacer32
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_calendar
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderFilterSheet(
    component: OrdersComponent
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val startPrice = component.filterPriceFrom.asState()
    val endPrice = component.filterPriceTo.asState()
    val orderNumber = component.orderNumber.asState()
    val payerName = component.payerName.asState()
    val dateFrom = component.dateFrom.asState()
    val dateTo = component.dateTo.asState()
    val showState = component.showFilterState.asState()
    val showDateDialogFrom = mutableStateOf(false).asState()
    val showDateDialogTo = mutableStateOf(false).asState()

    if (showState.value) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 100.dp),
            onDismissRequest = {
                showState.negate()
            },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            dragHandle = {
                BottomSheetDefaults.DragHandle(
                    color = KarikaColors.Gray2,
                    width = 60.dp
                )
            }
        ) {
            Column {
                KarikaText(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    text = "FILTERI",
                    color = KarikaColors.Gray2,
                    textSize = 18.sp,
                    fontWeight = FontWeight.W400,
                    textAlign = TextAlign.Center
                )
                YSpacer16()
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(),
                    thickness = 1.dp,
                    color = KarikaColors.Divider
                )
                YSpacer16()
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .hideKeyboard(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Datum kupovine",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KarikaTextField2(
                            modifier = Modifier
                                .onClick {
                                    showDateDialogFrom.negate()
                                }
                                .weight(1f),
                            value = dateFrom,
                            placeholder = "OD",
                            imeAction = ImeAction.Next,
                            enabled = false,
                            disabledTextColor = KarikaColors.Gray2,
                            keyboardType = KeyboardType.Number,
                            trailingIcons = {
                                Icon(
                                    modifier = Modifier
                                        .onClick {
                                            showDateDialogFrom.negate()
                                        },
                                    imageVector = vectorResource(Res.drawable.ic_calendar),
                                    tint = KarikaColors.Gray22,
                                    contentDescription = ""
                                )
                            }
                        )
                        KarikaTextField2(
                            modifier = Modifier
                                .onClick {
                                    showDateDialogTo.negate()
                                }
                                .weight(1f),
                            value = dateTo,
                            placeholder = "DO",
                            disabledTextColor = KarikaColors.Gray2,
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number,
                            enabled = false,
                            trailingIcons = {
                                Icon(
                                    modifier = Modifier
                                        .onClick {
                                            showDateDialogTo.negate()
                                        },
                                    imageVector = vectorResource(Res.drawable.ic_calendar),
                                    tint = KarikaColors.Gray22,
                                    contentDescription = ""
                                )
                            }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )

                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Ukupno VPC",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        KarikaAmountField(
                            modifier = Modifier
                                .weight(1f),
                            value = startPrice,
                            placeholder = "OD",
                            imeAction = ImeAction.Next,
                            trailingIcons = {
                                KarikaText(
                                    modifier = Modifier,
                                    text = "KM",
                                    color = KarikaColors.Gray22,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                        )
                        KarikaAmountField(
                            modifier = Modifier
                                .weight(1f),
                            value = endPrice,
                            placeholder = "DO",
                            imeAction = ImeAction.Next,
                            trailingIcons = {
                                KarikaText(
                                    modifier = Modifier,
                                    text = "KM",
                                    color = KarikaColors.Gray22,
                                    textSize = 14.sp,
                                    fontWeight = FontWeight.W400
                                )
                            }
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Broj narudžbe",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    KarikaTextField2(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        value = orderNumber,
                        placeholder = "Broj narudžbe",
                        allowedChars = KarikaConstants.numbers,
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Number
                    )
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Račun na ime",
                        color = KarikaColors.Gray2,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W700
                    )
                    KarikaTextField2(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        value = payerName,
                        placeholder = "Račun na ime",
                        imeAction = ImeAction.Next,
                        keyboardType = KeyboardType.Text
                    )

                    YSpacer32()
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        SecondaryButton(
                            modifier = Modifier
                                .weight(1f),
                            title = "Odustani",
                            textSize = 16.sp,
                            color = KarikaColors.Blue
                        ) {
                            showState.negate()
                        }
                        SecondaryButtonFilled(
                            modifier = Modifier
                                .weight(1f),
                            title = "Filtriraj"
                        ) {
                            showState.negate()
                            component.filter()
                        }
                    }
                }
            }
            KarikaDatePicker(
                showPicker = showDateDialogFrom,
                selectableDatesInPast = true
            ) {
                dateFrom.value = it.toDate()
            }
            KarikaDatePicker(
                showPicker = showDateDialogTo,
                selectableDatesInPast = true
            ) {
                dateTo.value = it.toDate()
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
fun Long.toDate(): String {
    val localDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)

    val dateFormat = LocalDateTime.Format {
        year()
        char('-')
        monthNumber()
        char('-')
        dayOfMonth()
    }
    return localDate.format(dateFormat)
}

@OptIn(ExperimentalTime::class)
fun Long.toDate1(): String {
    val localDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)

    val dateFormat = LocalDateTime.Format {
        dayOfMonth()
        char('.')
        monthNumber()
        char('.')
        year()
        char('.')
    }
    return localDate.format(dateFormat)
}

@OptIn(ExperimentalTime::class)
fun String.toDate1(): String {
    val isoString = replace(" ", "T")
    val localDateTime = LocalDateTime.parse(isoString)
    val instant = localDateTime.toInstant(TimeZone.UTC)
    return instant.toEpochMilliseconds().toDate1()
}
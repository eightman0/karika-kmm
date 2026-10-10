package karika.distribucija.ba.ui.view.distributer.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KTextField
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaDatePicker
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_calendar
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.vectorResource
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** Test tags of the order filter sheet, for the end-to-end tests. */
const val ORDER_FILTER_PRICE_FROM_TAG = "order_filter_price_from"
const val ORDER_FILTER_PRICE_TO_TAG = "order_filter_price_to"
const val ORDER_FILTER_NUMBER_TAG = "order_filter_number"
const val ORDER_FILTER_PAYER_TAG = "order_filter_payer"

private val AMOUNT_REGEX = Regex("^(0|[1-9]\\d*)([.]\\d{0,2})?$")

/** Same input rules as KarikaAmountField: a positive amount with at most two decimals. */
private fun MutableState<String>.setAmount(input: String) {
    val newValue = input.replace(',', '.')
    if (newValue == "0") {
        value = newValue
        return
    }
    if (newValue.startsWith("0") && !newValue.startsWith("0.")) {
        return
    }
    if (newValue.isEmpty() || AMOUNT_REGEX.matches(newValue)) {
        value = newValue
    }
}

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
            // Keeps the sheet clear of the status bar when it is tall
            modifier = Modifier.padding(top = 56.dp),
            onDismissRequest = {
                showState.negate()
            },
            sheetState = sheetState,
            containerColor = KarikaColors.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            contentWindowInsets = { WindowInsets.navigationBars.only(WindowInsetsSides.Bottom) },
            dragHandle = { SheetHandle() }
        ) {
            Column(modifier = Modifier.imePadding()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 12.dp, top = 4.dp, bottom = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KarikaText(
                        modifier = Modifier.weight(1f),
                        text = "Filteri",
                        color = KarikaUiColors.Ink,
                        textSize = 20.sp,
                        lineHeight = 24.sp,
                        fontWeight = FontWeight.W700
                    )
                    KarikaText(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { component.clear() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        text = "Poništi sve",
                        color = VendorAccent,
                        textSize = 14.sp,
                        fontWeight = FontWeight.W600
                    )
                }
                KDivider()
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState())
                        .hideKeyboard()
                ) {
                    FilterSection("Datum kupovine") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            DateBox(
                                modifier = Modifier.weight(1f),
                                value = dateFrom.value,
                                placeholder = "Od",
                                onClick = { showDateDialogFrom.negate() }
                            )
                            DateBox(
                                modifier = Modifier.weight(1f),
                                value = dateTo.value,
                                placeholder = "Do",
                                onClick = { showDateDialogTo.negate() }
                            )
                        }
                    }
                    FilterSection("Ukupno VPC") {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            KTextField(
                                modifier = Modifier
                                    .testTag(ORDER_FILTER_PRICE_FROM_TAG)
                                    .weight(1f),
                                value = startPrice.value,
                                onValueChange = { startPrice.setAmount(it) },
                                placeholder = "Od",
                                keyboardType = KeyboardType.Decimal,
                                minHeight = 48.dp,
                                trailing = { KmSuffix() }
                            )
                            KTextField(
                                modifier = Modifier
                                    .testTag(ORDER_FILTER_PRICE_TO_TAG)
                                    .weight(1f),
                                value = endPrice.value,
                                onValueChange = { endPrice.setAmount(it) },
                                placeholder = "Do",
                                keyboardType = KeyboardType.Decimal,
                                minHeight = 48.dp,
                                trailing = { KmSuffix() }
                            )
                        }
                    }
                    FilterSection("Broj narudžbe") {
                        KTextField(
                            modifier = Modifier.testTag(ORDER_FILTER_NUMBER_TAG),
                            value = orderNumber.value,
                            onValueChange = { new -> if (new.all { it.isDigit() }) orderNumber.value = new },
                            placeholder = "npr. 3000000859",
                            keyboardType = KeyboardType.Number,
                            minHeight = 48.dp
                        )
                    }
                    FilterSection("Račun na ime", divider = false) {
                        KTextField(
                            modifier = Modifier.testTag(ORDER_FILTER_PAYER_TAG),
                            value = payerName.value,
                            onValueChange = { payerName.value = it },
                            placeholder = "Naziv kupca",
                            keyboardType = KeyboardType.Text,
                            imeAction = ImeAction.Done,
                            minHeight = 48.dp
                        )
                    }
                }
                KDivider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    KTonalButton(
                        modifier = Modifier.weight(1f),
                        text = "Odustani",
                        background = KarikaUiColors.Field,
                        color = KarikaUiColors.Ink,
                        height = 52.dp,
                        onClick = { showState.negate() }
                    )
                    KPrimaryButton(
                        modifier = Modifier.weight(1f),
                        text = "Prikaži narudžbe",
                        background = VendorAccent,
                        onClick = {
                            showState.negate()
                            component.filter()
                        }
                    )
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

@Composable
private fun FilterSection(
    title: String,
    divider: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 16.dp)) {
        KarikaText(
            modifier = Modifier.padding(bottom = 10.dp),
            text = title,
            color = KarikaUiColors.Ink,
            textSize = 14.sp,
            fontWeight = FontWeight.W700
        )
        content()
    }
    if (divider) {
        KDivider()
    }
}

/** Read-only date field that opens the date picker. */
@Composable
private fun DateBox(
    value: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .height(48.dp)
            .clip(shape)
            .background(KarikaColors.White)
            .border(1.dp, KarikaUiColors.Border, shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier.weight(1f),
            text = value.ifEmpty { placeholder },
            color = if (value.isEmpty()) KarikaUiColors.Subtle else KarikaUiColors.Ink,
            textSize = 15.sp,
            maxLines = 1
        )
        KIcon(icon = vectorResource(Res.drawable.ic_k_calendar), tint = KarikaUiColors.Muted, size = 18.dp)
    }
}

@Composable
private fun KmSuffix() {
    KarikaText(text = "KM", color = KarikaUiColors.Muted, textSize = 13.sp, fontWeight = FontWeight.W700)
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
        day()
    }
    return localDate.format(dateFormat)
}

@OptIn(ExperimentalTime::class)
fun Long.toDate1(): String {
    val localDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)

    val dateFormat = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
        char('.')
    }
    return localDate.format(dateFormat)
}

@OptIn(ExperimentalTime::class)
fun Long.toDateTime(): String {
    val localDate = Instant.fromEpochMilliseconds(this)
        .toLocalDateTime(TimeZone.UTC)

    val dateFormat = LocalDateTime.Format {
        day()
        char('.')
        monthNumber()
        char('.')
        year()
        char('.')
        char(' ')
        hour()
        char(':')
        minute()
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

@OptIn(ExperimentalTime::class)
fun String.toDateTime(): String {
    val isoString = replace(" ", "T")
    val localDateTime = LocalDateTime.parse(isoString)
    val instant =
        localDateTime.toInstant(TimeZone.UTC).toLocalDateTime(TimeZone.of("Europe/Sarajevo"))
            .toInstant(TimeZone.UTC)
    return instant.toEpochMilliseconds().toDateTime()
}
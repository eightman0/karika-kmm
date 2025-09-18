package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import karika.distribucija.ba.ui.components.KarikaCheckboxSecondary
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField2
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsComponent


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditOrderSheet(
    component: OrderDetailsComponent
) {
    val product = component.editOrderItem.asState()

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val newQty = mutableStateOf(product.value?.qtyOrdered ?: "0").asState()
    val discount = mutableStateOf(product.value?.rabat() ?: "0").asState()
    val discountAll = mutableStateOf(false).asState()

    ModalBottomSheet(
        modifier = Modifier
            .padding(top = 100.dp),
        onDismissRequest = {},
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = KarikaColors.Gray2,
                width = 60.dp
            )
        }
    ) {
        Column(
            modifier = Modifier
                .hideKeyboard()
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = "Izmijeni narudžbu",
                color = KarikaColors.Gray2,
                textSize = 18.sp,
                fontWeight = FontWeight.W400,
                textAlign = TextAlign.Center
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
                text = "Rabat (%)",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaTextField2(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = discount,
                placeholder = "rabat",
                imeAction = ImeAction.Next,
                enabled = true,
                keyboardType = KeyboardType.Number
            )
            KarikaCheckboxSecondary(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                title = "Primijeni za sve proizvode u narudžbi",
                value = discountAll.value
            ) {
                discountAll.value = it
            }

            YSpacer8()
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp),
                text = "Količina (${product.value?.unit})",
                color = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W700
            )
            KarikaTextField2(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                value = newQty,
                placeholder = "Količina",
                imeAction = ImeAction.Next,
                enabled = true,
                keyboardType = KeyboardType.Number
            )

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
                    product.value = null
                }
                SecondaryButtonFilled(
                    modifier = Modifier
                        .weight(1f),
                    title = "Izmijeni"
                ) {

                    component.editOrderProduct(
                        discount.value,
                        discountAll.value,
                        newQty.value
                    )
                }
            }
        }
        LoadingView1(component)
    }
}
package karika.distribucija.ba.ui.view.distributer.orders.details.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KDivider
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.view.distributer.VendorAccent
import karika.distribucija.ba.ui.view.distributer.orders.details.OrderDetailsComponent
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_check
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.vectorResource


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
    val rabatError = mutableStateOf("").asState()
    val qtyError = mutableStateOf("").asState()

    ModalBottomSheet(
        modifier = Modifier
            .padding(top = 100.dp),
        onDismissRequest = {
            component.scope.launch {
                product.value = null
            }
        },
        sheetState = sheetState,
        containerColor = KarikaColors.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
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
                .hideKeyboard()
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp)
        ) {
            KarikaText(
                text = "Izmijeni narudžbu",
                color = KarikaUiColors.Ink,
                textSize = 20.sp,
                lineHeight = 24.sp,
                fontWeight = FontWeight.W700
            )
            if (!product.value?.name.isNullOrEmpty()) {
                Spacer(Modifier.height(4.dp))
                KarikaText(
                    text = product.value?.name,
                    color = KarikaUiColors.Muted,
                    textSize = 14.sp,
                    lineHeight = 19.sp
                )
            }
            Spacer(Modifier.height(18.dp))
            OrderField(
                modifier = Modifier.fillMaxWidth(),
                value = discount,
                label = "Rabat (%)",
                placeholder = "rabat",
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                leadingZero = false,
                maxLength = 3,
                error = rabatError.value,
                trailingText = "%",
                onValueChange = {
                    if (it.toIntOrNull() != null) {
                        if (it.toInt() > 100) {
                            rabatError.value = "Rabat ne može biti veći od 100%"
                        } else {
                            rabatError.value = ""
                        }
                    } else {
                        rabatError.value = "Rabat je obavezan!"
                    }
                }
            )
            Spacer(Modifier.height(12.dp))
            ApplyToAllRow(checked = discountAll.value) {
                discountAll.value = it
            }
            Spacer(Modifier.height(18.dp))
            OrderField(
                modifier = Modifier.fillMaxWidth(),
                value = newQty,
                label = "Količina (${product.value?.unit})",
                placeholder = "Količina",
                imeAction = ImeAction.Done,
                keyboardType = KeyboardType.Number,
                allowedChars = KarikaConstants.numbers,
                leadingZero = false,
                error = qtyError.value,
                onValueChange = {
                    if (it.toIntOrNull() != null) {
                        if (it.toInt() == 0) {
                            qtyError.value = "Količina ne može biti nula"
                        } else {
                            qtyError.value = ""
                        }
                    } else {
                        qtyError.value = "Količina je obavezna!"
                    }
                }
            )
            Spacer(Modifier.height(20.dp))
            KDivider()
            Spacer(Modifier.height(16.dp))
            OrderModalButtons(
                primaryText = "Izmijeni",
                primaryEnabled = rabatError.value.isEmpty() && qtyError.value.isEmpty(),
                onPrimary = {
                    component.editOrderProduct(
                        discount.value,
                        discountAll.value,
                        newQty.value
                    )
                },
                onSecondary = {
                    product.value = null
                }
            )
        }
    }
}

/** Checkbox row "Primijeni za sve proizvode u narudžbi". */
@Composable
private fun ApplyToAllRow(checked: Boolean, onChange: (Boolean) -> Unit) {
    val shape = RoundedCornerShape(6.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .clickable { onChange(!checked) }
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(shape)
                .background(if (checked) VendorAccent else KarikaColors.White)
                .border(1.5.dp, if (checked) VendorAccent else KarikaUiColors.Border, shape),
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                KIcon(icon = vectorResource(Res.drawable.ic_k_check), tint = KarikaColors.White, size = 14.dp)
            }
        }
        Spacer(Modifier.width(10.dp))
        KarikaText(
            text = "Primijeni za sve proizvode u narudžbi",
            color = KarikaUiColors.Ink,
            textSize = 14.sp
        )
    }
}

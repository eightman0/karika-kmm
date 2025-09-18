package karika.distribucija.ba.ui.view.distributer.products.components

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.SecondaryButton
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.distributer.products.details.ProductDetailsComponent
import karika.distribucija.ba.util.KarikaConstants


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TakeDataFromAISheet(
    component: ProductDetailsComponent
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val name = component.name.asState()
    val short = component.shortDesc.asState()
    val longDesc = component.longDesc.asState()
    val aiImages = component.aiImages.asState()
    val showAISheet = component.showAISheet.asState()
    val aiImagesSelected = component.aiImagesSelected.asState()

    if (showAISheet.value) {
        ModalBottomSheet(
            modifier = Modifier
                .padding(top = 100.dp),
            onDismissRequest = {

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
                    text = "Popuni sa KARIKA AI",
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
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        KarikaTextField1(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = name,
                            title = "Naziv proizvoda*",
                            placeholder = "Naziv proizvoda",
                            allowedChars = KarikaConstants.numbers,
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Number
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        KarikaTextField1(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = short,
                            title = "Kratki opis artikla",
                            placeholder = "Kratki opis artikla",
                            imeAction = ImeAction.Next,
                            keyboardType = KeyboardType.Text
                        )
                    }
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth()
                    ) {
                        KarikaTextField1(
                            modifier = Modifier
                                .fillMaxWidth(),
                            value = longDesc,
                            title = "Opis artikla",
                            placeholder = "Opis artikla",
                            imeAction = ImeAction.Done,
                            keyboardType = KeyboardType.Text
                        )
                    }
                    KarikaText(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        text = "Odaberite sliku koja najbolje oslikava Vaš proizvod",
                        color = KarikaColors.Gray4,
                        textSize = 16.sp,
                        fontWeight = FontWeight.W400
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .height(140.dp)
                            .horizontalScroll(rememberScrollState())
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        aiImages.value.forEach {
                            KarikaImage(
                                modifier = Modifier
                                    .border(
                                        width = 2.dp,
                                        color = if (aiImagesSelected.value.contains(it)) KarikaColors.Blue else KarikaColors.Gray21,
                                        shape = RoundedCornerShape(4.dp)
                                    )
                                    .size(120.dp)
                                    .onClick {
                                        if (aiImagesSelected.value.contains(it)) {
                                            aiImagesSelected.value -= it
                                        } else {
                                            aiImagesSelected.value += it
                                        }
                                    },
                                model = it
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier
                            .fillMaxWidth(),
                        thickness = 1.dp,
                        color = KarikaColors.Divider
                    )
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
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
                            showAISheet.negate()
                            longDesc.value = ""
                            short.value = ""
                        }
                        SecondaryButtonFilled(
                            modifier = Modifier
                                .weight(1f),
                            title = "PRIHVATI"
                        ) {
                            component.acceptAIInput()
                        }
                    }
                }
            }
        }
    }
}
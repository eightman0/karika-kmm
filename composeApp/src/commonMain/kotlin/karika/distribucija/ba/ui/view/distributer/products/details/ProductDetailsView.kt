package karika.distribucija.ba.ui.view.distributer.products.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FabPosition
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.HorizontalSecondaryButtons
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaDatePicker
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaSwitch1
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaTextField1
import karika.distribucija.ba.ui.components.SecondaryButtonFilled
import karika.distribucija.ba.ui.components.YSpacer8
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.components.rounded
import karika.distribucija.ba.ui.view.distributer.orders.toDate1
import karika.distribucija.ba.ui.view.distributer.products.components.ChooseCategoryModal
import karika.distribucija.ba.ui.view.distributer.products.components.FillWithAIModal
import karika.distribucija.ba.ui.view.distributer.products.components.TakeDataFromAISheet
import karika.distribucija.ba.util.KarikaConstants
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_ai
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_down
import karikav2.composeapp.generated.resources.ic_calendar
import karikav2.composeapp.generated.resources.ic_photo
import karikav2.composeapp.generated.resources.ic_tertiary
import org.jetbrains.compose.resources.vectorResource

@Composable
fun ProductDetailsView(component: ProductDetailsComponent) {
    val focusManager = LocalFocusManager.current
    val nestedScrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(
                available: Offset,
                source: NestedScrollSource
            ): Offset {
                focusManager.clearFocus()
                return Offset.Zero
            }
        }
    }

    Column(
        modifier = Modifier
            .background(color = KarikaColors.Gray20)
            .fillMaxSize()
            .nestedScroll(nestedScrollConnection)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier
                .hideKeyboard()
                .padding(start = 16.dp, end = 16.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Title(component)
            NameBox(component)
            PriceBox(component)
            MinQtyBox(component)
            CategoriesBox(component)
            SwitchBox(component)
            DescBox(component)
            Images(component)
        }
        ButtonsBox(component)
    }
}

@Composable
private fun Title(component: ProductDetailsComponent) {
    val product by component.product.collectAsState()
    val showAIModal = mutableStateOf(false).asState()
    val name = component.name.asState()

    IconTextItem(
        modifier = Modifier
            .onClick {
                component.dashBack()
            },
        icon = vectorResource(Res.drawable.ic_arrow_back),
        iconColor = KarikaColors.Gray2,
        textColor = KarikaColors.Gray2,
        text = "Nazad na upravljanje artiklima",
        fontWeight = FontWeight.W400,
        textSize = 14.sp,
        iconPosition = FabPosition.Start
    )

    if (product.name.isNullOrEmpty()) {
        KarikaText(
            modifier = Modifier,
            text = "Dodavanje novog artikla",
            color = KarikaColors.Gray2,
            textSize = 18.sp,
            fontWeight = FontWeight.W700
        )
        SecondaryButtonFilled(
            modifier = Modifier,
            title = "Popuni sa AI",
            iconPosition = FabPosition.Start,
            icon = Res.drawable.ic_ai
        ) {
            showAIModal.negate()
        }
    }

    if (showAIModal.value) {
        FillWithAIModal(
            value = name.value,
            onSubmit = {
                showAIModal.negate()
                name.value = it
                component.fillWithAi()
            },
            onCancel = {
                showAIModal.negate()
            }
        )
    }

    TakeDataFromAISheet(component)
}

@Composable
private fun Images(component: ProductDetailsComponent) {
    val product by component.product.collectAsState()

    Column {
        KarikaText(
            modifier = Modifier,
            text = "Slika proizvoda",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        YSpacer8()
        LazyRow(
            modifier = Modifier
                .height(120.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .onClick {
                            component.pickImage()
                        }
                        .dashedBorder(color = KarikaColors.Blue2, cornerRadius = 3.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Column(
                        modifier = Modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            modifier = Modifier
                                .padding(vertical = 16.dp),
                            imageVector = vectorResource(Res.drawable.ic_photo),
                            contentDescription = "",
                            tint = KarikaColors.Gray2
                        )
                        KarikaText(
                            modifier = Modifier
                                .padding(bottom = 8.dp),
                            text = "Dodaj sliku",
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W400
                        )
                    }
                }
            }
            items(items = product.mediaGallery) {
                KarikaImage(
                    modifier = Modifier
                        .size(120.dp)
                        .onClick {
                            component.showImagePreview(it.image().toString())
                        }
                        .fillMaxSize(),
                    model = it.image()
                )
            }
        }
    }
}

@Composable
private fun PriceBox(component: ProductDetailsComponent) {
    val price = component.price.asState()
    val specialPrice = component.specialPrice.asState()
    val specialPriceFrom = component.specialPriceFrom.asState()
    val specialPriceTo = component.specialPriceTo.asState()
    val showDateDialogFrom = mutableStateOf(false).asState()
    val showDateDialogTo = mutableStateOf(false).asState()

    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Redovna cijena (VPC)*",
        value = price,
        placeholder = "Redovna cijena (VPC)",
        keyboardType = KeyboardType.Number,
        allowedChars = KarikaConstants.numbers.plus(","),
        imeAction = ImeAction.Next,
        trailingIcons = {
            KarikaText(
                modifier = Modifier,
                text = "KM",
                color = KarikaColors.Gray4,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Akcijska cijena (VPC)",
        value = specialPrice,
        placeholder = "Akcijska cijena (VPC)",
        keyboardType = KeyboardType.Number,
        imeAction = ImeAction.Next,
        allowedChars = KarikaConstants.numbers.plus(","),
        trailingIcons = {
            KarikaText(
                modifier = Modifier,
                text = "KM",
                color = KarikaColors.Gray4,
                textSize = 16.sp,
                fontWeight = FontWeight.W400
            )
        }
    )
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Akcijska cijena od",
                value = specialPriceFrom,
                placeholder = "Od",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                trailingIcons = {
                    Icon(
                        modifier = Modifier
                            .onClick {
                                showDateDialogFrom.negate()
                            },
                        imageVector = vectorResource(Res.drawable.ic_calendar),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                },
                enabled = false
            )
        }
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Akcijska cijena do",
                value = specialPriceTo,
                placeholder = "Do",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                trailingIcons = {
                    Icon(
                        modifier = Modifier
                            .onClick {
                                showDateDialogTo.negate()
                            },
                        imageVector = vectorResource(Res.drawable.ic_calendar),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                },
                enabled = false
            )
        }
    }

    KarikaDatePicker(
        showPicker = showDateDialogFrom,
        selectableDatesInPast = true
    ) {
        specialPriceFrom.value = it.toDate1()
    }
    KarikaDatePicker(
        showPicker = showDateDialogTo,
        selectableDatesInPast = true
    ) {
        specialPriceTo.value = it.toDate1()
    }
}

@Composable
private fun NameBox(component: ProductDetailsComponent) {
    val approveProduct = component.approveProduct.asState()
    val name = component.name.asState()
    val barcode = component.barcode.asState()
    val sku = component.sku.asState()

    KarikaSwitch1(
        title = "Prikaži artikal kao aktivan",
        checked = approveProduct
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Naziv proizvoda*",
        value = name,
        placeholder = "Naziv proizvoda",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Barcode",
        value = barcode,
        placeholder = "Barcode",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "SKU",
        value = sku,
        placeholder = "SKU",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next,
        enabled = false
    )
}

@Composable
private fun MinQtyBox(component: ProductDetailsComponent) {
    val minQty = component.minQty.asState()
    val minQtyUnit = component.minQtyUnit.asState()
    val availableQty = component.availableQty.asState()
    val dropdownState = remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(modifier = Modifier.weight(1f)) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Min. Količina*",
                value = minQty,
                placeholder = "Min. Količina",
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Next
            )
        }
        BoxWithConstraints(modifier = Modifier.weight(1f)) {
            KarikaTextField1(
                modifier = Modifier
                    .fillMaxWidth(),
                title = "Jedinica mjere*",
                value = minQtyUnit,
                placeholder = "Jedinica mjere",
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
                trailingIcons = {
                    Icon(
                        modifier = Modifier
                            .onClick {
                                dropdownState.negate()
                            },
                        imageVector = vectorResource(Res.drawable.ic_arrow_down),
                        contentDescription = "",
                        tint = KarikaColors.Gray2
                    )
                },
                enabled = false
            )
            if (dropdownState.value) {
                DropdownMenu(
                    modifier = Modifier
                        .width(maxWidth),
                    offset = DpOffset(x = 0.dp, y = (8).dp),
                    shadowElevation = 10.dp,
                    shape = RoundedCornerShape(8.dp),
                    containerColor = KarikaColors.White,
                    expanded = true,
                    onDismissRequest = {
                        dropdownState.negate()
                    }
                ) {
                    component.stateHolder.commonHandler.config.value.unitOptions
                        .forEach {
                            DropdownMenuItem(
                                onClick = {
                                    dropdownState.negate()
                                    component.minQtyUnit.value = it.label()
                                },
                                text = {
                                    KarikaText(
                                        modifier = Modifier,
                                        text = it.label(),
                                        color = KarikaColors.Gray4,
                                        textSize = 16.sp,
                                        fontWeight = FontWeight.W400
                                    )
                                }
                            )
                        }
                }
            }
        }
    }
    KarikaTextField1(
        modifier = Modifier
            .fillMaxWidth(),
        title = "Dostupna količina artikala",
        value = availableQty,
        placeholder = "Dostupna količina artikala",
        keyboardType = KeyboardType.Number,
        allowedChars = KarikaConstants.numbers.plus(","),
        imeAction = ImeAction.Next
    )
}

@Composable
private fun CategoriesBox(component: ProductDetailsComponent) {
    val categories = component.categories.asState()
    val showCategoryModal = mutableStateOf(false).asState()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .weight(1f),
            text = "Kategorije",
            color = KarikaColors.Gray4,
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
        KarikaText(
            modifier = Modifier
                .onClick {
                    showCategoryModal.negate()
                },
            text = "Dodaj",
            color = KarikaColors.Blue,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
    FlowRow(
        modifier = Modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        if (categories.value.isEmpty()) {
            KarikaText(
                modifier = Modifier
                    .onClick {
                        showCategoryModal.negate()
                    }
                    .fillMaxWidth(),
                text = "Dodaj kategoriju",
                color = KarikaColors.Blue,
                textSize = 16.sp,
                fontWeight = FontWeight.W600,
                textAlign = TextAlign.Center
            )
        }
        categories.value.forEach {
            Box(
                modifier = Modifier
                    .rounded(color = KarikaColors.Gray19)
            ) {
                IconTextItem(
                    modifier = Modifier
                        .onClick {
                            categories.value -= it
                        }
                        .padding(4.dp),
                    icon = vectorResource(Res.drawable.ic_tertiary),
                    iconColor = KarikaColors.Black1,
                    iconSize = 16.dp,
                    text = it.name,
                    textColor = KarikaColors.Gray2,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700,
                    iconPosition = FabPosition.End
                )
            }
        }
    }
    if (showCategoryModal.value) {
        ChooseCategoryModal(
            categories = component.stateHolder.commonHandler.categories.value,
            selectedCategories = categories.value,
            onSubmit = {
                categories.value = it
                showCategoryModal.negate()
            },
            onCancel = {
                showCategoryModal.negate()
            }
        )
    }
}

@Composable
private fun SwitchBox(component: ProductDetailsComponent) {
    val onlyKarika = component.onlyKarika.asState()
    val availableMessages = component.availableMessages.asState()
    KarikaSwitch1(
        title = "Prikaži u sekciji: “Samo na karici”",
        checked = onlyKarika
    )
    KarikaSwitch1(
        title = "Dozvoli kupcu da pošalje poruku za ovaj proizvod",
        checked = availableMessages
    )
}

@Composable
private fun DescBox(component: ProductDetailsComponent) {
    val shortDesc = component.shortDesc.asState()
    val longDesc = component.longDesc.asState()

    KarikaTextField1(
        modifier = Modifier
            .heightIn(min = 120.dp)
            .fillMaxWidth(),
        title = "Kratki opis artikla",
        value = shortDesc,
        placeholder = "Kratki opis artikla",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
    KarikaTextField1(
        modifier = Modifier
            .heightIn(min = 120.dp)
            .fillMaxWidth(),
        title = "Opis artikla",
        value = longDesc,
        placeholder = "Opis artikla",
        keyboardType = KeyboardType.Text,
        imeAction = ImeAction.Next
    )
}

@Composable
private fun ButtonsBox(component: ProductDetailsComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxWidth()
    ) {
        HorizontalSecondaryButtons(
            modifier = Modifier
                .padding(16.dp),
            primaryTitle = "Spasi artikal",
            secondaryTitle = "Nazad"
        ) {
            if (it == "Nazad") {
                component.dashBack()
                return@HorizontalSecondaryButtons
            }

            component.save()
        }
    }
}

fun Modifier.dashedBorder(
    color: Color,
    strokeWidth: Dp = 2.dp,
    cornerRadius: Dp = 12.dp,
    on: Dp = 8.dp,
    off: Dp = 4.dp
) = this.then(
    Modifier.drawWithCache {
        val strokePx = strokeWidth.toPx()
        val dash = floatArrayOf(on.toPx(), off.toPx())
        val effect = PathEffect.dashPathEffect(dash, 0f)
        val radiusPx = cornerRadius.toPx()

        onDrawBehind {
            drawRoundRect(
                color = color,
                style = Stroke(width = strokePx, pathEffect = effect),
                cornerRadius = CornerRadius(radiusPx, radiusPx)
            )
        }
    }
)

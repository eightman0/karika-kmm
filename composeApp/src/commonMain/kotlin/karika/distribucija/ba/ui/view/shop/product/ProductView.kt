package karika.distribucija.ba.ui.view.shop.product

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.CommonComponent
import karika.distribucija.ba.ui.common.HtmlTextWithStyles
import karika.distribucija.ba.ui.components.KBottomPanel
import karika.distribucija.ba.ui.components.KCard
import karika.distribucija.ba.ui.components.KCircleButton
import karika.distribucija.ba.ui.components.KIcon
import karika.distribucija.ba.ui.components.KInitials
import karika.distribucija.ba.ui.components.KPill
import karika.distribucija.ba.ui.components.KPrimaryButton
import karika.distribucija.ba.ui.components.KSectionTitle
import karika.distribucija.ba.ui.components.KTonalButton
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaIntTextField
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.KarikaUiColors
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.isTabletLandscape
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.shop.home.DiscountView
import karika.distribucija.ba.ui.view.shop.home.NewView
import karika.distribucija.ba.ui.view.shop.home.NotAvailableOverlay
import karika.distribucija.ba.ui.view.shop.home.ProductItem
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_k_cart
import karikav2.composeapp.generated.resources.ic_k_chat
import karikav2.composeapp.generated.resources.ic_k_chevron_down
import karikav2.composeapp.generated.resources.ic_k_chevron_left
import karikav2.composeapp.generated.resources.ic_k_chevron_right
import karikav2.composeapp.generated.resources.ic_k_gift
import karikav2.composeapp.generated.resources.ic_k_minus
import karikav2.composeapp.generated.resources.ic_k_plus
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.vectorResource

/** Test tag of the "+" of the quantity stepper in the bottom panel of the product details. */
const val PRODUCT_QTY_PLUS_TAG = "product_qty_plus"

private val SheetShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
private val BonusText = Color(0xFF166534)
private val BonusBackground = Color(0xFFF0FDF4)
private val GrayPill = Color(0xFFF3F4F6)
private val PhoneImageHeight = 340.dp
private val SheetOverlap = 22.dp

@Composable
fun ProductView(component: ProductComponent) {
    val product by component.product.collectAsState()

    key(product.hashCode()) {
        KarikaScaffold(
            containerColor = KarikaUiColors.Page,
            contentWindowInsets = WindowInsets.systemBars,
            component = component,
            topBar = {
                // White behind the status bar when shown at the app root (inside the shell it is already consumed)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(KarikaColors.White)
                        .windowInsetsPadding(WindowInsets.statusBars)
                )
            },
            bottomBar = {
                if (product.createdAt != null) {
                    ProductBottomPanel(component)
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(KarikaUiColors.Page)
                    .padding(it)
                    .verticalScroll(rememberScrollState())
                    .hideKeyboard()
            ) {
                if (product.createdAt == null) {
                    return@KarikaScaffold
                }

                if (isTabletLandscape()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ProductGallery(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(16.dp)),
                            component = component
                        )
                        Column(
                            modifier = Modifier
                                .weight(2f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(KarikaColors.White)
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 18.dp)
                        ) {
                            ProductSummary(component)
                        }
                    }
                } else {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ProductGallery(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(PhoneImageHeight),
                            component = component
                        )
                        Column(
                            modifier = Modifier
                                .padding(top = PhoneImageHeight - SheetOverlap)
                                .fillMaxWidth()
                                .clip(SheetShape)
                                .background(KarikaColors.White)
                                .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 18.dp)
                        ) {
                            ProductSummary(component)
                        }
                    }
                }

                ProductDescriptionCard(component)
                VendorProducts(component)
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

/** Image pager with the floating back and chat buttons and the pink page indicator. */
@Composable
private fun ProductGallery(modifier: Modifier, component: ProductComponent) {
    val product by component.product.collectAsState()
    val images = product.getImages()
    val pagerState = rememberPagerState { images.size }

    Box(
        modifier = modifier
            .background(KarikaColors.White)
    ) {
        Box(
            modifier = Modifier
                .blur(radius = if (product.hasOnStock()) 0.dp else 5.dp)
                .fillMaxSize()
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxSize()
            ) { page ->
                KarikaImage(
                    modifier = Modifier
                        .onClick {
                            component.showImagesPreview(images, page)
                        }
                        .fillMaxSize(),
                    model = images[page],
                    // Fill the gallery like the vendor cards, so the floating buttons sit on the photo
                    contentScale = ContentScale.Crop
                )
            }
        }
        NotAvailableOverlay(product)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = SheetOverlap)
        ) {
            DiscountView(product)
            NewView(product)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KCircleButton(
                modifier = Modifier.shadow(6.dp, CircleShape),
                icon = vectorResource(Res.drawable.ic_k_chevron_left),
                background = KarikaColors.White
            ) {
                component.back()
            }
            Spacer(modifier = Modifier.weight(1f))
            KCircleButton(
                modifier = Modifier.shadow(6.dp, CircleShape),
                icon = vectorResource(Res.drawable.ic_k_chat),
                background = KarikaColors.White
            ) {
                component.sendMessageToVendor(product)
            }
        }
        if (images.size > 1) {
            ProductPagerIndicator(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = SheetOverlap + 8.dp),
                pageCount = images.size,
                currentPage = pagerState.currentPage
            )
        }
    }
}

@Composable
private fun ProductPagerIndicator(modifier: Modifier, pageCount: Int, currentPage: Int) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(pageCount) { page ->
            Box(
                modifier = Modifier
                    .height(5.dp)
                    .width(if (page == currentPage) 16.dp else 5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (page == currentPage) KarikaUiColors.Pink else KarikaUiColors.Border)
            )
        }
    }
}

/** Vendor, name, price, stock and minimum quantity, bonus and the message action. */
@Composable
private fun ProductSummary(component: ProductComponent) {
    val product by component.product.collectAsState()
    val unit = component.getUnit(product.minQtyUnit())

    ProductVendorRow(product, component)
    KarikaText(
        modifier = Modifier.padding(top = 10.dp),
        text = product.name(),
        color = KarikaUiColors.Ink,
        textSize = 22.sp,
        lineHeight = 27.sp,
        fontWeight = FontWeight.W700
    )

    Row(
        modifier = Modifier.padding(top = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        KarikaText(
            modifier = Modifier.alignByBaseline(),
            text = if (product.hasSpecialPrice()) product.specialPriceString() else product.originalPriceString(),
            color = if (product.hasSpecialPrice()) KarikaUiColors.Pink else KarikaUiColors.Ink,
            textSize = 27.sp,
            lineHeight = 32.sp,
            fontWeight = FontWeight.W700,
            maxLines = 1
        )
        KarikaText(
            modifier = Modifier.alignByBaseline(),
            text = "/ $unit",
            color = KarikaUiColors.Subtle,
            textSize = 13.sp,
            maxLines = 1
        )
        if (product.hasSpecialPrice()) {
            KarikaText(
                modifier = Modifier.alignByBaseline(),
                text = product.originalPriceString(),
                color = KarikaUiColors.Subtle,
                textSize = 15.sp,
                fontWeight = FontWeight.W500,
                maxLines = 1,
                decoration = TextDecoration.LineThrough
            )
        }
    }

    if (product.hasMpc()) {
        Row(
            modifier = Modifier.padding(top = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KarikaText(
                text = "Preporučena MPC:",
                color = KarikaUiColors.Muted,
                textSize = 13.sp
            )
            KarikaText(
                text = product.mpcString(),
                color = KarikaUiColors.Ink,
                textSize = 13.sp,
                fontWeight = FontWeight.W600
            )
        }
    }

    Row(
        modifier = Modifier.padding(top = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (product.hasOnStock()) {
            KPill(
                text = product.isInStockLabel(),
                background = KarikaUiColors.GreenSoft,
                color = KarikaUiColors.Green,
                dot = KarikaUiColors.Green
            )
        } else {
            KPill(
                text = product.isInStockLabel(),
                background = KarikaUiColors.RedSoft,
                color = KarikaUiColors.Red,
                dot = KarikaUiColors.Red
            )
        }
        KPill(
            text = "Min. količina ${product.minQty()} $unit",
            background = GrayPill,
            color = KarikaUiColors.Muted
        )
    }

    if (product.hasBonus()) {
        Row(
            modifier = Modifier
                .padding(top = 14.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(BonusBackground)
                .border(1.dp, KarikaUiColors.GreenSoft, RoundedCornerShape(12.dp))
                .padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            KIcon(
                icon = vectorResource(Res.drawable.ic_k_gift),
                tint = KarikaUiColors.Green,
                size = 20.dp
            )
            KarikaText(
                modifier = Modifier.weight(1f),
                text = "Bonus za kupovinu proizvoda",
                color = BonusText,
                textSize = 13.sp,
                fontWeight = FontWeight.W500
            )
            KarikaText(
                text = product.bonusString(),
                color = BonusText,
                textSize = 14.sp,
                fontWeight = FontWeight.W700,
                maxLines = 1
            )
        }
    }

    KTonalButton(
        modifier = Modifier
            .padding(top = 14.dp)
            .fillMaxWidth(),
        text = "Pošalji poruku dobavljaču",
        icon = vectorResource(Res.drawable.ic_k_chat)
    ) {
        component.sendMessageToVendor(product)
    }
}

/** Pink vendor link with initials, hidden for guests (they cannot open vendors). */
@Composable
private fun ProductVendorRow(product: Product, component: CommonComponent) {
    if (component.isGuest()) {
        return
    }
    Row(
        modifier = Modifier
            .onClick {
                component.showVendor(product.toVendor())
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KInitials(
            name = product.vendorName(),
            size = 24.dp,
            shape = RoundedCornerShape(7.dp),
            textSize = 9.sp
        )
        KarikaText(
            modifier = Modifier.weight(1f, fill = false),
            text = product.vendorName(),
            color = KarikaUiColors.Pink,
            textSize = 13.sp,
            fontWeight = FontWeight.W500,
            maxLines = 1
        )
        KIcon(
            icon = vectorResource(Res.drawable.ic_k_chevron_right),
            tint = KarikaUiColors.Pink,
            size = 14.dp
        )
    }
}

@Composable
private fun ProductDescriptionCard(component: ProductComponent) {
    val product by component.product.collectAsState()
    if (product.description.isNullOrEmpty()) {
        return
    }
    var showDescription by remember { mutableStateOf(false) }

    KCard(
        modifier = Modifier
            .padding(start = 16.dp, end = 16.dp, top = 16.dp)
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (!product.shortDescription.isNullOrEmpty()) {
                HtmlTextWithStyles(
                    html = product.shortDescription ?: "",
                    textColor = KarikaUiColors.Muted
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDescription = !showDescription },
                verticalAlignment = Alignment.CenterVertically
            ) {
                KarikaText(
                    modifier = Modifier.weight(1f),
                    text = "Opis proizvoda",
                    color = KarikaUiColors.Ink,
                    textSize = 16.sp,
                    fontWeight = FontWeight.W700
                )
                KIcon(
                    modifier = Modifier.rotate(if (showDescription) 180f else 0f),
                    icon = vectorResource(Res.drawable.ic_k_chevron_down),
                    tint = KarikaUiColors.Muted,
                    size = 20.dp
                )
            }
            if (showDescription) {
                HtmlTextWithStyles(
                    html = product.description ?: "",
                    textColor = KarikaUiColors.Ink
                )
            }
        }
    }
}

@Composable
private fun VendorProducts(component: ProductComponent) {
    val product by component.product.collectAsState()
    val products by component.products.collectAsState()
    if (products.size > 1) {
        KSectionTitle(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 22.dp, bottom = 12.dp),
            title = "Proizvodi istog dobavljača",
            actionText = if (component.isGuest()) null else "Svi",
            onAction = { component.showVendor(product.toVendor()) }
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            products.forEach {
                Box(modifier = Modifier.width(150.dp)) {
                    ProductItem(it, component)
                }
            }
        }
    }
}

/** Bottom panel: quantity stepper (steps by the minimum quantity) and "Dodaj u korpu". */
@Composable
private fun ProductBottomPanel(component: ProductComponent) {
    val product by component.product.collectAsState()

    KBottomPanel(
        modifier = Modifier
            .shadow(elevation = 12.dp, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (product.hasOnStock()) {
                ProductQtyStepper(product, component.productQty)
            }
            KPrimaryButton(
                modifier = Modifier.weight(1f),
                text = "Dodaj u korpu",
                height = 50.dp,
                icon = vectorResource(Res.drawable.ic_k_cart),
                enabled = product.hasOnStock()
            ) {
                component.addToCartWithPut(product, component.productQty.value)
            }
        }
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
    }
}

@Composable
private fun ProductQtyStepper(product: Product, qty: MutableState<Int>) {
    Row(
        modifier = Modifier
            .height(50.dp)
            .clip(RoundedCornerShape(50))
            .background(KarikaUiColors.PinkSoft)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(KarikaColors.White)
                .clickable {
                    if (qty.value != product.minQty()) {
                        qty.value -= product.minQty()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_minus), tint = KarikaUiColors.Pink, size = 18.dp)
        }
        Box(
            modifier = Modifier
                .widthIn(min = 40.dp, max = 72.dp)
                .width(IntrinsicSize.Min),
            contentAlignment = Alignment.Center
        ) {
            KarikaIntTextField(
                value = qty,
                minValue = product.minQty(),
                onValueChange = {
                    if (it == qty.value) {
                        return@KarikaIntTextField
                    }

                    val entered = it ?: qty.value
                    val min = product.minQty()

                    val adjusted = if (entered <= min) {
                        min
                    } else {
                        val remainder = entered % min
                        if (remainder == 0) {
                            entered
                        } else {
                            entered + (min - remainder)
                        }
                    }

                    qty.value = adjusted
                }
            )
        }
        Box(
            modifier = Modifier
                .testTag(PRODUCT_QTY_PLUS_TAG)
                .size(42.dp)
                .clip(CircleShape)
                .background(KarikaUiColors.Pink)
                .clickable {
                    qty.value += product.minQty()
                },
            contentAlignment = Alignment.Center
        ) {
            KIcon(icon = vectorResource(Res.drawable.ic_k_plus), tint = KarikaColors.White, size = 18.dp)
        }
    }
}

@Composable
fun VendorName(product: Product, component: CommonComponent) {
    if (component.isGuest()) {
        return
    }
    Row(
        modifier = Modifier
            .onClick {
                component.showVendor(product.toVendor())
            }
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Blue,
            text = product.vendorName(),
            textSize = 14.sp,
            maxLines = 1,
            fontWeight = FontWeight.W600,
            decoration = TextDecoration.Underline
        )
    }
}

@Composable
fun ProductQtyAction(
    product: Product,
    qty: MutableState<Int>,
    component: CommonComponent,
    disableUpdate: Boolean = true,
    autoUpdate: Boolean = false
) {
    var pendingQty by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(pendingQty) {
        pendingQty?.let { newQty ->
            delay(600)
            if (!disableUpdate) {
                component.updateCart(product, newQty)
            }
            pendingQty = null
        }
    }

    Row {
        Box(
            modifier = Modifier
                .onClick {
                    if (qty.value == product.minQty()) {
                        return@onClick
                    }
                    qty.value -= product.minQty()
                    pendingQty = qty.value
                }
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border,
                    shape = RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "-",
                textSize = 22.sp,
                fontWeight = FontWeight.W600
            )
        }
        Box(
            modifier = Modifier
                .height(40.dp)
                .width(IntrinsicSize.Min)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaIntTextField(
                value = qty,
                minValue = product.minQty(),
                onValueChange = {
                    if (it == qty.value) {
                        return@KarikaIntTextField
                    }

                    val entered = it ?: qty.value
                    val min = product.minQty()

                    val adjusted = if (entered <= min) {
                        min
                    } else {
                        val remainder = entered % min
                        if (remainder == 0) {
                            entered
                        } else {
                            entered + (min - remainder)
                        }
                    }


                    qty.value = adjusted
                    if (autoUpdate) {
                        pendingQty = qty.value
                    }
                }
            )
        }
        Box(
            modifier = Modifier
                .onClick {
                    qty.value += product.minQty()
                    pendingQty = qty.value
                }
                .size(40.dp)
                .border(
                    width = 1.dp,
                    color = KarikaColors.Border,
                    shape = RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = "+",
                textSize = 22.sp,
                fontWeight = FontWeight.W600
            )
        }
    }
}

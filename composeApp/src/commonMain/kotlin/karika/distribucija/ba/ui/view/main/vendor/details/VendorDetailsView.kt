package karika.distribucija.ba.ui.view.main.vendor.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Product
import karika.distribucija.ba.ui.common.openPhoneCall
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaLazyColumn
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.PrimaryButton
import karika.distribucija.ba.ui.components.RoundedItem
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.bgWhite
import karika.distribucija.ba.ui.components.gridColumnCount
import karika.distribucija.ba.ui.components.hideKeyboard
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.main.home.ProductItem
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_email
import karikav2.composeapp.generated.resources.ic_location
import karikav2.composeapp.generated.resources.ic_phone
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VendorDetailsView(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()

    key(vendor.hashCode()) {
        KarikaScaffold(
            containerColor = KarikaColors.White,
            contentWindowInsets = WindowInsets.systemBars,
            component = component,
            topBar = {
                TopBarWithBack(vendor.name()) {
                    component.mainBack()
                }
            }
        ) {
            Column(
                modifier = Modifier
                    .bgWhite()
                    .padding(16.dp)
                    .fillMaxSize()
                    .padding(it)
            ) {
                VendorProducts(modifier = Modifier.weight(1f), component)
            }
        }
    }
}

@Composable
private fun VendorProducts(modifier: Modifier, component: VendorDetailsComponent) {
    val products = component.products.collectAsState()
    val state = rememberLazyListState()
    val searchText = component.searchText.asState()
    val gridColumnCount = gridColumnCount()
    Box(contentAlignment = Alignment.Center) {
        KarikaLazyColumn(
            modifier = modifier
                .hideKeyboard(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            state = state
        ) {
            item {
                VendorImage(component)
                YSpacer16()
                VendorInfo(component)
                YSpacer16()
                VendorCategories(component)
                YSpacer16()
                SearchBoxBorder(
                    modifier = Modifier
                        .fillMaxWidth(),
                    onValueChange = { text ->
                        searchText.value = text
                    },
                    onClose = {
                        searchText.value = ""
                        component.loadNextPage(true)
                    },
                    onSearchExecute = {
                        if (searchText.value.length > 2) {
                            component.loadNextPage(true)
                        }
                    }
                )
                YSpacer16()
            }
            items(items = products.value.chunked(gridColumnCount)) { item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item.forEach {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        ) {
                            ProductItem(it, component, true)
                        }
                    }
                    if (item.size == 1) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                        )
                    }
                }
            }
            item { EmptyState(component) }
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }
}

@Composable
private fun VendorCategories(component: VendorDetailsComponent) {
    val categories by component.vendorCategories.collectAsState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(items = categories) {
            RoundedItem(it.name, component.selectedCategories.value.contains(it)) {
                if (component.selectedCategories.value.contains(it)) {
                    component.selectedCategories.value -= it
                } else {
                    component.selectedCategories.value += it
                }
                component.loadNextPage(true)
            }
        }
    }
}

@Composable
private fun VendorInfo(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()

    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = vendor.name(),
        color = KarikaColors.Black,
        textSize = 20.sp,
        fontWeight = FontWeight.W600
    )
    //IconTextItem(
    //    modifier = Modifier,
    //    icon = vectorResource(Res.drawable.ic_email),
    //    iconColor = KarikaColors.Gray2,
    //    textColor = KarikaColors.Gray2,
    //    textSize = 14.sp,
    //    fontWeight = FontWeight.W400,
    //    text = vendor.email,
    //    iconPosition = FabPosition.Start
    //)
    //IconTextItem(
    //    modifier = Modifier
    //        .onClick {
    //            openPhoneCall(vendor.b2bVendorPhone ?: return@onClick)
    //        },
    //    icon = vectorResource(Res.drawable.ic_phone),
    //    iconColor = KarikaColors.Gray2,
    //    textColor = KarikaColors.Gray2,
    //    textSize = 14.sp,
    //    fontWeight = FontWeight.W400,
    //    text = vendor.b2bVendorPhone,
    //    iconPosition = FabPosition.Start
    //)
    IconTextItem(
        modifier = Modifier,
        icon = vectorResource(Res.drawable.ic_location),
        iconColor = KarikaColors.Gray2,
        textColor = KarikaColors.Gray2,
        textSize = 14.sp,
        fontWeight = FontWeight.W400,
        text = vendor.address,
        iconPosition = FabPosition.Start
    )
}

@Composable
private fun VendorImage(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()

    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .onClick {}
                .size(100.dp)
                .border(width = 1.dp, color = KarikaColors.Gray5)
        ) {
            KarikaImage(
                modifier = Modifier
                    .onClick {
                        component.showImagePreview(vendor.image())
                    }
                    .fillMaxSize(),
                model = vendor.image()
            )
        }
        PrimaryButton(
            modifier = Modifier
                .height(48.dp),
            title = "Pošalji poruku dobavljaču",
        ) {
            component.sendMessageToVendor(
                Product(vendorId = vendor.entityId.toString(), vendorName = vendor.publicName)
            )
        }
    }
}

@Composable
private fun BreadCrumbs(component: VendorDetailsComponent) {
    val vendor by component.vendor.collectAsState()
    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = vendor.breadCrumbs(),
        color = KarikaColors.Black,
        textSize = 14.sp,
        fontWeight = FontWeight.W400
    )
}

@Composable
private fun EmptyState(component: VendorDetailsComponent) {
    val vendors by component.products.collectAsState()
    val loader by component.stateHolder.loaderHandler.loader.collectAsState()
    if (vendors.isEmpty() && !loader) {
        Box(
            modifier = Modifier
                .height(200.dp)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Primary,
                textSize = 16.sp,
                fontWeight = FontWeight.W700,
                text = "Nema rezultata."
            )
        }
    }
}
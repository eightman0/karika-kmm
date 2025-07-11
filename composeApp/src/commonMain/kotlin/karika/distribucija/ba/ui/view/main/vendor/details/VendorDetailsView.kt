package karika.distribucija.ba.ui.view.main.vendor.details

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.bgWhite
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
    val searchText = component.searchText.asState()

    key(vendor.hashCode()) {
        KarikaScaffold(
            containerColor = KarikaColors.White,
            contentWindowInsets = WindowInsets.systemBars,
            component = component,
            topBar = {
                TopBarWithBack(vendor.name()) {
                    component.appBack()
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
                VendorImage(component)
                YSpacer16()
                VendorInfo(component)
                KarikaText(
                    modifier = Modifier
                        .padding(vertical = 16.dp),
                    color = KarikaColors.Black,
                    text = "Najprodavaniji proizvodi:",
                    textSize = 20.sp,
                    fontWeight = FontWeight.W700
                )
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
                        component.loadNextPage(true)
                    }
                )
                YSpacer16()
                VendorProducts(modifier = Modifier.weight(1f), component)
            }
        }
    }


}

@Composable
private fun VendorProducts(modifier: Modifier, component: VendorDetailsComponent) {
    val products = component.products.collectAsState()
    val state = rememberLazyGridState()

    LazyVerticalGrid(
        modifier = modifier,
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = state
    ) {
        items(items = products.value) { item ->
            ProductItem(item, component)
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
    /*  val vendor by component.vendor.collectAsState()
    val products by component.products.collectAsState()

    LazyRow(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            RoundedItem("Brašno i proizvodi od brašna")
        }
        item {
            RoundedItem("Mliječni proizvodi")
        }
        item {
            RoundedItem("Lisnato tijesto")
        }
    }*/
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
    IconTextItem(
        modifier = Modifier,
        icon = vectorResource(Res.drawable.ic_email),
        iconColor = KarikaColors.Gray2,
        textColor = KarikaColors.Gray2,
        textSize = 14.sp,
        fontWeight = FontWeight.W400,
        text = vendor.email,
        iconPosition = FabPosition.Start
    )
    IconTextItem(
        modifier = Modifier,
        icon = vectorResource(Res.drawable.ic_phone),
        iconColor = KarikaColors.Gray2,
        textColor = KarikaColors.Gray2,
        textSize = 14.sp,
        fontWeight = FontWeight.W400,
        text = vendor.b2bVendorPhone,
        iconPosition = FabPosition.Start
    )
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

    Box(
        modifier = Modifier
            .onClick {}
            .size(100.dp)
            .border(width = 1.dp, color = KarikaColors.Gray5)
    ) {
        KarikaImage(
            modifier = Modifier
                .fillMaxSize(),
            model = vendor.image()
        )
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
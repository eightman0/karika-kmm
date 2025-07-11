package karika.distribucija.ba.ui.view.main.vendor

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FabPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Vendor
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView
import karika.distribucija.ba.ui.components.SearchBoxBorder
import karika.distribucija.ba.ui.components.asState
import karika.distribucija.ba.ui.components.negate
import karika.distribucija.ba.ui.components.onClick
import karika.distribucija.ba.ui.view.main.vendor.details.filter.FilterSheet
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_filter_alt
import org.jetbrains.compose.resources.vectorResource

@Composable
fun VendorView(viewModel: VendorComponent) {
    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Vendors(viewModel)
        }
    }

    LoadingView(viewModel)
}

@Composable
private fun Vendors(component: VendorComponent) {
    val vendors by component.vendors.collectAsState()
    val state = rememberLazyGridState()

    KarikaText(
        modifier = Modifier,
        color = KarikaColors.Black,
        text = "DOBAVLJAČI",
        textSize = 20.sp,
        fontWeight = FontWeight.W700
    )
    Filter(component)
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        state = state
    ) {
        items(
            items = vendors.toList()
        ) {
            VendorItem(it, component)
        }
    }

    LaunchedEffect(state.canScrollForward) {
        if (!state.canScrollForward) {
            component.loadNextPage()
        }
    }
}

@Composable
private fun VendorItem(vendor: Vendor, component: VendorComponent) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .onClick {
                    component.showVendor(vendor)
                }
                .fillMaxWidth()
                .border(width = 1.dp, color = KarikaColors.Gray5)
                .aspectRatio(1f),
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = vendor.image()
            )
        }
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Black,
            text = vendor.name(),
            textSize = 14.sp,
            fontWeight = FontWeight.W400,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun Filter(component: VendorComponent) {
    val showState = remember { mutableStateOf(false) }
    val filter = component.filter.asState()
    val sort = component.sort.asState()
    val searchText = component.searchText.asState()

    SearchBoxBorder(
        modifier = Modifier
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        onValueChange = {
            searchText.value = it
        },
        onClose = {
            searchText.value = ""
            component.loadNextPage(true)
        },
        onSearchExecute = {
            component.loadNextPage(true)
        },
        placeholder = "Pretraži dobavljače.."
    )
    Row(
        modifier = Modifier
            .height(32.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .onClick {
                    showState.negate()
                }
                .fillMaxHeight()
                .weight(1f)
                .border(width = 1.dp, color = KarikaColors.Gray1, shape = RoundedCornerShape(4.dp)),
            contentAlignment = Alignment.Center
        ) {
            IconTextItem(
                modifier = Modifier,
                icon = vectorResource(Res.drawable.ic_filter_alt),
                iconColor = KarikaColors.Black1,
                iconSize = 16.dp,
                text = "Filteri",
                textColor = KarikaColors.Gray2,
                textSize = 16.sp,
                fontWeight = FontWeight.W400,
                iconPosition = FabPosition.End
            )
        }
        // KarikaPickerSmall(
        //     modifier = Modifier
        //         .weight(1f),
        //     padding = 4.dp,
        //     borderColor = KarikaColors.Gray2,
        //     value = sort,
        //     values = mutableStateOf(listOf("A - Z", "Z - A")).asState()
        // ) {
        //     component.loadNextPage(reset = true)
        // }
    }
    if (filter.value.first.isNotBlank()) {
        KarikaText(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Black,
            atext = buildAnnotatedString {
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W400,
                        color = KarikaColors.Gray1,
                        fontSize = 16.sp
                    )
                ) {
                    append("Uključeni filter: ")
                }
                withStyle(
                    style = SpanStyle(
                        fontWeight = FontWeight.W600,
                        color = KarikaColors.Gray2,
                        fontSize = 16.sp
                    )
                ) {
                    append(filter.value.third.ifBlank { filter.value.second })
                }
            }
        )
    }

    FilterSheet(showState) { filterBy, filterName, filterValue ->
        component.filter.value = Triple(filterBy, filterValue, filterName)
        component.loadNextPage(reset = true)
    }
}
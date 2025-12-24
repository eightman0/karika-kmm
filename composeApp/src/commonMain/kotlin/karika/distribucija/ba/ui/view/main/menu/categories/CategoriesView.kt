package karika.distribucija.ba.ui.view.main.menu.categories

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.EndIconTextItem
import karika.distribucija.ba.ui.components.IconTextItem
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_arrow_right
import org.jetbrains.compose.resources.vectorResource

@Composable
fun CategoriesView(component: CategoriesComponent) {
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Kategorije proizvoda") {
                component.mainBack()
            }
        },
        component = component
    ) {
        Column(
            modifier = Modifier
                .padding(it)
        ) {
            YSpacer16()
            Categories(component)
            YSpacer16()
        }
    }
}

@Composable
private fun Categories(component: CategoriesComponent) {
    val categories by component.categories.collectAsState()
    val category by component.subCategory.collectAsState()

    if (category != null) {
        IconTextItem(
            modifier = Modifier
                .padding(start = 8.dp, end = 16.dp)
                .onClick {
                    component.reset()
                },
            icon = vectorResource(Res.drawable.ic_arrow_back),
            iconColor = KarikaColors.Gray2,
            textColor = KarikaColors.Gray2,
            text = "Kategorije proizvoda",
            fontWeight = FontWeight.W400,
            textSize = 14.sp,
            iconPosition = FabPosition.Start
        )
        YSpacer16()
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Divider,
            thickness = 1.dp
        )
        YSpacer16()
        KarikaText(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = category?.name,
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            fontWeight = FontWeight.W700,
        )
        YSpacer16()
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            color = KarikaColors.Divider,
            thickness = 1.dp
        )
        YSpacer16()
        KarikaText(
            modifier = Modifier
                .onClick {
                    component.showProducts(category)
                }
                .padding(horizontal = 16.dp),
            text = "Vidi sve u ${category?.name?.lowercase()}",
            color = KarikaColors.Gray6,
            textSize = 14.sp,
            decoration = TextDecoration.Underline,
            fontWeight = FontWeight.W600,
        )
        YSpacer16()
    }
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
    ) {
        items(items = categories.find { it.id == category?.id }?.childrenData ?: categories) {
            Button(
                onClick = {
                    if (category != null || it.childrenData.isEmpty()) {
                        component.showProducts(it)
                    } else {
                        component.onSelectCategory(it)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = KarikaColors.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                EndIconTextItem(
                    modifier = Modifier
                        .fillMaxWidth(),
                    //icon = if (it.childrenData.isEmpty()) null else vectorResource(Res.drawable.ic_arrow_right),
                    icon = if (category != null) null else vectorResource(Res.drawable.ic_arrow_right),
                    iconColor = KarikaColors.Gray3,
                    textColor = KarikaColors.Gray3,
                    textSize = 14.sp,
                    fontWeight = FontWeight.W600,
                    text = it.name
                )
            }
        }
    }
}
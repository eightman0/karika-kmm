package karika.distribucija.ba.ui.view.shop.menu.blog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.domain.model.Blog
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaImage
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TopBarWithBack
import karika.distribucija.ba.ui.components.isTabletLandscape
import karika.distribucija.ba.ui.components.onClick

@Composable
fun BlogsView(component: BlogsComponent) {
    val blogs by component.blogs.collectAsState()
    val columns = if (isTabletLandscape()) 2 else 1
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.systemBars,
        topBar = {
            TopBarWithBack("Svi blog članci") {
                component.appBack()
            }
        },
        component = component
    ) {
        LazyColumn(
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(items = blogs.chunked(columns)) { items ->
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    items.forEach { item ->
                        Box(modifier = Modifier.weight(1f)) {
                            BlogItem(blog = item, component = component)
                        }
                    }
                    if (items.size < columns) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun BlogItem(blog: Blog, component: BlogsComponent) {
    Row(
        modifier = Modifier
            .onClick {
                component.navigateToBlog(blog)
            }
            .border(width = 1.dp, color = KarikaColors.Gray5)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxSize(),
                model = blog.image()
            )
        }
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray18,
                text = blog.date?.split(" ")?.first(),
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = blog.title,
                textSize = 16.sp,
                fontWeight = FontWeight.W500
            )
            KarikaText(
                modifier = Modifier,
                color = KarikaColors.Gray2,
                text = blog.desc,
                textSize = 14.sp,
                maxLines = 2,
                fontWeight = FontWeight.W400
            )
        }
    }
}
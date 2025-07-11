package karika.distribucija.ba.ui.view.main.menu.blog

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import karika.distribucija.ba.ui.components.YSpacer16
import karika.distribucija.ba.ui.components.onClick

@Composable
fun BlogsView(component: BlogsComponent) {
    val blogs by component.blogs.collectAsState()

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
            items(items = blogs) { item ->
                BlogItem(item, component)
                YSpacer16()
            }
        }
    }
}

@Composable
private fun BlogItem(blog: Blog, component: BlogsComponent) {
    Column(
        modifier = Modifier
            .onClick {
                component.navigateToBlog(blog)
            }
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.5f)
                .border(width = 1.dp, color = KarikaColors.Gray5)
        ) {
            KarikaImage(
                modifier = Modifier
                    .fillMaxWidth(),
                model = blog.image()
            )
        }

        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Gray18,
            text = blog.date,
            textSize = 12.sp,
            fontWeight = FontWeight.W300
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
            textSize = 16.sp,
            fontWeight = FontWeight.W400
        )
    }
}
package karika.distribucija.ba.ui.view.main.menu

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.TextArrowItem
import karika.distribucija.ba.ui.components.TextItem
import karika.distribucija.ba.ui.components.YSpacer16

@Composable
fun MenuView(viewModel: MenuViewModel) {
    Column(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                color = KarikaColors.Gray2,
                text = "MENI",
                fontWeight = FontWeight.W700,
                textSize = 16.sp
            )
        }
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth(),
            thickness = 1.dp,
            color = KarikaColors.Divider
        )
        YSpacer16()
        TextArrowItem("Kategorije proizvoda", viewModel::categories)
        TextItem("Dobavljači", viewModel::vendors)
        TextItem("Blog", viewModel::blog)
        TextItem("Samo na Kariki", viewModel::karika)
    }
}
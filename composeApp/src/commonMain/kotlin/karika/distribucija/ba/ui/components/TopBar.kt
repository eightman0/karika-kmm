package karika.distribucija.ba.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.common.CommonViewModel
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_action
import karikav2.composeapp.generated.resources.ic_arrow_back
import karikav2.composeapp.generated.resources.ic_outlet
import org.jetbrains.compose.resources.vectorResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBarWithBack(viewModel: CommonViewModel) {
    TopAppBar(
        modifier = Modifier
            .fillMaxWidth(),
        title = {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = viewModel.title,
                color = KarikaColors.White,
                textSize = 20.sp,
                fontWeight = FontWeight.W700,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            Icon(
                modifier = Modifier
                    .onClick {
                        viewModel.back()
                    }
                    .padding(horizontal = 4.dp),
                imageVector = vectorResource(Res.drawable.ic_arrow_back),
                contentDescription = "",
                tint = KarikaColors.White
            )
        },
        actions = {

        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = KarikaColors.Primary
        ),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar() {
    Column {
        YSpacer8()
        TopAppBar(
            modifier = Modifier
                .fillMaxWidth(),
            title = {
                SearchBox(
                    modifier = Modifier
                        .padding(end = 16.dp)
                        .fillMaxWidth(),
                    onValueChange = {

                    },
                    onSearchExecute = {

                    }
                )
            },
            actions = {

            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = KarikaColors.Primary
            ),
        )
        YSpacer8()
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            color = KarikaColors.White,
            thickness = 1.dp
        )
        ActionBar()
    }
}

@Composable
fun ActionBar() {
    Row(
        modifier = Modifier
            .height(40.dp)
            .background(color = KarikaColors.Primary)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconTextItem(
            modifier = Modifier
                .padding(start = 16.dp),
            icon = vectorResource(Res.drawable.ic_outlet),
            iconColor = KarikaColors.White,
            textColor = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W600,
            text = "OUTLET"
        )
        IconTextItem(
            modifier = Modifier,
            icon = vectorResource(Res.drawable.ic_action),
            iconColor = KarikaColors.White,
            textColor = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W600,
            text = "AKCIJE"
        )
        KarikaText(
            modifier = Modifier
                .padding(end = 16.dp),
            text = "Kontaktirajte nas",
            color = KarikaColors.White,
            textSize = 16.sp,
            fontWeight = FontWeight.W600
        )
    }
}

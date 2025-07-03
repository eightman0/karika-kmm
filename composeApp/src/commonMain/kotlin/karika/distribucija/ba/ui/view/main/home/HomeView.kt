package karika.distribucija.ba.ui.view.main.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.LoadingView1
import karika.distribucija.ba.ui.components.asState

@Composable
fun HomeView(viewModel: HomeViewModel) {

    Box(
        modifier = Modifier
            .background(color = KarikaColors.White)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        KarikaText(
            modifier = Modifier,
            color = KarikaColors.Secondary,
            text = ""
        )

        LoadingView1(viewModel)
    }

    LaunchedEffect(Unit) {
        viewModel.loadData()
    }
}
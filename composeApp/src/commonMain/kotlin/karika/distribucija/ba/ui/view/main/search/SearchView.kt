package karika.distribucija.ba.ui.view.main.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaScaffold
import karika.distribucija.ba.ui.components.TopBarSearch
import karika.distribucija.ba.ui.view.main.home.ProductItem

@Composable
fun SearchView(component: SearchComponent) {
    val products by component.products.collectAsState()
    val state = rememberLazyGridState()
    KarikaScaffold(
        containerColor = KarikaColors.White,
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopBarSearch(component)
        },
        component = component
    ) {
        LazyVerticalGrid(
            state = state,
            modifier = Modifier
                .padding(it)
                .padding(16.dp),
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = products.toList()
            ) {
                ProductItem(it, component)
            }
        }

        LaunchedEffect(state.canScrollForward) {
            if (!state.canScrollForward) {
                component.search(false)
            }
        }
    }
}
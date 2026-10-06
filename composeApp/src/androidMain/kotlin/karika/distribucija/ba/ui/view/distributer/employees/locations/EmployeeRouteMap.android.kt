package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MarkerComposable
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karika.distribucija.ba.ui.components.KarikaColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_primary_logo
import org.jetbrains.compose.resources.vectorResource

@Composable
actual fun EmployeeRouteMap(
    points: List<EmployeeLocationPoint>,
    focus: EmployeeLocationPoint?,
    showRoute: Boolean,
    modifier: Modifier
) {
    val latLngs = remember(points) { points.map { LatLng(it.latitude, it.longitude) } }
    val cameraState = rememberCameraPositionState()
    var mapLoaded by remember { mutableStateOf(false) }
    // Resolved here (not inside the marker) so the pin bitmap re-renders once the vector is ready.
    val logo = vectorResource(Res.drawable.ic_primary_logo)

    // newLatLngBounds needs a laid-out map, so wait for onMapLoaded before fitting the route.
    LaunchedEffect(latLngs, mapLoaded, showRoute) {
        if (!mapLoaded || latLngs.isEmpty()) return@LaunchedEffect
        val bounds = LatLngBounds.builder().apply { latLngs.forEach(::include) }.build()
        val update = if (!showRoute || bounds.northeast == bounds.southwest) {
            CameraUpdateFactory.newLatLngZoom(latLngs.last(), 15f)
        } else {
            CameraUpdateFactory.newLatLngBounds(bounds, 120)
        }
        cameraState.animate(update)
    }

    LaunchedEffect(focus, mapLoaded) {
        if (!mapLoaded || focus == null) return@LaunchedEffect
        cameraState.animate(CameraUpdateFactory.newLatLngZoom(LatLng(focus.latitude, focus.longitude), 16f))
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        uiSettings = MapUiSettings(zoomControlsEnabled = true, mapToolbarEnabled = false),
        onMapLoaded = { mapLoaded = true }
    ) {
        if (showRoute && latLngs.size > 1) {
            Polyline(points = latLngs, color = KarikaColors.Primary, width = 10f)
        }
        if (showRoute && latLngs.size > 1) {
            val start = latLngs.first()
            MarkerComposable(
                logo, KarikaColors.Green5,
                state = remember(start) { MarkerState(position = start) },
                title = "Početak",
            ) {
                KarikaPin(logo = logo, color = KarikaColors.Green5, size = 30.dp)
            }
        }
        latLngs.lastOrNull()?.let { last ->
            MarkerComposable(
                logo, KarikaColors.Primary,
                state = remember(last) { MarkerState(position = last) },
                title = "Zadnja lokacija",
                zIndex = 1f,
            ) {
                KarikaPin(logo = logo, color = KarikaColors.Primary, size = 38.dp)
            }
        }
        focus?.let {
            val position = LatLng(it.latitude, it.longitude)
            MarkerComposable(
                logo, KarikaColors.Blue,
                state = remember(position) { MarkerState(position = position) },
                title = "Odabrana tačka",
                zIndex = 2f,
            ) {
                KarikaPin(logo = logo, color = KarikaColors.Blue, size = 34.dp)
            }
        }
    }
}

/** Teardrop pin: brand logo in white on a colored disc, tip anchored at the point (default anchor 0.5,1). */
@Composable
private fun KarikaPin(logo: ImageVector, color: Color, size: Dp) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size)
                .background(color, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.padding(size * 0.2f),
                imageVector = logo,
                contentDescription = null,
                tint = Color.White
            )
        }
        Canvas(modifier = Modifier.size(width = size * 0.4f, height = size * 0.3f)) {
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(this@Canvas.size.width, 0f)
                lineTo(this@Canvas.size.width / 2, this@Canvas.size.height)
                close()
            }
            drawPath(path, color)
            drawLine(Color.White, Offset(0f, 0f), Offset(this.size.width / 2, this.size.height), 1.5f)
            drawLine(Color.White, Offset(this.size.width, 0f), Offset(this.size.width / 2, this.size.height), 1.5f)
        }
    }
}

package karika.distribucija.ba.ui.view.distributer.employees.locations

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.viewinterop.UIKitView
import karika.distribucija.ba.domain.model.EmployeeLocationPoint
import karika.distribucija.ba.ui.components.KarikaColors
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_primary_logo
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.useContents
import kotlinx.cinterop.usePinned
import org.jetbrains.compose.resources.vectorResource
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import platform.CoreGraphics.CGPointMake
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.Foundation.NSData
import platform.Foundation.create
import platform.MapKit.MKAnnotationProtocol
import platform.MapKit.MKAnnotationView
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKPointAnnotation
import platform.MapKit.MKPolyline
import platform.MapKit.MKPolylineRenderer
import platform.MapKit.addOverlay
import platform.MapKit.overlays
import platform.MapKit.removeOverlays
import platform.UIKit.UIColor
import platform.UIKit.UIEdgeInsetsMake
import platform.UIKit.UIImage
import platform.darwin.NSObject
import kotlin.math.roundToInt

private const val TITLE_START = "Početak"
private const val TITLE_LAST = "Zadnja lokacija"
private const val TITLE_FOCUS = "Odabrana tačka"

/** MapKit on iOS - no API key or extra SDK needed. Pins match Android: brand logo on a colored drop. */
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun EmployeeRouteMap(
    points: List<EmployeeLocationPoint>,
    focus: EmployeeLocationPoint?,
    showRoute: Boolean,
    modifier: Modifier
) {
    val density = LocalDensity.current
    val logo = rememberVectorPainter(vectorResource(Res.drawable.ic_primary_logo))
    val pins = remember(logo, density) {
        mapOf(
            TITLE_START to renderPin(logo, KarikaColors.Green5, 30f, density),
            TITLE_LAST to renderPin(logo, KarikaColors.Primary, 38f, density),
            TITLE_FOCUS to renderPin(logo, KarikaColors.Blue, 34f, density),
        )
    }
    val delegate = remember { RouteMapDelegate() }
    delegate.pins = pins
    val mapView = remember { MKMapView().apply { this.delegate = delegate } }
    val focusAnnotation = remember { MKPointAnnotation().apply { setTitle(TITLE_FOCUS) } }

    UIKitView(factory = { mapView }, modifier = modifier)

    LaunchedEffect(points, pins, showRoute) {
        mapView.removeOverlays(mapView.overlays)
        mapView.removeAnnotations(mapView.annotations.filter { it != focusAnnotation })
        if (points.isEmpty()) return@LaunchedEffect

        val last = points.last()
        if (showRoute && points.size > 1) {
            val polyline = memScoped {
                val coords = allocArray<CLLocationCoordinate2D>(points.size)
                points.forEachIndexed { i, p ->
                    coords[i].latitude = p.latitude
                    coords[i].longitude = p.longitude
                }
                MKPolyline.polylineWithCoordinates(coords, points.size.toULong())
            }
            mapView.addOverlay(polyline)
            mapView.setVisibleMapRect(
                polyline.boundingMapRect,
                edgePadding = UIEdgeInsetsMake(56.0, 48.0, 32.0, 48.0),
                animated = true
            )
            mapView.addAnnotation(points.first().annotation(TITLE_START))
        } else {
            mapView.setRegion(
                MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(last.latitude, last.longitude), 600.0, 600.0
                ),
                animated = true
            )
        }
        mapView.addAnnotation(last.annotation(TITLE_LAST))
    }

    LaunchedEffect(focus) {
        mapView.removeAnnotation(focusAnnotation)
        if (focus == null) return@LaunchedEffect
        val coordinate = CLLocationCoordinate2DMake(focus.latitude, focus.longitude)
        focusAnnotation.setCoordinate(coordinate)
        mapView.addAnnotation(focusAnnotation)
        mapView.setRegion(MKCoordinateRegionMakeWithDistance(coordinate, 400.0, 400.0), animated = true)
    }
}

@OptIn(ExperimentalForeignApi::class)
private fun EmployeeLocationPoint.annotation(title: String) = MKPointAnnotation().apply {
    setCoordinate(CLLocationCoordinate2DMake(latitude, longitude))
    setTitle(title)
}

/** Draws the same pin as Android's KarikaPin into a bitmap: colored disc with a white ring,
 * white logo inside and a tail whose tip is the bottom-center of the image. */
@OptIn(ExperimentalForeignApi::class)
private fun renderPin(logo: VectorPainter, color: Color, sizeDp: Float, density: Density): UIImage? {
    val scale = density.density
    val disc = sizeDp * scale
    val tail = disc * 0.3f
    val width = disc.roundToInt()
    val height = (disc + tail).roundToInt()
    val bitmap = ImageBitmap(width, height)

    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(bitmap), Size(width.toFloat(), height.toFloat())) {
        val center = Offset(disc / 2, disc / 2)
        drawPath(
            Path().apply {
                moveTo(disc * 0.3f, disc * 0.8f)
                lineTo(disc * 0.7f, disc * 0.8f)
                lineTo(disc / 2, disc + tail)
                close()
            },
            color
        )
        drawCircle(Color.White, radius = disc / 2, center = center)
        drawCircle(color, radius = disc / 2 - 2 * scale, center = center)

        // Fit the (non-square) logo into the inner 60% of the disc, keeping its aspect ratio.
        val box = disc * 0.6f
        val intrinsic = logo.intrinsicSize
        val fit = minOf(box / intrinsic.width, box / intrinsic.height)
        val logoSize = Size(intrinsic.width * fit, intrinsic.height * fit)
        translate((disc - logoSize.width) / 2, (disc - logoSize.height) / 2) {
            with(logo) { draw(logoSize, colorFilter = ColorFilter.tint(Color.White)) }
        }
    }

    val png = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)?.bytes
        ?: return null
    val data = png.usePinned { NSData.create(bytes = it.addressOf(0), length = png.size.toULong()) }
    return UIImage.imageWithData(data, scale = scale.toDouble())
}

private class RouteMapDelegate : NSObject(), MKMapViewDelegateProtocol {
    var pins: Map<String, UIImage?> = emptyMap()

    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        val polyline = rendererForOverlay as? MKPolyline ?: return MKOverlayRenderer(rendererForOverlay)
        return MKPolylineRenderer(polyline).apply {
            strokeColor = UIColor(red = 0xFF / 255.0, green = 0x2A / 255.0, blue = 0x86 / 255.0, alpha = 1.0)
            lineWidth = 4.0
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun mapView(mapView: MKMapView, viewForAnnotation: MKAnnotationProtocol): MKAnnotationView? {
        val title = viewForAnnotation.title ?: return null
        val image = pins[title] ?: return null
        val view = mapView.dequeueReusableAnnotationViewWithIdentifier(title)
            ?: MKAnnotationView(annotation = viewForAnnotation, reuseIdentifier = title)
        view.annotation = viewForAnnotation
        view.image = image
        view.canShowCallout = true
        // Anchor the tail tip (bottom-center) on the coordinate.
        view.centerOffset = CGPointMake(0.0, -image.size.useContents { height } / 2)
        view.layer.zPosition = when (title) {
            TITLE_FOCUS -> 2.0
            TITLE_LAST -> 1.0
            else -> 0.0
        }
        return view
    }
}

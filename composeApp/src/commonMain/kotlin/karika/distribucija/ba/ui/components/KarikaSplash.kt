package karika.distribucija.ba.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_primary_logo
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.PI
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin

/**
 * Animated splash shown over the app on start: the outline of Bosnia and Herzegovina draws itself,
 * buyers (white) and suppliers (blue) pop up in its towns and get connected, orders travel the lines,
 * then the map settles lower and the Karika logo and name come in above and below it.
 *
 * Drawn on a 1080x1920 stage, scaled to fit the screen, with the pink ground filling the rest.
 */
@Composable
fun KarikaSplash() {
    var visible by rememberSaveable { mutableStateOf(true) }
    var time by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        if (!visible) return@LaunchedEffect
        val start = withFrameNanos { it }
        while (time < SPLASH_END) {
            withFrameNanos { now -> time = (now - start) / 1_000_000_000f }
        }
        visible = false
    }

    AnimatedVisibility(
        visible = visible,
        enter = androidx.compose.animation.EnterTransition.None,
        exit = fadeOut(tween(350))
    ) {
        val logo = rememberVectorPainter(vectorResource(Res.drawable.ic_primary_logo))
        val measurer = rememberTextMeasurer()
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .background(Ground)
                // swallow touches so nothing underneath reacts while the splash is up
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
        ) {
            val s = min(size.width / STAGE_W, size.height / VISIBLE_H)
            val ox = (size.width - STAGE_W * s) / 2f
            val oy = (size.height - VISIBLE_H * s) / 2f - VISIBLE_TOP * s

            withTransform({
                translate(ox, oy)
                scale(s, s, pivot = Offset.Zero)
            }) {
                drawAir()
                val t = time
                if (t < NET_END) {
                    drawNet(t - NET_LEAD)
                } else {
                    drawJoin(t - NET_END, logo)
                }
            }
            if (time >= NET_END) {
                drawWords(time - NET_END, s, ox, oy, measurer)
            }
        }
    }
}

// --- timing (seconds) -----------------------------------------------------------------------

/** The network scene runs from -0.7 so the border starts from nothing. */
private const val NET_LEAD = 0.7f
private const val NET_END = NET_LEAD + 1f
private const val JOIN_END = NET_END + 1f
private const val SPLASH_END = JOIN_END + 0.5f

// --- stage ----------------------------------------------------------------------------------

private const val STAGE_W = 1080f
private const val VISIBLE_TOP = 150f
private const val VISIBLE_H = 1500f
private const val CX = 540f
private const val CY = 860f

private val Ground = Color(0xFFFF3B8A)
private val Main = Color.White
private val Second = Color(0xFF1C1A2E)
private val Accent = Color(0xFF3A8DDE)

private fun mix(a: Color, amountOfA: Float, b: Color) = lerp(b, a, amountOfA)

private val Glow = mix(Main, 0.30f, Ground)
private val Halo = mix(Main, 0.45f, Ground)
private val Wire = mix(Second, 0.55f, Ground)
private val Shade = Second.copy(alpha = 0.22f)
private val Land = mix(Main, 0.16f, Ground)
private val Border = mix(Main, 0.75f, Ground)
private val Shine = Main.copy(alpha = 0.70f)
private val BoxStripe = mix(Accent, 0.40f, Main)

private val BihPath: Path = PathParser().parsePathString(
    "M93 444 L136 428 L176 471 L224 504 L267 431 L333 444 L438 457 L509 464 L580 487 L616 454 L728 484 " +
        "L770 481 L806 530 L858 550 L939 540 L918 616 L877 705 L937 768 L989 844 L972 916 L913 982 L861 1015 " +
        "L842 1065 L782 1147 L742 1306 L675 1289 L592 1230 L526 1190 L533 1154 L485 1108 L438 1015 L367 949 " +
        "L295 883 L212 807 L165 735 L112 662 L79 570 Z"
).toPath()

/** Firms in real towns: Bihać, Prijedor, Banja Luka, Doboj, Brčko, Bijeljina, Tuzla, Zenica, Jajce,
 * Sarajevo, Goražde, Livno, Mostar, Trebinje. Kind 0 is a buyer, 1 a supplier. */
private val Nodes = listOf(
    Triple(110f, 563f, 0), Triple(310f, 510f, 1), Triple(424f, 579f, 0), Triple(637f, 593f, 1),
    Triple(808f, 547f, 0), Triple(904f, 583f, 1), Triple(775f, 655f, 0), Triple(595f, 768f, 1),
    Triple(443f, 721f, 0), Triple(713f, 880f, 1), Triple(849f, 942f, 0), Triple(381f, 890f, 1),
    Triple(571f, 1051f, 0), Triple(697f, 1259f, 1)
)
private val Edges = listOf(
    0 to 1, 1 to 2, 2 to 3, 3 to 4, 4 to 5, 5 to 6, 6 to 3, 3 to 7, 7 to 8, 8 to 2, 7 to 9, 6 to 9,
    9 to 10, 9 to 12, 12 to 11, 11 to 8, 11 to 0, 12 to 13, 10 to 13
)

/** Nodes nearest the centre appear first, then the network spreads outward. */
private val Rank: IntArray = IntArray(Nodes.size).also { rank ->
    Nodes.indices.sortedBy { hypot(Nodes[it].first - CX, Nodes[it].second - CY) }
        .forEachIndexed { r, i -> rank[i] = r }
}

private fun svg(d: String) = PathParser().parsePathString(d).toPath()

private val NodeShine = svg("M-26 -18 A32 32 0 0 1 10 -32")
private val BuyerRoof = svg("M-19 -6 L-15 -16 H15 L19 -6 Z")
private val BuyerAwning = svg("M-19 -6 q4.75 6 9.5 0 q4.75 6 9.5 0 q4.75 6 9.5 0 q4.75 6 9.5 0")
private val SupplierBox = svg("M-16 -6 L0 -14 L16 -6 L16 12 L0 19 L-16 12 Z")
private val SupplierFold = svg("M-16 -6 L0 2 L16 -6 M0 2 V19")

// --- easing ---------------------------------------------------------------------------------

private fun seg(t: Float, a: Float, b: Float) = ((t - a) / (b - a)).coerceIn(0f, 1f)
private fun eo(x: Float) = 1f - (1f - x).pow(3)
private fun eio(x: Float) = if (x < 0.5f) 4f * x * x * x else 1f - (-2f * x + 2f).pow(3) / 2f
private fun eob(x: Float, s: Float): Float {
    val p = x - 1f
    return 1f + (s + 1f) * p * p * p + s * p * p
}
private fun lerpF(a: Float, b: Float, t: Float) = a + (b - a) * t
private fun frac(x: Float) = ((x % 1f) + 1f) % 1f

// --- drawing --------------------------------------------------------------------------------

private fun DrawScope.drawAir() {
    // a soft lighter glow behind the map: an ellipse 60% x 40% of the stage, at 50% / 45%
    val center = Offset(CX, 1920f * 0.45f)
    val ry = 1920f * 0.40f
    val rx = STAGE_W * 0.60f
    scale(scaleX = rx / ry, scaleY = 1f, pivot = center) {
        drawCircle(
            brush = Brush.radialGradient(
                0f to Glow, 0.75f to Glow.copy(alpha = 0f),
                center = center, radius = ry
            ),
            radius = ry,
            center = center
        )
    }
}

private fun DrawScope.drawMap(drawn: Float, landAlpha: Float) {
    if (landAlpha > 0f) drawPath(BihPath, Land, alpha = landAlpha)
    if (drawn <= 0f) return
    val border = if (drawn >= 1f) {
        BihPath
    } else {
        val measure = PathMeasure().apply { setPath(BihPath, false) }
        Path().also { measure.getSegment(0f, measure.length * drawn, it, true) }
    }
    drawPath(border, Border, style = Stroke(width = 6f, join = StrokeJoin.Round))
}

private fun DrawScope.drawNode(x: Float, y: Float, scale: Float, kind: Int) {
    if (scale <= 0f) return
    withTransform({
        translate(x, y)
        scale(scale, scale, pivot = Offset.Zero)
    }) {
        drawOval(Shade, topLeft = Offset(4f - 30f, 44f - 6f), size = Size(60f, 12f))
        drawCircle(Halo, radius = 44f, center = Offset.Zero)
        drawCircle(if (kind == 1) Accent else Main, radius = 36f, center = Offset.Zero)
        drawPath(NodeShine, Shine, style = Stroke(width = 4f, cap = StrokeCap.Round))
        if (kind == 1) {
            drawPath(SupplierBox, Main)
            drawPath(SupplierFold, Accent, style = Stroke(width = 3f, join = StrokeJoin.Round))
            drawLine(BoxStripe, Offset(-8f, -10f), Offset(8f, -2f), strokeWidth = 5f)
        } else {
            drawPath(BuyerRoof, Second)
            drawPath(BuyerAwning, Second)
            drawRect(Second, topLeft = Offset(-15f, -2f), size = Size(30f, 20f), style = Stroke(width = 3f))
            drawRect(Second, topLeft = Offset(-4f, 6f), size = Size(8f, 12f))
        }
    }
}

/** An order travelling a finished line, buyer and supplier taking turns to send. */
private fun DrawScope.drawPacket(j: Int, clock: Float, alphaIn: Float) {
    val (a, b) = Edges[j]
    val t0 = 0.35f + j * 0.02f
    val q = frac((clock - t0) / 0.8f)
    val go = if (j % 2 == 1) 1f - q else q
    val alpha = alphaIn * min(1f, sin(PI.toFloat() * q) * 3f)
    if (alpha <= 0f) return
    val p = Offset(lerpF(Nodes[a].first, Nodes[b].first, go), lerpF(Nodes[a].second, Nodes[b].second, go))
    drawCircle(Main, radius = 7f, center = p, alpha = alpha.coerceIn(0f, 1f))
}

private fun DrawScope.drawNet(t: Float) {
    val cam = lerpF(1f, 1.03f, eio(seg(t, 0f, 1f)))
    scale(cam, cam, pivot = Offset(CX, CY)) {
        drawMap(drawn = eio(seg(t, -0.7f, 0.5f)), landAlpha = eo(seg(t, -0.4f, 0.3f)))
        Edges.forEachIndexed { j, (a, b) ->
            val d = eo(seg(t, 0.05f + j * 0.02f, 0.3f + j * 0.02f))
            if (d > 0f) {
                val from = Offset(Nodes[a].first, Nodes[a].second)
                val to = Offset(lerpF(Nodes[a].first, Nodes[b].first, d), lerpF(Nodes[a].second, Nodes[b].second, d))
                drawLine(Wire, from, to, strokeWidth = 5f, cap = StrokeCap.Round)
            }
        }
        Edges.indices.forEach { j ->
            val t0 = 0.35f + j * 0.02f
            if (t >= t0) drawPacket(j, t, eo(seg(t, t0, t0 + 0.15f)))
        }
        Nodes.forEachIndexed { i, (x, y, kind) ->
            val a = -0.5f + Rank[i] * 0.03f
            val s = eob(seg(t, a, a + 0.3f), 1.7f) * (1f + 0.015f * sin(2f * PI.toFloat() * t / 3f + i))
            drawNode(x, y, max(0f, s), kind)
        }
    }
}

private fun DrawScope.drawJoin(t: Float, logo: androidx.compose.ui.graphics.painter.Painter) {
    val clock = t + 1f
    val cam = lerpF(1.03f, 1f, eio(seg(t, 0f, 0.6f)))
    scale(cam, cam, pivot = Offset(CX, CY)) {
        // one soft ring goes out from the logo
        val ring = seg(t, 0.3f, 0.9f)
        if (ring > 0f && ring < 1f) {
            drawCircle(
                Main, radius = lerpF(180f, 360f, eo(ring)), center = Offset(540f, 373f),
                alpha = 0.45f * (1f - ring), style = Stroke(width = 5f)
            )
        }
        // the map with its network settles lower and a little smaller, making room for the logo
        val mp = eio(seg(t, 0f, 0.5f))
        val ws = lerpF(1f, 0.8f, mp)
        withTransform({
            translate(0f, 140f * mp)
            scale(ws, ws, pivot = Offset(CX, CY))
        }) {
            drawMap(drawn = 1f, landAlpha = 1f)
            Edges.forEach { (a, b) ->
                drawLine(
                    Wire, Offset(Nodes[a].first, Nodes[a].second), Offset(Nodes[b].first, Nodes[b].second),
                    strokeWidth = 5f, cap = StrokeCap.Round
                )
            }
            Edges.indices.forEach { j -> drawPacket(j, clock, 1f) }
            Nodes.forEachIndexed { i, (x, y, kind) ->
                drawNode(x, y, 1f + 0.015f * sin(2f * PI.toFloat() * clock / 3f + i), kind)
            }
        }
    }

    // the logo drops in above the map
    val hand = eo(seg(t, 0.2f, 0.45f))
    if (hand > 0f) {
        val pop = eob(seg(t, 0.2f, 0.6f), 1.6f)
        val ls = lerpF(0.85f, 1f, pop) * (1f + 0.012f * sin(2f * PI.toFloat() * t / 3f))
        val box = Size(300f, 346f)
        val fit = min(box.width / logo.intrinsicSize.width, box.height / logo.intrinsicSize.height)
        val drawSize = Size(logo.intrinsicSize.width * fit, logo.intrinsicSize.height * fit)
        val center = Offset(390f + box.width / 2f, 200f + box.height / 2f - 40f * (1f - pop))
        withTransform({
            scale(ls, ls, pivot = center)
            translate(center.x - drawSize.width / 2f, center.y - drawSize.height / 2f)
        }) {
            with(logo) { draw(drawSize, alpha = hand, colorFilter = ColorFilter.tint(Main)) }
        }
    }
}

/** "KARIKA" and the line under it, drawn in screen space so the text stays crisp. */
private fun DrawScope.drawWords(
    t: Float,
    s: Float,
    ox: Float,
    oy: Float,
    measurer: androidx.compose.ui.text.TextMeasurer
) {
    val a = eo(seg(t, 0.38f, 0.62f))
    val b = eo(seg(t, 0.48f, 0.72f))
    val px = { v: Float -> (v * s / density / fontScale).sp }
    if (a > 0f) {
        val name = measurer.measure(
            "KARIKA",
            TextStyle(color = Main, fontSize = px(120f), fontWeight = FontWeight.Bold, letterSpacing = 0.04.em)
        )
        val y = oy + (1420f + 30f * (1f - a)) * s
        drawText(name, topLeft = Offset(ox + (STAGE_W * s - name.size.width) / 2f, y), alpha = a)
    }
    if (b > 0f) {
        val sub = measurer.measure(
            "Veleprodaja koja spaja",
            TextStyle(color = Main, fontSize = px(42f), fontWeight = FontWeight.Normal)
        )
        val y = oy + (1420f + 120f + 18f + 26f * (1f - b) - 2f * t) * s
        drawText(sub, topLeft = Offset(ox + (STAGE_W * s - sub.size.width) / 2f, y), alpha = b)
    }
}

package karika.distribucija.ba.ui.view.distributer.board

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import karika.distribucija.ba.ui.components.KarikaColors
import karika.distribucija.ba.ui.components.KarikaText
import karika.distribucija.ba.ui.components.XSpacer32
import karika.distribucija.ba.ui.components.YSpacer16
import karikav2.composeapp.generated.resources.Res
import karikav2.composeapp.generated.resources.ic_order_approved_total
import karikav2.composeapp.generated.resources.ic_order_total
import karikav2.composeapp.generated.resources.ic_total
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.vectorResource
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun BoardView(component: BoardComponent) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Board(component)
        Status(component)
        Products(component)
    }

    LaunchedEffect(Unit) {
        component.dash()
    }
}

@Composable
private fun Board(component: BoardComponent) {
    val dash = component.dash.collectAsState()

    KarikaText(
        modifier = Modifier
            .fillMaxWidth(),
        text = "Kontrolna ploča",
        color = KarikaColors.Gray2,
        textSize = 18.sp,
        fontWeight = FontWeight.W700
    )
    Item(
        title = "Ukupno odobrenih narudžbi:",
        value = dash.value.approvedOrdersCount ?: "0",
        icon = Res.drawable.ic_order_approved_total
    )
    Item(
        title = "Ukupan broj narudžbi:",
        value = dash.value.ordersPlacedTotal ?: "0",
        icon = Res.drawable.ic_order_total
    )
    Item(
        title = "Ukupan promet:",
        value = dash.value.approvedTotal() + " KM",
        icon = Res.drawable.ic_total
    )
}

@Composable
private fun Item(
    title: String,
    value: String,
    icon: DrawableResource
) {
    Row(
        modifier = Modifier
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(4.dp))
            .fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = title,
                color = KarikaColors.Gray2,
                textSize = 14.sp,
                fontWeight = FontWeight.W400
            )
            KarikaText(
                modifier = Modifier
                    .fillMaxWidth(),
                text = value,
                color = KarikaColors.Gray2,
                textSize = 22.sp,
                fontWeight = FontWeight.W700
            )
        }
        Image(
            modifier = Modifier
                .padding(16.dp),
            imageVector = vectorResource(icon),
            contentDescription = ""
        )
    }
}

@Composable
private fun Status(component: BoardComponent) {
    val dash = component.dash.collectAsState()
    Column(
        modifier = Modifier
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(4.dp))
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KarikaText(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            text = "Status narudžbi",
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            fontWeight = FontWeight.W700
        )

        val data = listOf(
            PieSlice(dash.value.approved(), Color(0xFF4CAF50), "Odobrene narudžbe"),
            PieSlice(dash.value.pending(), Color(0xFFFFA726), "Narudžbe na čekanju"),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PieChart(
                slices = data,
                modifier = Modifier
                    .padding(start = 8.dp, bottom = 8.dp)
                    .size(132.dp),
                startAngle = -90f,
                holeRatio = 0.75f,
                animate = true,
                onSliceClick = { _, _ -> }
            )
            XSpacer32()
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                data.forEachIndexed { i, s ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .padding(end = 8.dp)
                    ) {
                        Box(
                            Modifier
                                .size(14.dp)
                                .background(s.color, CircleShape)
                        )
                        Spacer(Modifier.width(8.dp))
                        KarikaText(
                            modifier = Modifier,
                            text = s.label,
                            color = KarikaColors.Gray2,
                            textSize = 14.sp,
                            fontWeight = FontWeight.W700
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Products(component: BoardComponent) {
    val dash = component.dash.collectAsState()
    if (dash.value.bestSellerProducts.isNullOrEmpty()) {
        return
    }

    Column(
        modifier = Modifier
            .border(width = 1.dp, color = KarikaColors.Border, shape = RoundedCornerShape(4.dp))
            .fillMaxSize(),
    ) {
        KarikaText(
            modifier = Modifier
                .padding(top = 16.dp, start = 16.dp, end = 16.dp)
                .fillMaxWidth(),
            text = "Najprodavaniji proizvodi",
            color = KarikaColors.Gray2,
            textSize = 16.sp,
            fontWeight = FontWeight.W700
        )
        YSpacer16()
        TableHeaderRow()
        dash.value.bestSellerProducts?.forEach {
            TableRow(
                name = (it.productName ?: ""),
                qty = (it.orderedQuantity ?: "") + " kom",
                price = it.price() + " KM"
            )
        }
        YSpacer16()
    }
}

@Composable
private fun TableHeaderRow() {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(color = KarikaColors.Gray16)
            .border(width = 0.5.dp, color = KarikaColors.Border)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .weight(0.4f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "ARTIKAL"
            )
        }
        Box(
            modifier = Modifier
                .weight(0.3f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "KOLIČINA"
            )
        }
        Box(
            modifier = Modifier
                .weight(0.3f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray15,
                fontWeight = FontWeight.W600,
                textSize = 10.sp,
                text = "CIJENA"
            )
        }
    }
}

@Composable
private fun TableRow(name: String, qty: String, price: String) {
    val height = remember { mutableStateOf(0) }
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .background(color = KarikaColors.White)
            .border(width = 0.5.dp, color = KarikaColors.Border)
            .fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .onGloballyPositioned {
                    height.value = it.size.height
                }
                .weight(0.4f)
                .border(width = 0.5.dp, color = KarikaColors.Border)
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = name
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .weight(0.3f)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = qty
            )
        }
        Box(
            modifier = Modifier
                .height(with(LocalDensity.current) { height.value.toDp() })
                .weight(0.3f)
                .border(width = 0.5.dp, color = KarikaColors.Border),
            contentAlignment = Alignment.CenterStart
        ) {
            KarikaText(
                modifier = Modifier
                    .padding(8.dp),
                color = KarikaColors.Gray17,
                fontWeight = FontWeight.W600,
                textSize = 12.sp,
                text = price
            )
        }
    }
}

data class PieSlice(
    val value: Float,
    val color: Color,
    val label: String = ""
)

private fun List<PieSlice>.normalized(): List<PieSlice> {
    val total = sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(0.0001f)
    return map { it.copy(value = it.value / total) }
}

private fun angleOfPoint(center: Offset, p: Offset): Float {
    val v = p - center
    val angleRad = atan2(v.y, v.x)
    val angleDeg = (angleRad * 180f / PI.toFloat())
    return (angleDeg + 360f) % 360f
}

// 2) PieChart kompozabla
@Composable
fun PieChart(
    slices: List<PieSlice>,
    modifier: Modifier = Modifier,
    startAngle: Float = -90f,
    holeRatio: Float = 0f,
    strokeWidth: Dp = 0.dp,
    animate: Boolean = true,
    onSliceClick: ((index: Int, slice: PieSlice) -> Unit)? = null,
    selectedIndex: Int? = null,
    selectedOffsetDp: Dp = 8.dp
) {
    val norm = remember(slices) { slices.normalized() }
    val totalSweep = 360f

    val animProgress by animateFloatAsState(
        targetValue = if (animate) 1f else 1f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "pie_anim_progress"
    )
    val density = LocalDensity.current
    val selectedOffsetPx = with(density) { selectedOffsetDp.toPx() }
    val segments = remember(norm, startAngle) {
        buildList {
            var curStart = startAngle
            norm.forEach { s ->
                val sweep = s.value * totalSweep
                add(Triple(curStart, curStart + sweep, s))
                curStart += sweep
            }
        }
    }

    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Canvas(
        modifier = modifier
            .onSizeChanged { canvasSize = it }
            .pointerInput(onSliceClick != null) {
                if (onSliceClick == null) return@pointerInput
                detectTapGestures { offset ->
                    val w = canvasSize.width.toFloat()
                    val h = canvasSize.height.toFloat()
                    val center = Offset(w / 2f, h / 2f)
                    val r = min(w, h) / 2f

                    val dist = (offset - center).getDistance()
                    val inHole = holeRatio > 0f && dist < r * holeRatio
                    val outside = dist > r

                    if (!inHole && !outside) {
                        val touchAngle = angleOfPoint(center, offset)
                        var a = (touchAngle - startAngle + 360f) % 360f

                        segments.forEachIndexed { index, (sa, ea, slice) ->
                            val sweep = (ea - sa + 360f) % 360f
                            val rel = (a + 360f) % 360f
                            if (rel >= 0f && rel <= sweep + 0.0001f) {
                                onSliceClick(index, slice)
                                return@detectTapGestures
                            }
                            a = (a - sweep + 360f) % 360f
                        }
                    }
                }
            }
    ) {
        if (segments.isEmpty()) return@Canvas

        val w = size.width
        val h = size.height
        val radius = min(w, h) / 2f
        val center = Offset(w / 2f, h / 2f)

        val style = if (holeRatio > 0f) {
            val ringWidth = radius * (1f - holeRatio)
            Stroke(width = ringWidth)
        } else if (strokeWidth > 0.dp) {
            Stroke(width = with(density) { strokeWidth.toPx() })
        } else {
            Fill
        }
        segments.forEachIndexed { idx, (sa, ea, s) ->
            val sweep = (ea - sa)
            val visibleSweep = sweep * animProgress

            val middleAngleRad = (sa + sweep / 2f) * (PI / 180f)
            val offsetTranslate = if (selectedIndex == idx && visibleSweep > 0f) {
                Offset(
                    (cos(middleAngleRad) * selectedOffsetPx).toFloat(),
                    (sin(middleAngleRad) * selectedOffsetPx).toFloat()
                )
            } else Offset.Zero
            val rect = Rect(
                left = center.x - radius + offsetTranslate.x,
                top = center.y - radius + offsetTranslate.y,
                right = center.x + radius + offsetTranslate.x,
                bottom = center.y + radius + offsetTranslate.y
            )

            drawArc(
                color = s.color,
                startAngle = sa,
                sweepAngle = visibleSweep,
                useCenter = style == Fill,
                topLeft = rect.topLeft,
                size = rect.size,
                style = style
            )
        }
    }
}

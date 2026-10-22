
package com.mkarshnas6.karenstudio.mathcalculator.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private val edgeColors = listOf(
    Color(0xFFE53935),
    Color(0xFF1E88E5),
    Color(0xFF43A047),
    Color(0xFFFB8C00),
    Color(0xFF8E24AA),
    Color(0xFF00ACC1),
    Color(0xFFD81B60),
    Color(0xFF3949AB),
    Color(0xFF7CB342),
    Color(0xFFF4511E),
    Color(0xFF6D4C41),
    Color(0xFF546E7A)
)

private data class GraphEdge(
    val from: Int,
    val to: Int,
    val weight: Int,
    val color: Color,
    val curve: Float
)

private fun pointOnQuadratic(
    start: Offset,
    control: Offset,
    end: Offset,
    t: Float
): Offset {
    val u = 1f - t
    return Offset(
        u * u * start.x + 2f * u * t * control.x + t * t * end.x,
        u * u * start.y + 2f * u * t * control.y + t * t * end.y
    )
}

private fun quadraticTangent(
    start: Offset,
    control: Offset,
    end: Offset,
    t: Float
): Offset {
    return Offset(
        2f * (1f - t) * (control.x - start.x) +
                2f * t * (end.x - control.x),
        2f * (1f - t) * (control.y - start.y) +
                2f * t * (end.y - control.y)
    )
}

@Composable
fun GraphCanvas(
    adjacency: List<List<Int>>,
    modifier: Modifier = Modifier
) {
    val n = adjacency.size

    if (n == 0) return

    val edges = remember(adjacency) {
        val rawEdges = buildList {
            for (i in 0 until n) {
                for (j in 0 until n) {
                    if (adjacency.getOrNull(i)?.getOrNull(j) != null &&
                        adjacency[i][j] != 0
                    ) {
                        add(Triple(i, j, adjacency[i][j]))
                    }
                }
            }
        }

        rawEdges.mapIndexed { index, edge ->
            val (from, to, weight) = edge

            val reverseExists = from != to &&
                    adjacency.getOrNull(to)?.getOrNull(from) != null &&
                    adjacency[to][from] != 0

            val curve = when {
                from == to -> 0f
                reverseExists -> if (from < to) 1f else -1f
                else -> if ((from + to) % 2 == 0) 1f else -1f
            }

            GraphEdge(
                from = from,
                to = to,
                weight = weight,
                color = edgeColors[index % edgeColors.size],
                curve = curve
            )
        }
    }

    var visibleNodes by remember { mutableIntStateOf(0) }
    var visibleEdges by remember { mutableIntStateOf(0) }
    val edgeProgress = remember { Animatable(0f) }

    LaunchedEffect(adjacency) {
        visibleNodes = 0
        visibleEdges = 0
        edgeProgress.snapTo(0f)

        for (i in 1..n) {
            delay(160)
            visibleNodes = i
        }

        delay(250)

        for (i in edges.indices) {
            edgeProgress.snapTo(0f)
            visibleEdges = i + 1
            edgeProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 450)
            )
            delay(60)
        }
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f

        val nodeRadius = 25.dp.toPx()
        val strokeWidth = 2.2.dp.toPx()
        val arrowLength = 12.dp.toPx()
        val arrowWidth = 6.dp.toPx()
        val labelOffset = 13.dp.toPx()
        val curveBase = 20.dp.toPx()

        val radius = minOf(
            size.width * 0.39f,
            size.height * 0.38f
        ).coerceAtLeast(nodeRadius * 1.5f)

        val positions = List(n) { i ->
            val angle = 2.0 * Math.PI * i / n - Math.PI / 2.0

            Offset(
                x = centerX + radius * cos(angle).toFloat(),
                y = centerY + radius * sin(angle).toFloat()
            )
        }

        val textPaint = android.graphics.Paint(
            android.graphics.Paint.ANTI_ALIAS_FLAG
        ).apply {
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 13.dp.toPx()
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        val nodeTextPaint = android.graphics.Paint(
            android.graphics.Paint.ANTI_ALIAS_FLAG
        ).apply {
            color = android.graphics.Color.WHITE
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = 17.dp.toPx()
            typeface = android.graphics.Typeface.create(
                android.graphics.Typeface.DEFAULT,
                android.graphics.Typeface.BOLD
            )
        }

        fun drawArrow(
            tip: Offset,
            tangent: Offset,
            color: Color
        ) {
            val length = sqrt(
                tangent.x * tangent.x + tangent.y * tangent.y
            )

            if (length < 0.001f) return

            val ux = tangent.x / length
            val uy = tangent.y / length

            val base = Offset(
                tip.x - ux * arrowLength,
                tip.y - uy * arrowLength
            )

            val perpendicular = Offset(-uy, ux)

            val left = Offset(
                base.x + perpendicular.x * arrowWidth,
                base.y + perpendicular.y * arrowWidth
            )

            val right = Offset(
                base.x - perpendicular.x * arrowWidth,
                base.y - perpendicular.y * arrowWidth
            )

            drawLine(
                color = color,
                start = left,
                end = tip,
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )

            drawLine(
                color = color,
                start = right,
                end = tip,
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }

        edges.take(visibleEdges).forEachIndexed { index, edge ->
            val startCenter = positions[edge.from]
            val endCenter = positions[edge.to]
            val progress = if (index == visibleEdges - 1) {
                edgeProgress.value
            } else {
                1f
            }

            val color = edge.color

            if (edge.from == edge.to) {
                val loopRadius = nodeRadius + 13.dp.toPx()
                val loopCenter = Offset(
                    startCenter.x,
                    startCenter.y - nodeRadius * 0.85f - loopRadius * 0.45f
                )

                val loopStart = Offset(
                    startCenter.x - nodeRadius * 0.65f,
                    startCenter.y - nodeRadius * 0.65f
                )

                val loopEnd = Offset(
                    startCenter.x + nodeRadius * 0.65f,
                    startCenter.y - nodeRadius * 0.65f
                )

                val control1 = Offset(
                    loopCenter.x - loopRadius,
                    loopCenter.y - loopRadius
                )

                val control2 = Offset(
                    loopCenter.x + loopRadius,
                    loopCenter.y - loopRadius
                )

                val segments = 48
                val maxSegment = (segments * progress).toInt()

                var previous = loopStart

                for (s in 1..maxSegment) {
                    val t = s.toFloat() / segments
                    val u = 1f - t

                    val current = Offset(
                        u * u * u * loopStart.x +
                                3f * u * u * t * control1.x +
                                3f * u * t * t * control2.x +
                                t * t * t * loopEnd.x,
                        u * u * u * loopStart.y +
                                3f * u * u * t * control1.y +
                                3f * u * t * t * control2.y +
                                t * t * t * loopEnd.y
                    )

                    drawLine(
                        color = color,
                        start = previous,
                        end = current,
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    previous = current
                }

                if (progress >= 0.98f) {
                    val tangent = Offset(
                        loopEnd.x - control2.x,
                        loopEnd.y - control2.y
                    )

                    drawArrow(loopEnd, tangent, color)
                }

                if (progress >= 0.85f) {
                    val labelPosition = Offset(
                        loopCenter.x,
                        loopCenter.y - loopRadius * 0.8f
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        edge.weight.toString(),
                        labelPosition.x,
                        labelPosition.y,
                        textPaint.apply {
                            this.color = android.graphics.Color.rgb(
                                (color.red * 255).toInt(),
                                (color.green * 255).toInt(),
                                (color.blue * 255).toInt()
                            )
                        }
                    )
                }
            } else {
                val dx = endCenter.x - startCenter.x
                val dy = endCenter.y - startCenter.y
                val distance = sqrt(dx * dx + dy * dy)

                if (distance <= nodeRadius * 2f) return@forEachIndexed

                val ux = dx / distance
                val uy = dy / distance

                val perpendicular = Offset(-uy, ux)

                val start = Offset(
                    startCenter.x + ux * nodeRadius,
                    startCenter.y + uy * nodeRadius
                )

                val end = Offset(
                    endCenter.x - ux * (nodeRadius + arrowLength * 0.7f),
                    endCenter.y - uy * (nodeRadius + arrowLength * 0.7f)
                )

                val curveAmount = if (
                    adjacency.getOrNull(edge.to)?.getOrNull(edge.from) != null &&
                    adjacency[edge.to][edge.from] != 0
                ) {
                    curveBase * 1.25f
                } else {
                    curveBase * 0.7f
                }

                val control = Offset(
                    (start.x + end.x) / 2f +
                            perpendicular.x * curveAmount * edge.curve,
                    (start.y + end.y) / 2f +
                            perpendicular.y * curveAmount * edge.curve
                )

                val segments = 48
                val maxSegment = (segments * progress).toInt()

                var previous = start

                for (s in 1..maxSegment) {
                    val t = s.toFloat() / segments
                    val current = pointOnQuadratic(
                        start,
                        control,
                        end,
                        t
                    )

                    drawLine(
                        color = color,
                        start = previous,
                        end = current,
                        strokeWidth = strokeWidth,
                        cap = androidx.compose.ui.graphics.StrokeCap.Round
                    )

                    previous = current
                }

                if (progress >= 0.98f) {
                    val tip = pointOnQuadratic(start, control, end, 1f)
                    val tangent = quadraticTangent(start, control, end, 1f)

                    drawArrow(tip, tangent, color)
                }

                if (progress >= 0.85f) {
                    val labelT = 0.5f
                    val labelPoint = pointOnQuadratic(
                        start,
                        control,
                        end,
                        labelT
                    )

                    val tangent = quadraticTangent(
                        start,
                        control,
                        end,
                        labelT
                    )

                    val tangentLength = sqrt(
                        tangent.x * tangent.x + tangent.y * tangent.y
                    ).coerceAtLeast(0.001f)

                    val normal = Offset(
                        -tangent.y / tangentLength,
                        tangent.x / tangentLength
                    )

                    val labelPosition = Offset(
                        labelPoint.x + normal.x * labelOffset,
                        labelPoint.y + normal.y * labelOffset
                    )

                    val paint = textPaint.apply {
                        this.color = android.graphics.Color.rgb(
                            (color.red * 255).toInt(),
                            (color.green * 255).toInt(),
                            (color.blue * 255).toInt()
                        )
                    }

                    val textWidth = paint.measureText(edge.weight.toString())
                    val fontMetrics = paint.fontMetrics
                    val textHeight = fontMetrics.descent - fontMetrics.ascent

                    drawCircle(
                        color = Color(0xFFF8F7FF),
                        radius = maxOf(textWidth, textHeight) * 0.58f,
                        center = labelPosition
                    )

                    drawContext.canvas.nativeCanvas.drawText(
                        edge.weight.toString(),
                        labelPosition.x,
                        labelPosition.y - (fontMetrics.ascent + fontMetrics.descent) / 2f,
                        paint
                    )
                }
            }
        }

        positions.take(visibleNodes).forEachIndexed { index, position ->
            drawCircle(
                color = Color(0xFF1565C0),
                radius = nodeRadius,
                center = position
            )

            drawCircle(
                color = Color.White,
                radius = nodeRadius,
                center = position,
                style = Stroke(width = 2.dp.toPx())
            )

            drawContext.canvas.nativeCanvas.drawText(
                vertexLabel(index),
                position.x,
                position.y - (
                        nodeTextPaint.ascent() + nodeTextPaint.descent()
                        ) / 2f,
                nodeTextPaint
            )
        }
    }
}

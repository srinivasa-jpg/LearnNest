package com.example.learnnest.ui.design

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

internal enum class HomeActionIcon { BOOK, QUIZ }

/** Reusable two-option Home card. Both cards are accessible clickable surfaces. */
@Composable
internal fun LearnNestActionCard(
    title: String,
    subtitle: String,
    primary: Boolean,
    icon: HomeActionIcon,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val foreground = if (primary) LearnNestColors.White else LearnNestColors.WineDark
    val gradient = if (primary) {
        Brush.horizontalGradient(listOf(LearnNestColors.WineDark, LearnNestColors.Wine))
    } else {
        Brush.horizontalGradient(listOf(LearnNestColors.Ivory, LearnNestColors.Rose))
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(21.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .background(gradient)
                .fillMaxWidth()
                .padding(horizontal = 17.dp, vertical = 19.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(13.dp)
        ) {
            Surface(
                modifier = Modifier.size(58.dp),
                shape = RoundedCornerShape(16.dp),
                color = LearnNestColors.Ivory
            ) {
                Box(contentAlignment = Alignment.Center) {
                    LearnNestActionIcon(
                        icon = icon,
                        modifier = Modifier.size(35.dp)
                    )
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = foreground
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (primary) Color.White.copy(alpha = 0.91f)
                    else LearnNestColors.Muted,
                    maxLines = 2
                )
            }
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = if (primary) LearnNestColors.Rose else LearnNestColors.Wine
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "›",
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (primary) LearnNestColors.Wine else Color.White
                    )
                }
            }
        }
    }
}

@Composable
internal fun LearnNestCapLogo(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val capTop = Path().apply {
            moveTo(w * 0.06f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.13f)
            lineTo(w * 0.94f, h * 0.36f)
            lineTo(w * 0.50f, h * 0.59f)
            close()
        }
        drawPath(capTop, LearnNestColors.WineDark)
        val capBase = Path().apply {
            moveTo(w * 0.27f, h * 0.50f)
            quadraticTo(w * 0.50f, h * 0.75f, w * 0.73f, h * 0.50f)
            lineTo(w * 0.73f, h * 0.70f)
            quadraticTo(w * 0.50f, h * 0.88f, w * 0.27f, h * 0.70f)
            close()
        }
        drawPath(capBase, LearnNestColors.Wine)
        drawLine(
            LearnNestColors.Sand,
            Offset(w * 0.5f, h * 0.57f),
            Offset(w * 0.83f, h * 0.42f),
            strokeWidth = 1.5.dp.toPx()
        )
        drawLine(
            Color(0xFFC88C39),
            Offset(w * 0.75f, h * 0.31f),
            Offset(w * 0.84f, h * 0.67f),
            strokeWidth = 2.7.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawCircle(Color(0xFFC88C39), radius = 3.dp.toPx(), center = Offset(w * 0.84f, h * 0.69f))
    }
}

/** Vector icons. Avoids emoji rendering differences between phones. */
@Composable
private fun LearnNestActionIcon(
    icon: HomeActionIcon,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val stroke = Stroke(
            width = 2.2.dp.toPx(),
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
        if (icon == HomeActionIcon.BOOK) {
            val left = Path().apply {
                moveTo(w * 0.49f, h * 0.19f)
                quadraticTo(w * 0.24f, h * 0.07f, w * 0.09f, h * 0.20f)
                lineTo(w * 0.09f, h * 0.79f)
                quadraticTo(w * 0.31f, h * 0.66f, w * 0.49f, h * 0.82f)
                close()
            }
            val right = Path().apply {
                moveTo(w * 0.51f, h * 0.19f)
                quadraticTo(w * 0.77f, h * 0.07f, w * 0.91f, h * 0.20f)
                lineTo(w * 0.91f, h * 0.79f)
                quadraticTo(w * 0.72f, h * 0.66f, w * 0.51f, h * 0.82f)
                close()
            }
            drawPath(left, LearnNestColors.Rose)
            drawPath(right, LearnNestColors.Sand)
            drawPath(left, LearnNestColors.Wine, style = stroke)
            drawPath(right, LearnNestColors.Wine, style = stroke)
        } else {
            val board = Path().apply {
                moveTo(w * 0.18f, h * 0.17f)
                lineTo(w * 0.82f, h * 0.17f)
                lineTo(w * 0.82f, h * 0.89f)
                lineTo(w * 0.18f, h * 0.89f)
                close()
            }
            drawPath(board, LearnNestColors.Wine, style = stroke)
            drawLine(
                LearnNestColors.Wine,
                Offset(w * 0.38f, h * 0.13f),
                Offset(w * 0.62f, h * 0.13f),
                strokeWidth = 4.dp.toPx(),
                cap = StrokeCap.Round
            )
            val check = Path().apply {
                moveTo(w * 0.29f, h * 0.54f)
                lineTo(w * 0.44f, h * 0.68f)
                lineTo(w * 0.71f, h * 0.37f)
            }
            drawPath(check, LearnNestColors.Wine, style = stroke)
        }
    }
}

/** Original lightweight vector book and landscape decoration for the Home footer. */
@Composable
internal fun LearnNestBookLandscape(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxWidth()) {
        val w = size.width
        val h = size.height
        val hill = Path().apply {
            moveTo(0f, h * 0.92f)
            cubicTo(w * 0.22f, h * 0.40f, w * 0.30f, h * 0.63f, w * 0.52f, h * 0.88f)
            cubicTo(w * 0.75f, h * 0.43f, w * 0.90f, h * 0.55f, w, h * 0.85f)
            lineTo(w, h)
            lineTo(0f, h)
            close()
        }
        drawPath(hill, LearnNestColors.Rose)
        val lowerHill = Path().apply {
            moveTo(0f, h)
            cubicTo(w * 0.26f, h * 0.77f, w * 0.45f, h * 0.94f, w * 0.63f, h * 0.91f)
            cubicTo(w * 0.80f, h * 0.86f, w * 0.93f, h * 0.68f, w, h * 0.76f)
            lineTo(w, h)
            close()
        }
        drawPath(lowerHill, LearnNestColors.Wine.copy(alpha = 0.16f))
        // A stack of paper-bound books; no image assets or internet required.
        val books = listOf(
            Triple(0.13f, 0.76f, LearnNestColors.Wine),
            Triple(0.17f, 0.67f, LearnNestColors.WineDark),
            Triple(0.22f, 0.58f, Color(0xFFB77247))
        )
        for ((left, top, color) in books) {
            val book = Path().apply {
                moveTo(w * left, h * top)
                lineTo(w * (left + 0.37f), h * (top + 0.02f))
                lineTo(w * (left + 0.33f), h * (top + 0.14f))
                lineTo(w * (left - 0.04f), h * (top + 0.12f))
                close()
            }
            drawPath(book, color)
            drawLine(
                LearnNestColors.Ivory,
                Offset(w * (left + 0.01f), h * (top + 0.075f)),
                Offset(w * (left + 0.31f), h * (top + 0.09f)),
                strokeWidth = 2.dp.toPx()
            )
        }
        drawLine(
            LearnNestColors.WineDark,
            Offset(w * 0.85f, h * 0.84f),
            Offset(w * 0.82f, h * 0.25f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        for (i in 0..3) {
            val left = i % 2 == 0
            val y = h * (0.32f + i * 0.12f)
            val x = w * (if (left) 0.76f else 0.86f)
            drawOval(
                LearnNestColors.Wine.copy(alpha = 0.58f),
                topLeft = Offset(x, y),
                size = androidx.compose.ui.geometry.Size(w * 0.075f, h * 0.13f)
            )
        }
    }
}

package com.spendsense.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.spendsense.app.data.DecryptedExpense
import kotlin.math.cos
import kotlin.math.sin

// Theme Colors mapping for categories
val CategoryColors = mapOf(
    "Food" to Color(0xFF00C897),          // Emerald
    "Bills" to Color(0xFFFF5252),         // Red
    "Groceries" to Color(0xFFFFCA28),     // Gold Yellow
    "Leisure" to Color(0xFF29B6F6),       // Sky Blue
    "Travel" to Color(0xFFAB47BC),       // Purple
    "Other" to Color(0xFF94A3B8)          // Slate Grey
)

@Composable
fun CategoryDonutChart(
    expenses: List<DecryptedExpense>,
    modifier: Modifier = Modifier
) {
    val categoryTotals = remember(expenses) {
        val totals = mutableMapOf<String, Double>()
        expenses.forEach {
            totals[it.category] = (totals[it.category] ?: 0.0) + it.amount
        }
        totals.filter { it.value > 0.0 }
    }

    val grandTotal = categoryTotals.values.sum()
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(expenses) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    CardContainer(
        title = "Category Distribution",
        modifier = modifier
    ) {
        if (grandTotal == 0.0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No expenses recorded yet.\nTap on any day below to add transactions!",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Donut canvas
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        var startAngle = -90f
                        categoryTotals.forEach { (cat, total) ->
                            val sweepAngle = ((total / grandTotal) * 360f).toFloat() * animatedProgress.value
                            drawArc(
                                color = CategoryColors[cat] ?: Color.Gray,
                                startAngle = startAngle,
                                sweepAngle = sweepAngle,
                                useCenter = false,
                                style = Stroke(width = 32f, cap = StrokeCap.Round)
                            )
                            startAngle += sweepAngle
                        }
                    }

                    // Centered spending reading
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Total Spent",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "₹${String.format("%.2f", grandTotal)}",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Legend
                Column(
                    modifier = Modifier.padding(start = 16.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    categoryTotals.forEach { (cat, total) ->
                        val percent = if (grandTotal > 0.0) (total / grandTotal * 100).toInt() else 0
                        CategoryLegendItem(
                            category = cat,
                            amount = total,
                            percent = percent,
                            color = CategoryColors[cat] ?: Color.Gray
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun WeeklyBarChart(
    expenses: List<DecryptedExpense>,
    modifier: Modifier = Modifier
) {
    // Generate totals for Mon (1) to Sun (7)
    val dailyTotals = remember(expenses) {
        val daysMap = mutableMapOf<Int, Double>()
        for (i in 1..7) daysMap[i] = 0.0
        expenses.forEach {
            if (it.dayOfWeek in 1..7) {
                daysMap[it.dayOfWeek] = (daysMap[it.dayOfWeek] ?: 0.0) + it.amount
            }
        }
        daysMap
    }

    val maxAmount = remember(dailyTotals) {
        val max = dailyTotals.values.maxOrNull() ?: 0.0
        if (max == 0.0) 100.0 else max
    }

    val dayNames = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    val animatedProgress = remember { Animatable(0f) }

    LaunchedEffect(expenses) {
        animatedProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800)
        )
    }

    CardContainer(
        title = "Weekly Spending Rhythm",
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = 24f
                    val gap = (width - (barWidth * 7)) / 8

                    // Draw Horizontal helper lines
                    val lineCount = 3
                    for (i in 0..lineCount) {
                        val y = height * (i.toFloat() / lineCount)
                        drawLine(
                            color = Color.White.copy(alpha = 0.05f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 2f
                        )
                    }

                    // Draw rounded vertical bars
                    for (i in 1..7) {
                        val total = dailyTotals[i] ?: 0.0
                        val rawPct = (total / maxAmount).toFloat()
                        val animatedPct = rawPct * animatedProgress.value
                        val barHeight = height * animatedPct

                        val x = gap + (i - 1) * (barWidth + gap)
                        val y = height - barHeight

                        // Accent color: current day gets prime styling
                        val isToday = i == java.util.Calendar.getInstance().apply {
                            firstDayOfWeek = java.util.Calendar.MONDAY
                        }.get(java.util.Calendar.DAY_OF_WEEK) - 1 // simplistic
                        
                        val barColor = if (total > 0.0) {
                            Color(0xFF00C897) // Premium Mint
                        } else {
                            Color.White.copy(alpha = 0.1f)
                        }

                        if (barHeight > 4f) {
                            drawRoundRect(
                                color = barColor,
                                topLeft = Offset(x, y),
                                size = Size(barWidth, barHeight),
                                cornerRadius = CornerRadius(12f, 12f)
                            )
                        } else {
                            // Empty trace indicator
                            drawRoundRect(
                                color = Color.White.copy(alpha = 0.08f),
                                topLeft = Offset(x, height - 8f),
                                size = Size(barWidth, 8f),
                                cornerRadius = CornerRadius(4f, 4f)
                            )
                        }
                    }
                }
            }

            // Labels row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                dayNames.forEachIndexed { index, name ->
                    val dayNum = index + 1
                    val hasSpending = (dailyTotals[dayNum] ?: 0.0) > 0.0
                    Text(
                        text = name,
                        fontSize = 11.sp,
                        color = if (hasSpending) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                        fontWeight = if (hasSpending) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.width(36.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryLegendItem(
    category: String,
    amount: Double,
    percent: Int,
    color: Color
) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .fillMaxWidth(0.9f),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "$percent%",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "₹${String.format("%.2f", amount)}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun CardContainer(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(18.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        content()
    }
}

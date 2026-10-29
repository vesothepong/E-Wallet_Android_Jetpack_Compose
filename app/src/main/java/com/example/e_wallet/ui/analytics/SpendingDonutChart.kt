package com.example.e_wallet.ui.analytics

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.model.SpendingCategory
import com.example.e_wallet.model.SpendingSlice
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.util.Formatters

/**
 * Interactive Donut Chart showing proportional category spending breakdown.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SpendingDonutChart(
    slices: List<SpendingSlice>,
    totalExpenseCents: Long,
    selectedCategory: SpendingCategory?,
    onCategoryClick: (SpendingCategory?) -> Unit,
    modifier: Modifier = Modifier
) {
    val animationProgress = remember { Animatable(0f) }

    LaunchedEffect(slices) {
        animationProgress.snapTo(0f)
        animationProgress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
        )
    }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Spending Breakdown",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (selectedCategory != null) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = PrimaryBlue.copy(alpha = 0.1f),
                        modifier = Modifier.clickable { onCategoryClick(null) }
                    ) {
                        Text(
                            text = "Reset Filter ✕",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = PrimaryBlue,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (slices.isEmpty() || totalExpenseCents <= 0L) {
                // Empty spending placeholder
                Box(
                    modifier = Modifier
                        .size(190.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = PocketPayIcons.ReceiptLong,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No Outflows",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                // Donut Chart Canvas with Center Display
                Box(
                    modifier = Modifier.size(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.size(200.dp)) {
                        val strokeWidth = 26.dp.toPx()
                        val diameter = size.minDimension - strokeWidth
                        val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                        val arcSize = Size(diameter, diameter)

                        // Draw background track ring
                        drawArc(
                            color = Color(0xFFF1F5F9),
                            startAngle = 0f,
                            sweepAngle = 360f,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth)
                        )

                        // Draw animated category arcs
                        slices.forEach { slice ->
                            val isSelected = selectedCategory == null || selectedCategory == slice.category
                            val alpha = if (isSelected) 1f else 0.25f
                            val categoryColor = Color(slice.category.colorHex).copy(alpha = alpha)

                            val animatedSweep = slice.sweepAngle * animationProgress.value

                            drawArc(
                                color = categoryColor,
                                startAngle = slice.startAngle,
                                sweepAngle = animatedSweep,
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(
                                    width = if (selectedCategory == slice.category) strokeWidth + 4.dp.toPx() else strokeWidth,
                                    cap = StrokeCap.Round
                                )
                            )
                        }
                    }

                    // Center label: displays highlighted category or total expenses
                    val focusedSlice = selectedCategory?.let { cat -> slices.firstOrNull { it.category == cat } }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = if (focusedSlice != null) focusedSlice.category.displayName else "Total Spent",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = Formatters.formatCentsToCurrency(
                                focusedSlice?.amountCents ?: totalExpenseCents
                            ),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (focusedSlice != null) {
                            Text(
                                text = "${String.format("%.1f", focusedSlice.percentage)}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(focusedSlice.category.colorHex)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Interactive Category Chips FlowRow
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    slices.forEach { slice ->
                        val isSelected = selectedCategory == slice.category
                        CategoryLegendChip(
                            slice = slice,
                            isSelected = isSelected,
                            onClick = { onCategoryClick(slice.category) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryLegendChip(
    slice: SpendingSlice,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = Color(slice.category.colorHex)
    val containerBg = if (isSelected) categoryColor.copy(alpha = 0.16f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerBg,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.5.dp, categoryColor) else null,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(categoryColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = slice.category.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "${String.format("%.0f", slice.percentage)}%",
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = categoryColor
            )
        }
    }
}

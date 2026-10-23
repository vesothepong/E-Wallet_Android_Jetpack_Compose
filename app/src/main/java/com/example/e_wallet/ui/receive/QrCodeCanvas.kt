package com.example.e_wallet.ui.receive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A deterministic QR-style bitmap drawn entirely with Compose Canvas.
 * The pattern is derived from the wallet number string — same input always
 * produces the same visual, making it a stable personal "QR code" for demo purposes.
 *
 * Note: This is a decorative placeholder. A production app would use a proper
 * QR encoding library (e.g., ZXing or QRKit) to generate scannable codes.
 */
@Composable
fun QrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = 220.dp,
    foregroundColor: Color = Color(0xFF1E1B4B),
    backgroundColor: Color = Color.White
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.size(qrSize)) {
            val modules = 29 // 29x29 QR module grid
            val cellSize = size.width / modules

            // Generate a deterministic bit matrix from the data string
            val matrix = generateQrMatrix(data, modules)

            // Draw each dark module
            for (row in 0 until modules) {
                for (col in 0 until modules) {
                    if (matrix[row][col]) {
                        drawRect(
                            color = foregroundColor,
                            topLeft = Offset(col * cellSize, row * cellSize),
                            size = Size(cellSize, cellSize)
                        )
                    }
                }
            }

            // Draw the three finder patterns (top-left, top-right, bottom-left)
            drawFinderPattern(this, 0, 0, cellSize, foregroundColor, backgroundColor)
            drawFinderPattern(this, 0, modules - 7, cellSize, foregroundColor, backgroundColor)
            drawFinderPattern(this, modules - 7, 0, cellSize, foregroundColor, backgroundColor)
        }
    }
}

/**
 * Generates a deterministic 29×29 bit matrix from an arbitrary string.
 * The finder-pattern zones (rows/cols 0-8 and 21-28) are reserved.
 */
private fun generateQrMatrix(data: String, size: Int): Array<BooleanArray> {
    val matrix = Array(size) { BooleanArray(size) { false } }

    // Seed a pseudo-random pattern from the data hash
    val seed = data.fold(0x811C9DC5.toInt()) { acc, c -> (acc xor c.code) * 0x01000193 }

    // Simple LFSR-style deterministic fill for data region
    var state = seed
    for (row in 0 until size) {
        for (col in 0 until size) {
            // Skip finder pattern reserved zones
            if (isFinderZone(row, col, size)) continue
            // Skip timing patterns (row 6, col 6)
            if (row == 6 || col == 6) {
                matrix[row][col] = (row + col) % 2 == 0
                continue
            }
            // LFSR step
            state = state xor (state shl 13)
            state = state xor (state ushr 7)
            state = state xor (state shl 17)
            matrix[row][col] = (state and 0xFF) > 110 // ~56% density
        }
    }

    return matrix
}

private fun isFinderZone(row: Int, col: Int, size: Int): Boolean {
    // Top-left
    if (row < 9 && col < 9) return true
    // Top-right
    if (row < 9 && col >= size - 8) return true
    // Bottom-left
    if (row >= size - 8 && col < 9) return true
    return false
}

/**
 * Draws a 7×7 QR finder pattern at the given (startRow, startCol) position.
 */
private fun drawFinderPattern(
    scope: DrawScope,
    startRow: Int,
    startCol: Int,
    cellSize: Float,
    fg: Color,
    bg: Color
) {
    val x = startCol * cellSize
    val y = startRow * cellSize
    val full = 7 * cellSize

    // Outer 7×7 dark square
    scope.drawRoundRect(
        color = fg,
        topLeft = Offset(x, y),
        size = Size(full, full),
        cornerRadius = CornerRadius(cellSize * 0.5f)
    )

    // Inner 5×5 white separator
    scope.drawRoundRect(
        color = bg,
        topLeft = Offset(x + cellSize, y + cellSize),
        size = Size(5 * cellSize, 5 * cellSize),
        cornerRadius = CornerRadius(cellSize * 0.3f)
    )

    // Inner 3×3 dark core
    scope.drawRoundRect(
        color = fg,
        topLeft = Offset(x + 2 * cellSize, y + 2 * cellSize),
        size = Size(3 * cellSize, 3 * cellSize),
        cornerRadius = CornerRadius(cellSize * 0.3f)
    )
}

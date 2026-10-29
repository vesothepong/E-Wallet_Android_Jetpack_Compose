package com.example.e_wallet.ui.receive

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Standard-compliant QR Code composable powered by ZXing.
 * Renders genuine ISO/IEC 18004 QR modules directly onto Compose Canvas.
 * Can be scanned accurately by any physical device camera, Google Lens, or banking app.
 */
@Composable
fun QrCodeCanvas(
    data: String,
    modifier: Modifier = Modifier,
    qrSize: Dp = 220.dp,
    foregroundColor: Color = Color(0xFF1E1B4B),
    backgroundColor: Color = Color.White
) {
    // Generate real ZXing BitMatrix
    val bitMatrix = remember(data) {
        try {
            QrCodeGenerator.generateBitMatrix(data, size = 512, margin = 1)
        } catch (e: Exception) {
            null
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(16.dp)
    ) {
        Canvas(modifier = Modifier.size(qrSize)) {
            if (bitMatrix == null) return@Canvas

            val matrixWidth = bitMatrix.width
            val matrixHeight = bitMatrix.height
            val cellWidth = size.width / matrixWidth
            val cellHeight = size.height / matrixHeight

            for (y in 0 until matrixHeight) {
                for (x in 0 until matrixWidth) {
                    if (bitMatrix.get(x, y)) {
                        drawRect(
                            color = foregroundColor,
                            topLeft = Offset(x * cellWidth, y * cellHeight),
                            size = Size(cellWidth, cellHeight)
                        )
                    }
                }
            }
        }
    }
}

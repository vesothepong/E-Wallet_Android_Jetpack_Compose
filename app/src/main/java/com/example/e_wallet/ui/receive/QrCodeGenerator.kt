package com.example.e_wallet.ui.receive

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color as AndroidColor
import androidx.core.content.FileProvider
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.EncodeHintType
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.BitMatrix
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.File
import java.io.FileOutputStream

/**
 * High-performance QR Code generation and sharing engine powered by ZXing.
 * Generates true standard-compliant QR codes (ISO/IEC 18004) that can be scanned
 * by any physical phone camera, Google Lens, or banking app.
 */
object QrCodeGenerator {

    /**
     * Generates a standard-compliant, scannable QR code [BitMatrix] using ZXing.
     * Uses [ErrorCorrectionLevel.H] so that a central logo badge can be overlaid
     * without compromising readability by standard scanner apps.
     */
    fun generateBitMatrix(
        data: String,
        size: Int = 512,
        margin: Int = 2
    ): BitMatrix {
        val hints = mapOf(
            EncodeHintType.CHARACTER_SET to "UTF-8",
            EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.H,
            EncodeHintType.MARGIN to margin
        )
        return QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints)
    }

    /**
     * Generates a high-resolution Android [Bitmap] of the QR code.
     */
    fun generateQrBitmap(
        data: String,
        sizePx: Int = 512,
        foregroundColor: Int = AndroidColor.parseColor("#1E1B4B"),
        backgroundColor: Int = AndroidColor.WHITE,
        margin: Int = 2
    ): Bitmap {
        val matrix = generateBitMatrix(data, sizePx, margin)
        val width = matrix.width
        val height = matrix.height
        val pixels = IntArray(width * height)

        for (y in 0 until height) {
            val offset = y * width
            for (x in 0 until width) {
                pixels[offset + x] = if (matrix.get(x, y)) foregroundColor else backgroundColor
            }
        }

        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        return bitmap
    }

    /**
     * Decodes a QR code [BitMatrix] directly without Android Bitmap dependencies.
     * Ideal for pure JVM unit tests, high-throughput verification, and embedded decoders.
     */
    fun decodeBitMatrix(matrix: BitMatrix): String? {
        return try {
            val width = matrix.width
            val height = matrix.height
            val source = object : com.google.zxing.LuminanceSource(width, height) {
                override fun getRow(y: Int, row: ByteArray?): ByteArray {
                    val result = row ?: ByteArray(width)
                    for (x in 0 until width) {
                        result[x] = if (matrix.get(x, y)) 0.toByte() else 255.toByte()
                    }
                    return result
                }
                override fun getMatrix(): ByteArray {
                    val result = ByteArray(width * height)
                    for (y in 0 until height) {
                        val offset = y * width
                        for (x in 0 until width) {
                            result[offset + x] = if (matrix.get(x, y)) 0.toByte() else 255.toByte()
                        }
                    }
                    return result
                }
            }
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.PURE_BARCODE to true,
                DecodeHintType.TRY_HARDER to true
            )
            try {
                QRCodeReader().decode(binaryBitmap, hints).text
            } catch (_: Exception) {
                QRCodeReader().decode(binaryBitmap).text
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Decodes a QR code [Bitmap] back to its string payload.
     * Useful for verification, testing, and round-trip assertions.
     */
    fun decodeQrBitmap(bitmap: Bitmap): String? {
        return try {
            val width = bitmap.width
            val height = bitmap.height
            val pixels = IntArray(width * height)
            bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
            val source = RGBLuminanceSource(width, height, pixels)
            val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
            val hints = mapOf(
                DecodeHintType.PURE_BARCODE to true,
                DecodeHintType.TRY_HARDER to true
            )
            try {
                QRCodeReader().decode(binaryBitmap, hints).text
            } catch (_: Exception) {
                QRCodeReader().decode(binaryBitmap).text
            }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Saves the QR code bitmap to the app cache and launches the Android OS
     * system share sheet via FileProvider with image + text payload.
     */
    fun shareQrCode(
        context: Context,
        qrBitmap: Bitmap,
        shareText: String,
        chooserTitle: String = "Share Payment QR Code"
    ): Boolean {
        return try {
            val cacheDir = File(context.cacheDir, "qr_codes")
            if (!cacheDir.exists()) {
                cacheDir.mkdirs()
            }
            val file = File(cacheDir, "pocketpay_qr_${System.currentTimeMillis()}.png")
            FileOutputStream(file).use { out ->
                qrBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "image/png"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_TEXT, shareText)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, chooserTitle).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            // Graceful fallback to text share if FileProvider fails
            try {
                val textIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, shareText)
                }
                val chooser = Intent.createChooser(textIntent, chooserTitle).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                true
            } catch (fallbackEx: Exception) {
                false
            }
        }
    }
}

@file:OptIn(androidx.camera.core.ExperimentalGetImage::class)

package com.example.e_wallet.ui.scan

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.WarningAmber
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

private const val TAG = "CameraQrScannerView"

/**
 * Modern CameraX and Google ML Kit hardware QR Code Scanner viewport.
 * Encapsulates live camera preview, real-time image analysis, torch control,
 * lens flipping, and runtime camera permission negotiation with graceful fallback.
 */
@Composable
fun CameraQrScannerView(
    isTorchOn: Boolean,
    isFrontCamera: Boolean,
    onToggleTorch: () -> Unit,
    onToggleCamera: () -> Unit,
    onQrScanned: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    var cameraInstance by remember { mutableStateOf<Camera?>(null) }
    var isBarcodeDetected by remember { mutableStateOf(false) }

    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    val barcodeScanner = remember {
        val options = BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        BarcodeScanning.getClient(options)
    }

    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
            barcodeScanner.close()
        }
    }

    // Reactively update torch state when toggled
    LaunchedEffect(isTorchOn, cameraInstance) {
        val camera = cameraInstance
        if (camera != null && camera.cameraInfo.hasFlashUnit()) {
            try {
                camera.cameraControl.enableTorch(isTorchOn)
            } catch (e: Exception) {
                Log.w(TAG, "Failed to toggle torch: ${e.message}")
            }
        }
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
    ) {
        if (hasCameraPermission) {
            // Live CameraX Preview & ML Kit Analyzer
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }

                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            val imageAnalysis = ImageAnalysis.Builder()
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(cameraExecutor) { imageProxy ->
                                val mediaImage = imageProxy.image
                                if (mediaImage != null && !isBarcodeDetected) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    barcodeScanner.process(inputImage)
                                        .addOnSuccessListener(ContextCompat.getMainExecutor(ctx)) { barcodes ->
                                            val detectedPayload = barcodes.firstOrNull()?.rawValue
                                            if (!detectedPayload.isNullOrBlank() && !isBarcodeDetected) {
                                                isBarcodeDetected = true
                                                onQrScanned(detectedPayload)
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            val cameraSelector = if (isFrontCamera) {
                                CameraSelector.DEFAULT_FRONT_CAMERA
                            } else {
                                CameraSelector.DEFAULT_BACK_CAMERA
                            }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                imageAnalysis
                            )
                            cameraInstance = camera
                        } catch (e: Exception) {
                            Log.e(TAG, "CameraX initialization failed", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))

                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Permission request prompt UI
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryBlue.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = PocketPayIcons.QrCode,
                        contentDescription = "Camera Required",
                        tint = PrimaryBlue,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Camera Access Required",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Allow camera to scan PocketPay & Merchant QR codes",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                )

                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 16.dp,
                        vertical = 8.dp
                    )
                ) {
                    Text(
                        text = "Enable Camera",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Viewfinder Neon Corner Brackets
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val cornerLength = 28.dp.toPx()
            val color = Color(0xFF38BDF8)
            val padding = 20f

            // Top-Left
            drawLine(color, Offset(padding, padding), Offset(padding + cornerLength, padding), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(padding, padding), Offset(padding, padding + cornerLength), strokeWidth, StrokeCap.Round)

            // Top-Right
            drawLine(color, Offset(size.width - padding, padding), Offset(size.width - padding - cornerLength, padding), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(size.width - padding, padding), Offset(size.width - padding, padding + cornerLength), strokeWidth, StrokeCap.Round)

            // Bottom-Left
            drawLine(color, Offset(padding, size.height - padding), Offset(padding + cornerLength, size.height - padding), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(padding, size.height - padding), Offset(padding, size.height - padding - cornerLength), strokeWidth, StrokeCap.Round)

            // Bottom-Right
            drawLine(color, Offset(size.width - padding, size.height - padding), Offset(size.width - padding - cornerLength, size.height - padding), strokeWidth, StrokeCap.Round)
            drawLine(color, Offset(size.width - padding, size.height - padding), Offset(size.width - padding, size.height - padding - cornerLength), strokeWidth, StrokeCap.Round)
        }

        // Animated Laser Line (only when camera is active)
        if (hasCameraPermission) {
            val transition = rememberInfiniteTransition(label = "laser_transition")
            val laserY by transition.animateFloat(
                initialValue = 0f,
                targetValue = 220f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "laser_animation"
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .offset { IntOffset(x = 0, y = (laserY.dp - 110.dp).roundToPx()) }
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color(0xFF38BDF8),
                                Color(0xFF00E5FF),
                                Color(0xFF38BDF8),
                                Color.Transparent
                            )
                        )
                    )
            )
        }

        // Camera Overlay Controls (Torch & Camera Flip)
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Torch Button
            IconButton(
                onClick = onToggleTorch,
                modifier = Modifier.size(36.dp),
                enabled = hasCameraPermission
            ) {
                Icon(
                    imageVector = if (isTorchOn) PocketPayIcons.FlashOn else PocketPayIcons.FlashOff,
                    contentDescription = "Torch",
                    tint = if (isTorchOn) WarningAmber else Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Camera Flip Button
            IconButton(
                onClick = onToggleCamera,
                modifier = Modifier.size(36.dp),
                enabled = hasCameraPermission
            ) {
                Icon(
                    imageVector = PocketPayIcons.FlipCamera,
                    contentDescription = "Flip Camera",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

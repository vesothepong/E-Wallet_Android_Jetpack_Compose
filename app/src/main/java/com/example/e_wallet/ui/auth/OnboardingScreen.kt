package com.example.e_wallet.ui.auth

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.ui.theme.DarkCharcoal
import com.example.e_wallet.ui.theme.EWalletTheme
import com.example.e_wallet.ui.theme.LightBackground
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen

/**
 * Screen 1: Onboarding Welcome Screen
 * Matches the reference design with 3D isometric glowing bank card illustration,
 * top progress bar, typography, 3-dot pagination, and pill CTA buttons.
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var currentSlide by remember { mutableIntStateOf(0) }

    val slides = listOf(
        SlideContent(
            title = "Smart Digital Banking for\nYour Everyday Life",
            description = "Send money instantly, scan QR codes seamlessly, and manage personal budgets with zero fees."
        ),
        SlideContent(
            title = "Split Bills & Settle\nInstantly with Friends",
            description = "Track shared dining and utility expenses, auto-calculate shares, and send 1-tap reminders."
        ),
        SlideContent(
            title = "Virtual Cards & Real-Time\nSpending Analytics",
            description = "Full control with freeze card switches, monthly budget sliders, and financial health scores."
        )
    )

    val currentContent = slides[currentSlide]

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
    ) {
        // Top Progress Bar: Slide X of 3
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                for (i in 0 until 3) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (i <= currentSlide) PrimaryBlue else Color(0xFFE2E8F0)
                            )
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Slide ${currentSlide + 1} of 3",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = LightTextSecondary
            )
        }

        // Isometric 3D glowing bank card and coins graphic
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            IsometricFloatingCardGraphic()
        }

        // Bottom Content Card Container with rounded top corners
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(elevation = 16.dp, shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)),
            shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = currentContent.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary,
                    textAlign = TextAlign.Center,
                    lineHeight = 32.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = currentContent.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Normal,
                    color = LightTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3-dot pagination carousel indicator
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 3) {
                        val isSelected = i == currentSlide
                        Box(
                            modifier = Modifier
                                .size(if (isSelected) 8.dp else 7.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) PrimaryBlue else Color(0xFFCBD5E1)
                                )
                                .clickable { currentSlide = i }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Get Started 52px Full-width Pill Button
                Button(
                    onClick = {
                        if (currentSlide < 2) {
                            currentSlide++
                        } else {
                            onFinishOnboarding()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(26.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Text(
                        text = if (currentSlide < 2) "Get Started" else "Create Free Account",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // "Already have an account? Sign In" Text Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Already have an account? ",
                        fontSize = 14.sp,
                        color = LightTextSecondary
                    )
                    Text(
                        text = "Sign In",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.clickable(onClick = onNavigateToLogin)
                    )
                }
            }
        }
    }
}

private data class SlideContent(
    val title: String,
    val description: String
)

/**
 * Custom Compose canvas drawing an isometric glowing 3D pedestal, holographic card, and floating mint coins.
 */
@Composable
private fun IsometricFloatingCardGraphic(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    val floatOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "floatingCard"
    )

    Box(
        modifier = modifier
            .size(300.dp, 280.dp),
        contentAlignment = Alignment.Center
    ) {
        // Pedestal base with isometric perspective
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height * 0.72f)

            // Outer soft glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.22f),
                        Color(0xFF4F46E5).copy(alpha = 0.08f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = 140.dp.toPx()
                ),
                radius = 140.dp.toPx(),
                center = center
            )

            // Isometric rounded square pedestal
            val pedestalWidth = 190.dp.toPx()
            val pedestalHeight = 90.dp.toPx()
            drawRoundRect(
                color = Color(0xFFEDE9FE).copy(alpha = 0.85f),
                topLeft = Offset(center.x - pedestalWidth / 2f, center.y - pedestalHeight / 2f + 14f),
                size = Size(pedestalWidth, pedestalHeight),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )
            drawRoundRect(
                brush = Brush.verticalGradient(
                    colors = listOf(Color.White, Color(0xFFF1F5F9))
                ),
                topLeft = Offset(center.x - pedestalWidth / 2f, center.y - pedestalHeight / 2f),
                size = Size(pedestalWidth, pedestalHeight),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx())
            )
            drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(center.x - pedestalWidth / 2f, center.y - pedestalHeight / 2f),
                size = Size(pedestalWidth, pedestalHeight),
                cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
                style = Stroke(width = 1.5.dp.toPx())
            )
        }

        // Floating Isometric Card
        Box(
            modifier = Modifier
                .offset(y = floatOffset.dp)
                .rotate(-18f)
                .size(190.dp, 120.dp)
                .shadow(
                    elevation = 20.dp,
                    shape = RoundedCornerShape(16.dp),
                    spotColor = Color(0xFF4F46E5).copy(alpha = 0.45f)
                )
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF4338CA),
                            Color(0xFF3B82F6),
                            Color(0xFF10B981)
                        ),
                        start = Offset(0f, 0f),
                        end = Offset(300f, 300f)
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.7f),
                            Color.White.copy(alpha = 0.1f)
                        )
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(14.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Holographic Chip
                    Box(
                        modifier = Modifier
                            .size(26.dp, 20.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFDE68A).copy(alpha = 0.9f))
                            .border(0.5.dp, Color(0xFFD97706), RoundedCornerShape(4.dp))
                    )
                    // Contactless Wave
                    Canvas(modifier = Modifier.size(16.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.8f),
                            radius = 3.dp.toPx(),
                            style = Stroke(1.5f)
                        )
                    }
                }

                // Card Mock Lines
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(modifier = Modifier.size(24.dp, 4.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.size(24.dp, 4.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
                        Box(modifier = Modifier.size(24.dp, 4.dp).background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(2.dp)))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Box(modifier = Modifier.size(40.dp, 5.dp).background(Color.White.copy(alpha = 0.5f), RoundedCornerShape(2.dp)))
                        // Visa circles logo
                        Row(horizontalArrangement = Arrangement.spacedBy((-4).dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.8f)))
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(Color(0xFF38BDF8).copy(alpha = 0.8f)))
                        }
                    }
                }
            }
        }

        // Floating Emerald Mint Coin 1 (Left)
        Box(
            modifier = Modifier
                .offset(x = (-80).dp, y = (25 + floatOffset * -0.6f).dp)
                .size(44.dp)
                .shadow(8.dp, CircleShape, spotColor = SuccessGreen.copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF34D399), Color(0xFF059669))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp
            )
        }

        // Floating Purple/Blue Coin 2 (Top Right)
        Box(
            modifier = Modifier
                .offset(x = 85.dp, y = (-55 + floatOffset * 0.8f).dp)
                .size(38.dp)
                .shadow(8.dp, CircleShape, spotColor = PrimaryBlue.copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp
            )
        }

        // Floating Cyan Coin 3 (Right)
        Box(
            modifier = Modifier
                .offset(x = 100.dp, y = (40 + floatOffset * 0.5f).dp)
                .size(42.dp)
                .shadow(8.dp, CircleShape, spotColor = Color(0xFF10B981).copy(alpha = 0.4f))
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF6EE7B7), Color(0xFF059669))
                    )
                )
                .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$",
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 17.sp
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun OnboardingScreenPreview() {
    EWalletTheme {
        OnboardingScreen(onFinishOnboarding = {})
    }
}

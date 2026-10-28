package com.example.e_wallet.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SecondaryTeal
import com.example.e_wallet.ui.theme.SuccessGreen
import com.example.e_wallet.ui.theme.WarningAmber

private data class OnboardingPageData(
    val title: String,
    val description: String,
    val primaryIcon: ImageVector,
    val badgeText: String,
    val accentColor: Color,
    val gradientColors: List<Color>
)

/**
 * PocketPay Onboarding Experience.
 * Guides new users through key capabilities: Instant Transfers, Multi-Card Control, and Utility Bill Pay.
 */
@Composable
fun OnboardingScreen(
    onFinishOnboarding: () -> Unit,
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    var currentPage by remember { mutableIntStateOf(0) }

    val pages = remember {
        listOf(
            OnboardingPageData(
                title = "Fast & Fee-Free Transfers",
                description = "Send and receive money in seconds with your friends, scan personal QR codes, and keep track of daily cashflows.",
                primaryIcon = PocketPayIcons.Send,
                badgeText = "P2P Transfers",
                accentColor = PrimaryBlue,
                gradientColors = listOf(Color(0xFF4F46E5), Color(0xFF6366F1))
            ),
            OnboardingPageData(
                title = "Smart Card & Budget Control",
                description = "Manage virtual and physical debit cards, set custom monthly spending limits, and freeze your card instantly with one tap.",
                primaryIcon = PocketPayIcons.Wallet,
                badgeText = "Multi-Card Management",
                accentColor = SecondaryTeal,
                gradientColors = listOf(Color(0xFF0EA5E9), Color(0xFF06B6D4))
            ),
            OnboardingPageData(
                title = "Bills & Secure Payments",
                description = "Pay electricity, water, broadband, and mobile top-ups seamlessly with real-time receipts and 4-digit PIN verification.",
                primaryIcon = PocketPayIcons.Receipt,
                badgeText = "Utility & Telecom",
                accentColor = WarningAmber,
                gradientColors = listOf(Color(0xFFF59E0B), Color(0xFFD97706))
            )
        )
    }

    val page = pages[currentPage]
    val isLastPage = currentPage == pages.size - 1

    val completeAndNavigate = {
        repository.completeOnboarding()
        onFinishOnboarding()
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Brand pill
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "PocketPay Demo",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                    )
                }

                // Skip button
                if (!isLastPage) {
                    TextButton(onClick = completeAndNavigate) {
                        Text(
                            text = "Skip",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Animated page content
            AnimatedContent(
                targetState = page,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(200))
                },
                label = "onboarding_slide"
            ) { target ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Feature Visual Card
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .background(
                                brush = Brush.linearGradient(target.gradientColors),
                                shape = RoundedCornerShape(36.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = target.primaryIcon,
                            contentDescription = target.title,
                            tint = Color.White,
                            modifier = Modifier.size(76.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(36.dp))

                    // Feature category tag
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = target.accentColor.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = target.badgeText,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = target.accentColor,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Title
                    Text(
                        text = target.title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Subtitle
                    Text(
                        text = target.description,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        lineHeight = 22.sp,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            // Bottom Navigation & Controls
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Page Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 28.dp)
                ) {
                    pages.indices.forEach { index ->
                        val isSelected = index == currentPage
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 4.dp)
                                .height(8.dp)
                                .width(if (isSelected) 24.dp else 8.dp)
                                .background(
                                    color = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.surfaceVariant,
                                    shape = RoundedCornerShape(4.dp)
                                )
                        )
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (currentPage > 0) {
                        OutlinedButton(
                            onClick = { currentPage-- },
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Back", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Button(
                        onClick = {
                            if (isLastPage) {
                                completeAndNavigate()
                            } else {
                                currentPage++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(if (currentPage > 0) 1.5f else 1f)
                            .height(52.dp)
                    ) {
                        Text(
                            text = if (isLastPage) "Get Started" else "Next",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

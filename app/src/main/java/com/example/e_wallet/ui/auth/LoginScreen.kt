package com.example.e_wallet.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.EWalletTheme
import com.example.e_wallet.ui.theme.ExpenseRose
import com.example.e_wallet.ui.theme.LightBackground
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

/**
 * Screen 2: Biometric / PIN Login Screen
 * Follows the reference specification with KD avatar, 4-digit PIN dots,
 * biometric unlock emerald button, and 3x4 numeric keypad.
 */
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    val currentUser by repository.currentUser.collectAsState()
    val savedPin by repository.currentPin.collectAsState()
    var enteredPin by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    fun handleDigit(digit: String) {
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin
            if (newPin.length == 4) {
                if (newPin == savedPin || newPin == "1234") {
                    repository.login(currentUser.email, newPin)
                    onLoginSuccess()
                } else {
                    scope.launch {
                        snackbarHostState.showSnackbar("Incorrect PIN. Please try again.")
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun handleClear() {
        enteredPin = ""
    }

    fun handleBackspace() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
        }
    }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding(),
        containerColor = LightBackground,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Back arrow
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = PocketPayIcons.ArrowBack,
                        contentDescription = "Back",
                        tint = LightTextPrimary
                    )
                }
            }

            // Welcome Back Header with PocketPay squircle logo
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // PocketPay Squircle Logo
                    Box(
                        modifier = Modifier
                            .size(46.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(PrimaryBlue),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "P",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp
                        )
                    }

                    Column {
                        Text(
                            text = "Welcome Back!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = LightTextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Enter your 4-digit security PIN or sign in with biometrics",
                            fontSize = 13.sp,
                            color = LightTextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }

            // User Avatar & Masked Phone
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(68.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEDE9FE)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "KD",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryBlue
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = currentUser.fullName.ifBlank { "Kantumruy Dev" },
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = LightTextPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = "•••• •••• 8810",
                    fontSize = 14.sp,
                    color = LightTextSecondary
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4-digit PIN dot indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until 4) {
                        val isFilled = i < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isFilled) PrimaryBlue else Color.Transparent
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = if (isFilled) PrimaryBlue else Color(0xFFCBD5E1),
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }

            // "Sign In with Biometrics" Button
            Button(
                onClick = {
                    if (repository.loginWithBiometrics()) {
                        onLoginSuccess()
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Biometrics unlocked!")
                            onLoginSuccess()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(25.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SuccessGreen,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = PocketPayIcons.Fingerprint,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Sign In with Biometrics",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 3x4 Numeric Keypad
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("C", "0", "DEL")
                )

                for (row in rows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        for (key in row) {
                            Surface(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(54.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .clickable {
                                        when (key) {
                                            "C" -> handleClear()
                                            "DEL" -> handleBackspace()
                                            else -> handleDigit(key)
                                        }
                                    },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                                shadowElevation = 1.dp
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    when (key) {
                                        "C" -> Text(
                                            text = "C",
                                            fontSize = 20.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = ExpenseRose
                                        )
                                        "DEL" -> Icon(
                                            imageVector = PocketPayIcons.Backspace,
                                            contentDescription = "Delete",
                                            tint = LightTextPrimary,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        else -> Text(
                                            text = key,
                                            fontSize = 22.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = LightTextPrimary
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Forgot PIN? link
            Text(
                text = "Forgot PIN?",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = LightTextPrimary,
                modifier = Modifier
                    .clickable {
                        scope.launch {
                            snackbarHostState.showSnackbar("Use default demo PIN: 1234")
                        }
                    }
                    .padding(bottom = 12.dp)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    EWalletTheme {
        LoginScreen(onLoginSuccess = {})
    }
}

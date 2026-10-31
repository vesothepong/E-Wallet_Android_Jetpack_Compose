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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.e_wallet.data.WalletRepository
import com.example.e_wallet.data.WalletRepositoryProvider
import com.example.e_wallet.ui.components.PocketPayIcons
import com.example.e_wallet.ui.theme.LightBackground
import com.example.e_wallet.ui.theme.LightBorder
import com.example.e_wallet.ui.theme.LightTextPrimary
import com.example.e_wallet.ui.theme.LightTextSecondary
import com.example.e_wallet.ui.theme.PrimaryBlue
import com.example.e_wallet.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

/**
 * Screen 3: KYC Signup (Create Account)
 * Form fields for Full Name, Email, Phone (+1), ID photo upload dashed card, terms checkbox, and CTA.
 */
@Composable
fun KycSignupScreen(
    onContinue: () -> Unit,
    onBackClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    repository: WalletRepository = WalletRepositoryProvider.repository
) {
    var fullName by remember { mutableStateOf("Kantumruy Dev") }
    var email by remember { mutableStateOf("kantumruy.dev@pocketpay.io") }
    var phoneNumber by remember { mutableStateOf("5550198810") }
    var isPhotoUploaded by remember { mutableStateOf(false) }
    var agreedToTerms by remember { mutableStateOf(true) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 12.dp)
        ) {
            // Top App Brand
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "PocketPay",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Header: Create Account + Step 2 of 3 badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LightTextPrimary
                )

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = PrimaryBlue.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = "Step 2 of 3",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = PrimaryBlue,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 1. Full Legal Name Field
            KycInputField(
                label = "Full Legal Name",
                value = fullName,
                onValueChange = { fullName = it },
                icon = PocketPayIcons.Person
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Email Address Field
            KycInputField(
                label = "Email Address",
                value = email,
                onValueChange = { email = it },
                icon = PocketPayIcons.Email
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Phone Number with (+1) Country Selector
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Text(
                        text = "Phone Number",
                        fontSize = 12.sp,
                        color = LightTextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "🇺🇸", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "(+1)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LightTextPrimary
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = PocketPayIcons.ChevronRight,
                            contentDescription = null,
                            tint = LightTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BasicTextField(
                            value = phoneNumber,
                            onValueChange = { phoneNumber = it },
                            modifier = Modifier.weight(1f),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = LightTextPrimary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 4. National ID / Passport Photo upload container (Dashed border)
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (isPhotoUploaded) SuccessGreen else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isPhotoUploaded = !isPhotoUploaded }
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "National ID / Passport Photo upload",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightTextPrimary,
                        modifier = Modifier.align(Alignment.Start)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(
                                if (isPhotoUploaded) SuccessGreen.copy(alpha = 0.12f)
                                else PrimaryBlue.copy(alpha = 0.08f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPhotoUploaded) PocketPayIcons.CheckCircle else PocketPayIcons.Camera,
                            contentDescription = "Upload ID",
                            tint = if (isPhotoUploaded) SuccessGreen else PrimaryBlue,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = if (isPhotoUploaded) "Document Verified (passport_sample.jpg)" else "Tap to upload identity document",
                        fontSize = 12.sp,
                        color = if (isPhotoUploaded) SuccessGreen else LightTextSecondary,
                        fontWeight = if (isPhotoUploaded) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Terms & Conditions Checkbox
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = agreedToTerms,
                    onCheckedChange = { agreedToTerms = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = PrimaryBlue,
                        uncheckedColor = LightBorder
                    )
                )
                Text(
                    text = "I agree to the Terms of Service & Privacy Policy",
                    fontSize = 13.sp,
                    color = LightTextPrimary,
                    modifier = Modifier.clickable { agreedToTerms = !agreedToTerms }
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // 6. Continue & Set PIN CTA (52px height)
            Button(
                onClick = {
                    if (!agreedToTerms) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please agree to the Terms of Service to continue")
                        }
                    } else {
                        repository.register(fullName, email, phoneNumber, "1234")
                        onContinue()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Continue & Set PIN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun KycInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, LightBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = LightTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = LightTextSecondary
                )
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightTextPrimary
                    )
                )
            }
        }
    }
}
